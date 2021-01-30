package personal.xingyuan.homework.gateway.upstream.connection;

import io.netty.util.concurrent.Future;

import java.util.function.Function;

public class DummyConnection<V> implements Connection<V> {

    @Override
    public boolean isPooled() {
        return false;
    }

    @Override
    public void register(Function<V, Future<Void>> onRead, Function<Throwable, Future<Void>> onError) {

    }

    @Override
    public Future<Void> connect() {
        return null;
    }

    @Override
    public Future<Void> write(V msg) {
        return null;
    }

    @Override
    public void release() {

    }
}
