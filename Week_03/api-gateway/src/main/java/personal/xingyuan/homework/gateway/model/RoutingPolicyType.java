package personal.xingyuan.homework.gateway.model;

public enum RoutingPolicyType {
    ROUND_ROBIN, RANDOM;

    public static RoutingPolicyType parseFrom(String text) throws IllegalArgumentException {
        switch (text) {
            case "round_robin":
                return RoutingPolicyType.ROUND_ROBIN;

            case "random":
                return RoutingPolicyType.RANDOM;

            default:
                throw new IllegalArgumentException(String.format("不支持的路由策略: %s", text));
        }
    }
}
