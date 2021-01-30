package personal.xingyuan.homework.gateway.upstream.routing;

import personal.xingyuan.homework.gateway.config.ServiceConfig;
import personal.xingyuan.homework.gateway.upstream.Upstream;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

public class HttpRandomRouting implements HttpRouting {

    private final Upstream[] routingSequence;

    public HttpRandomRouting(ServiceConfig config, Upstream[] upstreams) {
        this.routingSequence = createRoutingSequence(upstreams);
    }

    @Override
    public Upstream getUpstream() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double weight = random.nextDouble(routingSequence[routingSequence.length - 1].getWeight());
        for (Upstream u : routingSequence) {
            if (weight < u.getWeight()) {
                return u;
            }
        }

        return routingSequence[routingSequence.length - 1];
    }

    private Upstream[] createRoutingSequence(Upstream[] upstreams) {
        Upstream[] routingSequence = Arrays.stream(upstreams)
                .sorted(Comparator.comparing(Upstream::getWeight).reversed()).toArray(Upstream[]::new);

        for (int i = 1; i < routingSequence.length; i++) {
            Upstream u = routingSequence[i];
            routingSequence[i] = new Upstream(u.getEndpoint(), u.getConnectionPool(),
                    u.getWeight() + routingSequence[i - 1].getWeight());
        }

        return routingSequence;
    }
}
