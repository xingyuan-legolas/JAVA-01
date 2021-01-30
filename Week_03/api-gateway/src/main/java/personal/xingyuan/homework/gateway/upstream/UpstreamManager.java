package personal.xingyuan.homework.gateway.upstream;

import io.netty.handler.codec.http.HttpObject;
import personal.xingyuan.homework.gateway.config.GatewayConfig;
import personal.xingyuan.homework.gateway.config.ServiceConfig;
import personal.xingyuan.homework.gateway.model.Endpoint;
import personal.xingyuan.homework.gateway.upstream.connection.ConnectionPool;
import personal.xingyuan.homework.gateway.upstream.connection.FakeHttpConnectionPool;
import personal.xingyuan.homework.gateway.upstream.connection.HttpConnectionPool;
import personal.xingyuan.homework.gateway.upstream.routing.HttpRandomRouting;
import personal.xingyuan.homework.gateway.upstream.routing.HttpRoundRobinRouting;
import personal.xingyuan.homework.gateway.upstream.routing.HttpRouting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UpstreamManager {

    private final Map<String, HttpRouting> routings = new HashMap<>();

    public UpstreamManager(GatewayConfig gatewayConfig) {
        initializeRoutings(gatewayConfig);
    }

    public Upstream getUpstream(String host) {
        HttpRouting routing = routings.get(host);
        if (routing == null) {
            return null;
        }

        return routing.getUpstream();
    }

    private void initializeRoutings(GatewayConfig gatewayConfig) {
        Arrays.stream(gatewayConfig.getServices())
                .forEach(s -> routings.put(s.getHost(), createRouting(s, createUpstreams(s))));
    }

    private Upstream[] createUpstreams(ServiceConfig serviceConfig) {
        int poolSize = serviceConfig.getConnectionPoolSize();
        return Arrays.stream(serviceConfig.getUpstreams())
                .map(u -> new Upstream(u.getEndpoint(), createConnectionPool(u.getEndpoint(), poolSize), u.getWeight()))
                .toArray(Upstream[]::new);
    }

    private ConnectionPool<HttpObject> createConnectionPool(Endpoint endpoint, int poolSize) {
        if (poolSize <= 0) {
            return new FakeHttpConnectionPool(endpoint);
        }

        return new HttpConnectionPool(endpoint, poolSize);
    }

    private HttpRouting createRouting(ServiceConfig config, Upstream[] upstreams) {
        switch (config.getRoutingPolicyType()) {
            case RANDOM:
                return new HttpRandomRouting(config, upstreams);

            default:
                return new HttpRoundRobinRouting(config, upstreams);
        }
    }
}
