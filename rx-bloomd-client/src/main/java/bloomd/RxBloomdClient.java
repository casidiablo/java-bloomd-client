package bloomd;

import java.util.List;

import bloomd.args.CreateFilterArgs;
import bloomd.replies.BloomdFilter;
import bloomd.replies.BloomdInfo;
import bloomd.replies.ClearResult;
import bloomd.replies.CreateResult;
import bloomd.replies.StateResult;
import rx.Observable;

public interface RxBloomdClient {
    static RxBloomdClient newInstance(String host, int port) {
        return new RxBloomdClientImpl(host, port);
    }

    Observable<List<BloomdFilter>> list();

    Observable<List<BloomdFilter>> list(String prefix);

    Observable<CreateResult> create(String filterName);

    Observable<CreateResult> create(CreateFilterArgs args);

    Observable<Boolean> drop(String filterName);

    Observable<Boolean> close(String filterName);

    Observable<ClearResult> clear(String filterName);

    Observable<StateResult> check(String filterName, String key);

    Observable<StateResult> set(String filterName, String key);

    Observable<List<StateResult>> multi(String filterName, String... keys);

    Observable<List<StateResult>> bulk(String filterName, String... keys);

    Observable<BloomdInfo> info(String filterName);

    Observable<Boolean> flush(String filterName);
}
