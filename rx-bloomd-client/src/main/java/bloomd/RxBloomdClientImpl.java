package bloomd;

import java.util.List;
import java.util.concurrent.TimeUnit;

import bloomd.args.CreateFilterArgs;
import bloomd.replies.BloomdFilter;
import bloomd.replies.BloomdInfo;
import bloomd.replies.ClearResult;
import bloomd.replies.CreateResult;
import bloomd.replies.StateResult;
import rx.Single;

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
    public Single<List<BloomdFilter>> list() {
        return Single.from(client.list());
    }

    @Override
    public Single<List<BloomdFilter>> list(String prefix) {
        return Single.from(client.list(prefix));
    }

    @Override
    public Single<CreateResult> create(String filterName) {
        return Single.from(client.create(filterName));
    }

    @Override
    public Single<CreateResult> create(CreateFilterArgs args) {
        return Single.from(client.create(args));
    }

    @Override
    public Single<Boolean> drop(String filterName) {
        return Single.from(client.drop(filterName));
    }

    @Override
    public Single<Boolean> close(String filterName) {
        return Single.from(client.close(filterName));
    }

    @Override
    public Single<ClearResult> clear(String filterName) {
        return Single.from(client.clear(filterName));
    }

    @Override
    public Single<StateResult> check(String filterName, String key) {
        return Single.from(client.check(filterName, key));
    }

    @Override
    public Single<StateResult> set(String filterName, String key) {
        return Single.from(client.set(filterName, key));
    }

    @Override
    public Single<List<StateResult>> multi(String filterName, String... keys) {
        return Single.from(client.multi(filterName, keys));
    }

    @Override
    public Single<List<StateResult>> bulk(String filterName, String... keys) {
        return Single.from(client.bulk(filterName, keys));
    }

    @Override
    public Single<BloomdInfo> info(String filterName) {
        return Single.from(client.info(filterName));
    }

    @Override
    public Single<Boolean> flush(String filterName) {
        return Single.from(client.flush(filterName));
    }

    public BloomdClient getUnderlyingClient() {
        return client;
    }
}
