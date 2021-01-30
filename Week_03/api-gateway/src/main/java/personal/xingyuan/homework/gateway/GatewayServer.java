package personal.xingyuan.homework.gateway;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import personal.xingyuan.homework.gateway.config.GatewayConfig;
import personal.xingyuan.homework.gateway.filter.FilterHandler;
import personal.xingyuan.homework.gateway.upstream.UpstreamHandler;
import personal.xingyuan.homework.gateway.upstream.UpstreamManager;
import personal.xingyuan.homework.gateway.util.NettyFactory;

public class GatewayServer {
    private final int port;
    private final UpstreamManager upstreamManager;

    public GatewayServer(GatewayConfig config) {
        this.port = config.getPort();
        this.upstreamManager = new UpstreamManager(config);
    }

    public void run() {
        EventLoopGroup bossGroup = NettyFactory.eventLoopGroup(1, "server-boss-event-loop");
        EventLoopGroup workerGroup = NettyFactory.eventLoopGroup(Runtime.getRuntime().availableProcessors(), "server-worker-event-loop");

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NettyFactory.serverSocketChannelClass())
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.AUTO_READ, Boolean.FALSE)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("http-decoder", new HttpRequestDecoder())
                                .addLast("http-encoder", new HttpResponseEncoder())
                                .addLast("http-filter", new FilterHandler())
                                .addLast("http-upstream", new UpstreamHandler(upstreamManager));
                    }
                });

        ChannelFuture channelFuture = bootstrap.bind(port).syncUninterruptibly();
        channelFuture.channel().closeFuture().syncUninterruptibly();
    }
}
