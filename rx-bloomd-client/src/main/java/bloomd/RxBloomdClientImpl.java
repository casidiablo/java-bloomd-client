package bloomd;

import java.util.List;
import java.util.concurrent.TimeUnit;

import bloomd.args.CreateFilterArgs;
import bloomd.replies.BloomdFilter;
import bloomd.replies.BloomdInfo;
import bloomd.replies.ClearResult;
import bloomd.replies.CreateResult;
import bloomd.replies.StateResult;
import rx.Observable;

public class RxBloomdClientImpl implements RxBloomdClient {

    private final BloomdClient client;

    public RxBloomdClientImpl(String host, int port) {
        this(host, port, 10);
    }

    public RxBloomdClientImpl(String host, int port, int timeoutSeconds) {
        try {
            client = BloomdClient
                    .newInstance(host, port)
                    .get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public RxBloomdClientImpl(BloomdClient client) {
        this.client = client;
    }

    @Override
    public Observable<List<BloomdFilter>> list() {
        return Observable.from(client.list());
    }

    @Override
    public Observable<List<BloomdFilter>> list(String prefix) {
        return Observable.from(client.list(prefix));
    }

    @Override
    public Observable<CreateResult> create(String filterName) {
        return Observable.from(client.create(filterName));
    }

    @Override
    public Observable<CreateResult> create(CreateFilterArgs args) {
        return Observable.from(client.create(args));
    }

    @Override
    public Observable<Boolean> drop(String filterName) {
        return Observable.from(client.drop(filterName));
    }

    @Override
    public Observable<Boolean> close(String filterName) {
        return Observable.from(client.close(filterName));
    }

    @Override
    public Observable<ClearResult> clear(String filterName) {
        return Observable.from(client.clear(filterName));
    }

    @Override
    public Observable<StateResult> check(String filterName, String key) {
        return Observable.from(client.check(filterName, key));
    }

    @Override
    public Observable<StateResult> set(String filterName, String key) {
        return Observable.from(client.set(filterName, key));
    }

    @Override
    public Observable<List<StateResult>> multi(String filterName, String... keys) {
        return Observable.from(client.multi(filterName, keys));
    }

    @Override
    public Observable<List<StateResult>> bulk(String filterName, String... keys) {
        return Observable.from(client.bulk(filterName, keys));
    }

    @Override
    public Observable<BloomdInfo> info(String filterName) {
        return Observable.from(client.info(filterName));
    }

    @Override
    public Observable<Boolean> flush(String filterName) {
        return Observable.from(client.flush(filterName));
    }

    public BloomdClient getUnderlyingClient() {
        return client;
    }
}
