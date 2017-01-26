package bloomd;

import bloomd.args.CreateFilterArgs;
import bloomd.replies.*;
import rx.Single;

import java.util.List;

public interface RxBloomdClient {
    Single<List<BloomdFilter>> list();

    Single<List<BloomdFilter>> list(String prefix);

    Single<CreateResult> create(String filterName);

    Single<CreateResult> create(CreateFilterArgs args);

    Single<Boolean> drop(String filterName);

    Single<Boolean> close(String filterName);

    Single<ClearResult> clear(String filterName);

    Single<StateResult> check(String filterName, String key);

    Single<StateResult> set(String filterName, String key);

    Single<List<StateResult>> multi(String filterName, String... keys);

    Single<List<StateResult>> bulk(String filterName, String... keys);

    Single<BloomdInfo> info(String filterName);

    Single<Boolean> flush(String filterName);

    Single<Boolean> closeConnections();
}
