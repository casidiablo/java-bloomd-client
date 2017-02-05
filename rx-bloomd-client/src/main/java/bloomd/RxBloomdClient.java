package bloomd;

import bloomd.args.CreateFilterArgs;
import bloomd.replies.BloomdFilter;
import bloomd.replies.BloomdInfo;
import bloomd.replies.ClearResult;
import bloomd.replies.CreateResult;
import bloomd.replies.StateResult;
import rx.Single;

import java.util.List;

/**
 * Important: if you need to add a timeout to the operation use
 * the overloaded method that accepts a timeout value in milliseconds
 * instead of using Single.timeout(). Otherwise there is no way
 * to ensure that connections are returned to the pool.
 */
public interface RxBloomdClient {
    Single<List<BloomdFilter>> list();

    Single<List<BloomdFilter>> list(long timeoutMillis);

    Single<List<BloomdFilter>> list(String prefix);

    Single<List<BloomdFilter>> list(String prefix, long timeoutMillis);

    Single<CreateResult> create(String filterName);

    Single<CreateResult> create(String filterName, long timeoutMillis);

    Single<CreateResult> create(CreateFilterArgs args);

    Single<CreateResult> create(CreateFilterArgs args, long timeoutMillis);

    Single<Boolean> drop(String filterName);

    Single<Boolean> drop(String filterName, long timeoutMillis);

    Single<Boolean> close(String filterName);

    Single<Boolean> close(String filterName, long timeoutMillis);

    Single<ClearResult> clear(String filterName);

    Single<ClearResult> clear(String filterName, long timeoutMillis);

    Single<StateResult> check(String filterName, String key);

    Single<StateResult> check(String filterName, String key, long timeoutMillis);

    Single<StateResult> set(String filterName, String key);

    Single<StateResult> set(String filterName, String key, long timeoutMillis);

    Single<List<StateResult>> multi(String filterName, String... keys);

    Single<List<StateResult>> multi(String filterName, long timeoutMillis, String... keys);

    Single<List<StateResult>> bulk(String filterName, String... keys);

    Single<List<StateResult>> bulk(String filterName, long timeoutMillis, String... keys);

    Single<BloomdInfo> info(String filterName);

    Single<BloomdInfo> info(String filterName, long timeoutMillis);

    Single<Boolean> flush(String filterName);

    Single<Boolean> flush(String filterName, long timeoutMillis);

    Single<Boolean> closeConnections();
}
