package personal.xingyuan.homework.gateway.upstream;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.SucceededFuture;
import personal.xingyuan.homework.gateway.upstream.connection.Connection;
import personal.xingyuan.homework.gateway.util.HttpResponseUtil;

import java.util.LinkedList;
import java.util.Queue;

public class UpstreamHandler extends ChannelInboundHandlerAdapter {

    private final UpstreamManager upstreamManager;
    private final Queue<Object> buffer;
    private boolean inflight;
    private volatile Connection<HttpObject> connection;

    public UpstreamHandler(UpstreamManager upstreamManager) {
        this.upstreamManager = upstreamManager;
        this.buffer = new LinkedList<>();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // todo: support pipeline
        if (inflight || !buffer.isEmpty()) {
            buffer.offer(msg);
            return;
        }

        handleMessage(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        releaseConnection();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    private void releaseConnection(ChannelHandlerContext ctx) {
        ctx.executor().execute(this::releaseConnection);
    }

    private void releaseConnection() {
        if (connection != null) {
            connection.release();
            connection = null;
        }
    }

    private void handleMessage(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof DefaultHttpRequest) {
            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            handleDefaultHttpResult(ctx, request);
        } else {
            sendHttpMessage(ctx, (HttpObject) msg);
        }
    }

    private void handleDefaultHttpResult(ChannelHandlerContext ctx, DefaultHttpRequest request) {
        inflight = true;

        String host = request.headers().get(HttpHeaderNames.HOST);
        if (host == null) {
            flushAndClose(ctx, HttpResponseUtil.NOT_FOUND);
            ReferenceCountUtil.release(request);
            return;
        }

        Upstream upstream = upstreamManager.getUpstream(host);
        if (upstream == null) {
            flushAndClose(ctx, HttpResponseUtil.NOT_FOUND);
            ReferenceCountUtil.release(request);
            return;
        }

        Connection<HttpObject> connection = upstream.getConnectionPool().take();
        assert connection != null;
        connection.register(m -> handleResponseMessage(ctx, m), this::handleResponseError);
        this.connection = connection;
        connection.connect().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                    ctx.executor().execute(() -> sendHttpMessage(ctx, request));
                } else {
                    flushAndClose(ctx, HttpResponseUtil.BAD_GATEWAY);
                    releaseConnection(ctx);
                    future.cause().printStackTrace();
                }
            }
        });
    }

    private void sendHttpMessage(ChannelHandlerContext ctx, HttpObject msg) {
        connection.write(msg).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                    ctx.executor().execute(() -> {
                        Object nextMsg = buffer.poll();
                        if (nextMsg == null) {
                            inflight = false;
                            ctx.read();
                        } else {
                            handleMessage(ctx, nextMsg);
                        }
                    });
                } else {
                    flushAndClose(ctx, HttpResponseUtil.BAD_GATEWAY);
                    releaseConnection(ctx);
                    future.cause().printStackTrace();
                }
            }
        });
    }

    private Future<Void> handleResponseMessage(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg.decoderResult().isFailure()) {
            new SucceededFuture<>(ImmediateEventExecutor.INSTANCE, null);
        }

        return ctx.writeAndFlush(msg);
    }

    private Future<Void> handleResponseError(Throwable cause) {
        cause.printStackTrace();
        return new SucceededFuture<>(ImmediateEventExecutor.INSTANCE, null);
    }

    private void flushAndClose(ChannelHandlerContext ctx, HttpObject msg) {
        ctx.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE);
    }
}
