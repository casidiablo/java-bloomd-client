package bloomd;

import java.util.List;
import java.util.concurrent.Future;

import bloomd.replies.BloomdFilter;
import bloomd.replies.ClearResult;
import bloomd.replies.BloomdInfo;
import bloomd.replies.CreateResult;
import bloomd.replies.StateResult;
import bloomd.args.CreateFilterArgs;

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
}
