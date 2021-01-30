package personal.xingyuan.homework.gateway.upstream.connection;

import io.netty.channel.EventLoopGroup;
import personal.xingyuan.homework.gateway.util.NettyFactory;
import personal.xingyuan.homework.gateway.util.SystemUtil;

public abstract class ConnectionPoolBase<V> implements ConnectionPool<V> {

    protected static final EventLoopGroup EVENT_LOOP_GROUP = NettyFactory.eventLoopGroup(
            SystemUtil.getProcessorNumber(), "client-event-loop");

    public abstract Connection<V> take();

    public abstract void release(Connection<V> connection);
}
