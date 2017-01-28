package bloomd;

import bloomd.args.CreateFilterArgs;
import bloomd.replies.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@SuppressWarnings("SameParameterValue")
public interface BloomdClient {
    Future<List<BloomdFilter>> list();

    Future<List<BloomdFilter>> list(String prefix);

    Future<CreateResult> create(String filterName);

    Future<CreateResult> create(CreateFilterArgs args);

    Future<Boolean> drop(String filterName);

    Future<Boolean> close(String filterName);

    Future<ClearResult> clear(String filterName);

    Future<StateResult> check(String filterName, String key);

    Future<StateResult> set(String filterName, String key);

    Future<List<StateResult>> multi(String filterName, String... keys);

    Future<List<StateResult>> bulk(String filterName, String... keys);

    Future<BloomdInfo> info(String filterName);

    Future<Boolean> flush(String filterName);

    /**
     * @return a future that will resolve to a {@link BloomdClient} implementation
     */
    static CompletableFuture<BloomdClient> newInstance(String host, int port) {
        return newInstance(host, port, 1);
    }

    /**
     * @return a future that will resolve to a {@link BloomdClient} implementation
     */
    static CompletableFuture<BloomdClient> newInstance(String host, int port, int maxConnections) {
        return newInstance(host, port, maxConnections, 2_000, 2_000);
    }

    /**
     * @return a future that will resolve to a {@link BloomdClient} implementation
     */
    static CompletableFuture<BloomdClient> newInstance(String host, int port, int maxConnections, int connectionTimeoutMillis, int acquireTimeoutMillis) {
        return new BloomdClientPool(host, port, maxConnections, connectionTimeoutMillis, acquireTimeoutMillis).acquire();
    }
}
