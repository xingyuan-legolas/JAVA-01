package personal.xingyuan.homework.gateway.upstream.connection;

import io.netty.util.concurrent.Future;

import java.util.function.Function;

public interface Connection<V> {

    boolean isPooled();

    void register(Function<V, Future<Void>> onRead, Function<Throwable, Future<Void>> onError);

    Future<Void> connect();

    Future<Void> write(V msg);

    void release();
}
