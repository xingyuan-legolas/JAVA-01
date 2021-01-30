package personal.xingyuan.homework.gateway.upstream.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.util.concurrent.*;
import personal.xingyuan.homework.gateway.model.Endpoint;
import personal.xingyuan.homework.gateway.util.NettyFactory;

import java.util.function.Function;

public class HttpConnection implements Connection<HttpObject> {

    private final static Function<HttpObject, Future<Void>> NoopReadHandler = msg -> new SucceededFuture<>(ImmediateEventExecutor.INSTANCE, null);
    private final static Function<Throwable, Future<Void>> NoopErrorHandler = cause -> new SucceededFuture<>(ImmediateEventExecutor.INSTANCE, null);

    private final Bootstrap bootstrap;
    private final ConnectionPool<HttpObject> pool;
    private volatile Channel channel;
    private volatile Function<HttpObject, Future<Void>> readHandler;
    private volatile Function<Throwable, Future<Void>> errorHandler;

    public HttpConnection(EventLoopGroup eventLoopGroup, Endpoint endpoint, ConnectionPool<HttpObject> pool) {
        this.bootstrap = createBootstrap(eventLoopGroup, endpoint);
        this.pool = pool;
        this.readHandler = NoopReadHandler;
        this.errorHandler = NoopErrorHandler;
    }

    @Override
    public boolean isPooled() {
        return pool != null;
    }

    @Override
    public void register(Function<HttpObject, Future<Void>> onRead, Function<Throwable, Future<Void>> onError) {
        this.readHandler = onRead;
        this.errorHandler = onError;
    }

    @Override
    public void release() {
        readHandler = NoopReadHandler;
        errorHandler = NoopErrorHandler;

        if (pool == null) {
            close();
        } else {
            pool.release(this);
        }
    }

    @Override
    public Future<Void> connect() {
        if (channel == null || !channel.isActive()) {
            ChannelFuture future = bootstrap.connect();
            channel = future.channel();
            return future;
        }

        return new SucceededFuture<>(ImmediateEventExecutor.INSTANCE, null);
    }

    @Override
    public Future<Void> write(HttpObject msg) {
//        if (channel == null || !channel.isActive()) {
//            Promise<Void> promise = new DefaultPromise<>(ImmediateEventExecutor.INSTANCE);
//            Future<Void> future = connect();
//            future.addListener(new ChannelFutureListener() {
//                @Override
//                public void operationComplete(ChannelFuture future) throws Exception {
//                    if (future.isSuccess()) {
//                        future.channel().writeAndFlush(msg).addListener(new ChannelFutureListener() {
//                            @Override
//                            public void operationComplete(ChannelFuture future) throws Exception {
//                                if (future.isSuccess()) {
//                                    promise.trySuccess(null);
//                                } else {
//                                    close();
//                                    promise.tryFailure(future.cause());
//                                }
//                            }
//                        });
//                    } else {
//                        promise.tryFailure(future.cause());
//                    }
//                }
//            });
//
//            return promise;
//        }

        return channel.writeAndFlush(msg);
    }

    private void close() {
        if (channel != null && channel.isActive()) {
            channel.close();
        }

        channel = null;
    }

    private Bootstrap createBootstrap(EventLoopGroup eventLoopGroup, Endpoint endpoint) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                // todo: timeout config
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.AUTO_READ, Boolean.FALSE)
                .remoteAddress(endpoint.getAddress(), endpoint.getPort())
                .channel(NettyFactory.socketChannelClass())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("http-client-encoder", new HttpRequestEncoder())
                                .addLast("http-client-decoder", new HttpResponseDecoder())
                                .addLast("http-client-handler", new HttpClientHandler());
                    }
                });

        return bootstrap;
    }

    private class HttpClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.read();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            readHandler.apply((HttpObject) msg).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (future.isSuccess()) {
                        ctx.read();;
                    } else {
                        errorHandler.apply(future.cause());
                    }
                }
            });
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
        }
    }
}
