package personal.xingyuan.homework.gateway.upstream.connection;

public interface ConnectionPool<V> {

    Connection<V> take();

    void release(Connection<V> connection);
}
