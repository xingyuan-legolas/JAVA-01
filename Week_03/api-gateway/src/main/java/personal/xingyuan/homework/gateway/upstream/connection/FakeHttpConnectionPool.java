package personal.xingyuan.homework.gateway.upstream.connection;

import io.netty.handler.codec.http.HttpObject;
import personal.xingyuan.homework.gateway.model.Endpoint;

public class FakeHttpConnectionPool extends ConnectionPoolBase<HttpObject> implements ConnectionPool<HttpObject> {

    private final Endpoint endpoint;

    public FakeHttpConnectionPool(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Connection<HttpObject> take() {
        return new HttpConnection(EVENT_LOOP_GROUP, endpoint, null);
    }

    @Override
    public void release(Connection<HttpObject> connection) {

    }
}
