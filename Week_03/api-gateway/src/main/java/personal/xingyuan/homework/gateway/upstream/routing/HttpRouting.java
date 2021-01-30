package personal.xingyuan.homework.gateway.upstream.routing;

import personal.xingyuan.homework.gateway.upstream.Upstream;

public interface HttpRouting {
    Upstream getUpstream();
}
