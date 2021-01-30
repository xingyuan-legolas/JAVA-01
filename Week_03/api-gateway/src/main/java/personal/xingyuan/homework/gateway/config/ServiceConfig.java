package personal.xingyuan.homework.gateway.config;

import personal.xingyuan.homework.gateway.exception.ConfigParseException;
import personal.xingyuan.homework.gateway.model.RoutingPolicyType;

import java.util.List;
import java.util.Map;

public class ServiceConfig {
    private final String host;
    private final int connectionPoolSize;
    private final RoutingPolicyType routingPolicyType;
    private final UpstreamConfig[] upstreams;

    public ServiceConfig(String host, int connectionPoolSize, RoutingPolicyType routingPolicyType, UpstreamConfig[] upstreams) {
        this.host = host;
        this.connectionPoolSize = connectionPoolSize;
        this.routingPolicyType = routingPolicyType;
        this.upstreams = upstreams;
    }

    public String getHost() {
        return host;
    }

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public RoutingPolicyType getRoutingPolicyType() {
        return routingPolicyType;
    }

    public UpstreamConfig[] getUpstreams() {
        return upstreams;
    }

    static Builder newBuilder(Map<String, Object> source) {
        return new Builder(source);
    }

    static class Builder {
        private final Map<String, Object> source;

        public Builder(Map<String, Object> source) {
            this.source = source;
        }

        public ServiceConfig build() throws ConfigParseException {
            try {
                String host = (String) source.get("host");
                int connectionPoolSize = (int) source.getOrDefault("connection_pool_size", 0);

                RoutingPolicyType routingPolicyType = RoutingPolicyType.parseFrom(
                        (String) source.getOrDefault("routing_policy", "round_robin"));

                UpstreamConfig[] upstreams = ((List<Map<String, Object>>) source.get("upstreams")).stream()
                        .map(s -> UpstreamConfig.newBuilder(s).build())
                        .toArray(UpstreamConfig[]::new);

                return new ServiceConfig(host, connectionPoolSize, routingPolicyType, upstreams);
            } catch (Exception e) {
                throw new ConfigParseException(e);
            }
        }
    }
}
