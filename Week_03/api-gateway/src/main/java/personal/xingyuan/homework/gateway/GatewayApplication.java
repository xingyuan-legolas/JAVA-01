package personal.xingyuan.homework.gateway;

import personal.xingyuan.homework.gateway.config.GatewayConfig;

public class GatewayApplication {
    public static void main(String[] args) throws Exception {
        String defaultConfigPath = GatewayApplication.class.getClassLoader().getResource("config.yaml").getPath();
        GatewayConfig config = GatewayConfig.newBuilder(defaultConfigPath).build();

        new GatewayServer(config).run();
    }
}
