package personal.xingyuan.homework.gateway.config;

import personal.xingyuan.homework.gateway.exception.ConfigParseException;
import personal.xingyuan.homework.gateway.model.Endpoint;

import java.util.Map;

public class UpstreamConfig {
    private final Endpoint endpoint;
    private final double weight;

    UpstreamConfig(Endpoint endpoint, double weight) {
        this.endpoint = endpoint;
        this.weight = weight;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public double getWeight() {
        return weight;
    }

    static Builder newBuilder(Map<String, Object> source) {
        return new Builder(source);
    }

    static class Builder {
        private final Map<String, Object> source;

        public Builder(Map<String, Object> source) {
            this.source = source;
        }

        public UpstreamConfig build() throws ConfigParseException {
            try {
                String address = (String) source.get("address");
                int port = (int) source.get("port");
                double weight = Double.parseDouble(source.getOrDefault("weight", 1).toString());

                return new UpstreamConfig(new Endpoint(address, port), weight);
            } catch (Exception e) {
                throw new ConfigParseException(e);
            }
        }
    }
}
