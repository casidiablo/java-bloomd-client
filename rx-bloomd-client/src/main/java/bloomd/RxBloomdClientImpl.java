package bloomd;

import bloomd.args.CreateFilterArgs;
import bloomd.replies.BloomdFilter;
import bloomd.replies.BloomdInfo;
import bloomd.replies.ClearResult;
import bloomd.replies.CreateResult;
import bloomd.replies.StateResult;
import rx.Single;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RxBloomdClientImpl implements RxBloomdClient {

    private static final Logger LOG = Logger.getLogger(RxBloomdClient.class.getSimpleName());

    private final BloomdClientPool bloomdClientPool;

    public RxBloomdClientImpl(String host, int port) {
        this(host, port, 1, 2_000, 2_000);
    }

    public RxBloomdClientImpl(String host, int port, int maxConnections, int connectionTimeoutMillis, int acquireTimeoutMillis) {
        this(new BloomdClientPool(host, port, maxConnections, connectionTimeoutMillis, acquireTimeoutMillis));
    }

    public RxBloomdClientImpl(BloomdClientPool bloomdClientPool) {
        this.bloomdClientPool = bloomdClientPool;
    }

    @Override
    public Single<List<BloomdFilter>> list() {
        return execute(BloomdClient::list);
    }

    @Override
    public Single<List<BloomdFilter>> list(long timeoutMillis) {
        return execute(BloomdClient::list, timeoutMillis);
    }

    @Override
    public Single<List<BloomdFilter>> list(String prefix) {
        return execute(client -> client.list(prefix));
    }

    @Override
    public Single<List<BloomdFilter>> list(String prefix, long timeoutMillis) {
        return execute(client -> client.list(prefix), timeoutMillis);
    }

    @Override
    public Single<CreateResult> create(String filterName) {
        return execute(client -> client.create(filterName));
    }

    @Override
    public Single<CreateResult> create(String filterName, long timeoutMillis) {
        return execute(client -> client.create(filterName), timeoutMillis);
    }

    @Override
    public Single<CreateResult> create(CreateFilterArgs args) {
        return execute(client -> client.create(args));
    }

    @Override
    public Single<CreateResult> create(CreateFilterArgs args, long timeoutMillis) {
        return execute(client -> client.create(args), timeoutMillis);
    }

    @Override
    public Single<Boolean> drop(String filterName) {
        return execute(client -> client.drop(filterName));
    }

    @Override
    public Single<Boolean> drop(String filterName, long timeoutMillis) {
        return execute(client -> client.drop(filterName), timeoutMillis);
    }

    @Override
    public Single<Boolean> close(String filterName) {
        return execute(client -> client.close(filterName));
    }

    @Override
    public Single<Boolean> close(String filterName, long timeoutMillis) {
        return execute(client -> client.close(filterName), timeoutMillis);
    }

    @Override
    public Single<ClearResult> clear(String filterName) {
        return execute(client -> client.clear(filterName));
    }

    @Override
    public Single<ClearResult> clear(String filterName, long timeoutMillis) {
        return execute(client -> client.clear(filterName), timeoutMillis);
    }

    @Override
    public Single<StateResult> check(String filterName, String key) {
        return execute(client -> client.check(filterName, key));
    }

    @Override
    public Single<StateResult> check(String filterName, String key, long timeoutMillis) {
        return execute(client -> client.check(filterName, key), timeoutMillis);
    }

    @Override
    public Single<StateResult> set(String filterName, String key) {
        return execute(client -> client.set(filterName, key));
    }

    @Override
    public Single<StateResult> set(String filterName, String key, long timeoutMillis) {
        return execute(client -> client.set(filterName, key), timeoutMillis);
    }

    @Override
    public Single<List<StateResult>> multi(String filterName, String... keys) {
        return execute(client -> client.multi(filterName, keys));
    }

    @Override
    public Single<List<StateResult>> multi(String filterName, long timeoutMillis, String... keys) {
        return execute(client -> client.multi(filterName, keys), timeoutMillis);
    }

    @Override
    public Single<List<StateResult>> bulk(String filterName, String... keys) {
        return execute(client -> client.bulk(filterName, keys));
    }

    @Override
    public Single<List<StateResult>> bulk(String filterName, long timeoutMillis, String... keys) {
        return execute(client -> client.bulk(filterName, keys), timeoutMillis);
    }

    @Override
    public Single<BloomdInfo> info(String filterName) {
        return execute(client -> client.info(filterName));
    }

    @Override
    public Single<BloomdInfo> info(String filterName, long timeoutMillis) {
        return execute(client -> client.info(filterName), timeoutMillis);
    }

    @Override
    public Single<Boolean> flush(String filterName) {
        return execute(client -> client.flush(filterName));
    }

    @Override
    public Single<Boolean> flush(String filterName, long timeoutMillis) {
        return execute(client -> client.flush(filterName), timeoutMillis);
    }

    @Override
    public Single<Boolean> closeConnections() {
        return Single.defer(() ->
                Single.from(bloomdClientPool.closeConnections())
                        .map(ignore -> true));
    }

    private <T> Single<T> execute(Function<BloomdClient, Future<T>> fn) {
        String origin = origin();
        return Single.defer(() -> doExecute(fn, Long.MAX_VALUE, origin));
    }

    private <T> Single<T> execute(Function<BloomdClient, Future<T>> fn, long timeoutMillis) {
        String origin = origin();
        return Single.defer(() -> doExecute(fn, timeoutMillis, origin));
    }

    private static String origin() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        return stackTrace[2].getMethodName();
    }

    private <T> Single<T> doExecute(Function<BloomdClient, Future<T>> fn, long timeoutMillis, String origin) {
        AtomicBoolean alreadyReleased = new AtomicBoolean(false);
        CompletableFuture<BloomdClient> acquire = bloomdClientPool.acquire();
        return Single
                .from(acquire)
                .flatMap(client -> {
                    // execute actual computation and release the client from the pool
                    Single<T> computation = Single.from(fn.apply(client));
                    if (timeoutMillis != Long.MAX_VALUE) {
                        computation = computation.timeout(timeoutMillis, TimeUnit.MILLISECONDS);
                    }

                    return computation
                            .onErrorResumeNext(err -> {
                                if (err instanceof ExecutionException) {
                                    err = err.getCause();
                                }

                                LOG.log(Level.WARNING, err, () -> "Failed to apply computation: " + origin);
                                alreadyReleased.set(true);
                                bloomdClientPool.release(client);

                                return Single.error(err);
                            })
                            .doOnSuccess(ignore -> {
                                alreadyReleased.set(true);
                                bloomdClientPool.release(client);
                            });
                })
                .doOnUnsubscribe(() -> {
                    if (!alreadyReleased.get()) {
                        try {
                            bloomdClientPool.release(acquire.get());
                        } catch (Throwable e) {
                            LOG.log(Level.SEVERE, e, () -> "Failed to release connection");
                        }
                    }
                });
    }
}
