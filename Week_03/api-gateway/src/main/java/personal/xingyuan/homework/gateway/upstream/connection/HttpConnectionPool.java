package personal.xingyuan.homework.gateway.upstream.connection;

import io.netty.handler.codec.http.HttpObject;
import personal.xingyuan.homework.gateway.model.Endpoint;

import java.util.concurrent.ConcurrentLinkedQueue;

public class HttpConnectionPool extends ConnectionPoolBase<HttpObject> implements ConnectionPool<HttpObject> {

    private static final DummyConnection<HttpObject> DUMMY_CONNECTION = new DummyConnection<>();

    private final Endpoint endpoint;
    private final ConcurrentLinkedQueue<Connection<HttpObject>> pool;

    public HttpConnectionPool(Endpoint endpoint, int poolSize) {
        this.endpoint = endpoint;
        this.pool = createFullPool(poolSize);
    }

    @Override
    public Connection<HttpObject> take() {
        Connection<HttpObject> connection = pool.poll();
        if (connection == null) {
            return new HttpConnection(EVENT_LOOP_GROUP, endpoint, null);
        }

        if (connection == DUMMY_CONNECTION) {
            connection = new HttpConnection(EVENT_LOOP_GROUP, endpoint, this);
        }

        return connection;
    }

    @Override
    public void release(Connection<HttpObject> connection) {
        if (connection.isPooled()) {
            pool.add(connection);
        }
    }

    private ConcurrentLinkedQueue<Connection<HttpObject>> createFullPool(int poolSize) {
        assert poolSize > 0;
        ConcurrentLinkedQueue<Connection<HttpObject>> pool = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < poolSize; i++) {
            pool.add(DUMMY_CONNECTION);
        }

        return pool;
    }
}
