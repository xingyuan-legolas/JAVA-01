package personal.xingyuan.homework.gateway.config;

import org.yaml.snakeyaml.Yaml;
import personal.xingyuan.homework.gateway.exception.ConfigParseException;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class GatewayConfig {
    private final int port;
    private final ServiceConfig[] services;

    public GatewayConfig(int port, ServiceConfig[] services) {
        this.port = port;
        this.services = services;
    }

    public int getPort() {
        return port;
    }

    public ServiceConfig[] getServices() {
        return services;
    }

    public static Builder newBuilder(String path) {
        return new Builder(path);
    }

    public static class Builder {
        private final String path;

        public Builder(String path) {
            this.path = path;
        }

        public GatewayConfig build() throws ConfigParseException {
            try (InputStream inputStream = new FileInputStream(path)) {
                Map<String, Object> source = new Yaml().load(inputStream);

                int port = (int) source.getOrDefault("port", 80);

                ServiceConfig[] services = ((List<Map<String, Object>>) source.get("services")).stream()
                        .map(s -> ServiceConfig.newBuilder(s).build())
                        .toArray(ServiceConfig[]::new);

                return new GatewayConfig(port, services);
            } catch (Exception e) {
                throw new ConfigParseException(e);
            }
        }
    }
}
