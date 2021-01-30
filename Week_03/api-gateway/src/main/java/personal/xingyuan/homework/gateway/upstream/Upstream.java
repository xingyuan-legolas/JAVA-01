package personal.xingyuan.homework.gateway.upstream;

import io.netty.handler.codec.http.HttpObject;
import personal.xingyuan.homework.gateway.model.Endpoint;
import personal.xingyuan.homework.gateway.upstream.connection.ConnectionPool;

public class Upstream {
    private final Endpoint endpoint;
    private final ConnectionPool<HttpObject> connectionPool;
    private final double weight;

    public Upstream(Endpoint endpoint, ConnectionPool<HttpObject> connectionPool, double weight) {
        this.endpoint = endpoint;
        this.connectionPool = connectionPool;
        this.weight = weight;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public ConnectionPool<HttpObject> getConnectionPool() {
        return connectionPool;
    }

    public double getWeight() {
        return weight;
    }
}
