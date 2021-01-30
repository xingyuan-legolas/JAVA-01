package personal.xingyuan.homework.gateway.upstream.routing;

import personal.xingyuan.homework.gateway.config.ServiceConfig;
import personal.xingyuan.homework.gateway.upstream.Upstream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class HttpRoundRobinRouting implements HttpRouting {

    private static final int MAX_PER_ROUND = 128;

    private final Upstream[] routingSequence;
    private final AtomicInteger index;
    private final Supplier<Upstream> routeHandler;

    public HttpRoundRobinRouting(ServiceConfig config, Upstream[] upstreams) {
        this.index = new AtomicInteger();
        this.routingSequence = createRoutingSequence(upstreams).stream().map(o -> (Upstream) o).toArray(Upstream[]::new);
        this.routeHandler = createRoutingHandler();
    }

    @Override
    public Upstream getUpstream() {
        return routeHandler.get();
    }

    private ArrayList<Upstream> createRoutingSequence(Upstream[] upstreams) {

        double[] weight = new double[upstreams.length];
        double total = Arrays.stream(upstreams).map(Upstream::getWeight).reduce(0d, Double::sum);

        ArrayList<Upstream> sequence = new ArrayList<>(MAX_PER_ROUND);
        for (int i = 0; i < MAX_PER_ROUND; i++) {
            int maxIndex = 0;
            double maxWeight = Double.MIN_VALUE;
            for (int j = 0; j < weight.length; j++) {
                weight[j] += upstreams[j].getWeight();
                if (weight[j] > maxWeight) {
                    maxWeight = weight[j];
                    maxIndex = j;
                }
            }

            sequence.add(upstreams[maxIndex]);
            weight[maxIndex] -= total;

            if (isAllZero(weight)) {
                break;
            }
        }

        return sequence;
    }

    private boolean isAllZero(double[] weight) {
        for (double w : weight) {
            if (w != 0) {
                return false;
            }
        }

        return true;
    }

    private Supplier<Upstream> createRoutingHandler() {
        return (routingSequence.length & (routingSequence.length - 1)) == 0 ?
                this::getUpstreamOnPowOfTwo : this::getUpstreamOnGeneric;
    }

    private Upstream getUpstreamOnGeneric() {
        int idx = Math.abs(index.getAndIncrement()) % routingSequence.length;
        return routingSequence[idx];
    }

    private Upstream getUpstreamOnPowOfTwo() {
        int idx = index.getAndIncrement() & (routingSequence.length - 1);
        return routingSequence[idx];
    }
}
