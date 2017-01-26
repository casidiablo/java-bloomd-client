package bloomd;

import bloomd.args.CreateFilterArgs;
import bloomd.replies.*;
import rx.Single;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RxBloomdClientImpl implements RxBloomdClient {

    private static final Logger LOG = Logger.getLogger(RxBloomdClient.class.getSimpleName());

    private final BloomdClientPool bloomdClientPool;

    public RxBloomdClientImpl(String host, int port) {
        this(host, port, 1, 10_000);
    }

    public RxBloomdClientImpl(String host, int port, int maxConnections, int acquireTimeoutMillis) {
        this.bloomdClientPool = new BloomdClientPool(host, port, maxConnections, acquireTimeoutMillis);
    }

    public RxBloomdClientImpl(BloomdClientPool bloomdClientPool) {
        this.bloomdClientPool = bloomdClientPool;
    }

    @Override
    public Single<List<BloomdFilter>> list() {
        return execute(BloomdClient::list);
    }

    @Override
    public Single<List<BloomdFilter>> list(String prefix) {
        return execute(client -> client.list(prefix));
    }

    @Override
    public Single<CreateResult> create(String filterName) {
        return execute(client -> client.create(filterName));
    }

    @Override
    public Single<CreateResult> create(CreateFilterArgs args) {
        return execute(client -> client.create(args));
    }

    @Override
    public Single<Boolean> drop(String filterName) {
        return execute(client -> client.drop(filterName));
    }

    @Override
    public Single<Boolean> close(String filterName) {
        return execute(client -> client.close(filterName));
    }

    @Override
    public Single<ClearResult> clear(String filterName) {
        return execute(client -> client.clear(filterName));
    }

    @Override
    public Single<StateResult> check(String filterName, String key) {
        return execute(client -> client.check(filterName, key));
    }

    @Override
    public Single<StateResult> set(String filterName, String key) {
        return execute(client -> client.set(filterName, key));
    }

    @Override
    public Single<List<StateResult>> multi(String filterName, String... keys) {
        return execute(client -> client.multi(filterName, keys));
    }

    @Override
    public Single<List<StateResult>> bulk(String filterName, String... keys) {
        return execute(client -> client.bulk(filterName, keys));
    }

    @Override
    public Single<BloomdInfo> info(String filterName) {
        return execute(client -> client.info(filterName));
    }

    @Override
    public Single<Boolean> flush(String filterName) {
        return execute(client -> client.flush(filterName));
    }

    @Override
    public Single<Boolean> closeConnections() {
        return Single.defer(() ->
                Single.from(bloomdClientPool.closeConnections())
                        .map(ignore -> true));
    }

    private <T> Single<T> execute(Function<BloomdClient, Future<T>> fn) {
        return Single.defer(() -> {
            // acquire a client from the current pool
            return Single
                    .from(bloomdClientPool.acquire())
                    .flatMap(client -> {
                        // execute actual computation and release the client from the pool
                        return Single
                                .from(fn.apply(client))
                                .doOnError(err -> {
                                    LOG.log(Level.WARNING, err, () -> "Failed to apply computation");
                                    bloomdClientPool.release(client);
                                })
                                .doOnSuccess(ignore -> bloomdClientPool.release(client));
                    });
        });
    }
}
