package bloomd;

import java.io.IOException;
import java.util.List;

import bloomd.args.CreateFilterArgs;
import bloomd.replies.BloomdFilter;
import bloomd.replies.BloomdInfo;
import bloomd.replies.ClearResult;
import bloomd.replies.CreateResult;
import bloomd.replies.StateResult;
import rx.Observable;

public class RxBloomdClientImpl implements RxBloomdClient {

    private final NettyBloomdClient nettyBloomdClient;

    public RxBloomdClientImpl(String host, int port) throws IOException, InterruptedException {
        nettyBloomdClient = new NettyBloomdClient(host, port);
    }

    @Override
    public Observable<List<BloomdFilter>> list() {
        return Observable.from(nettyBloomdClient.list());
    }

    @Override
    public Observable<List<BloomdFilter>> list(String prefix) {
        return Observable.from(nettyBloomdClient.list(prefix));
    }

    @Override
    public Observable<CreateResult> create(String filterName) {
        return Observable.from(nettyBloomdClient.create(filterName));
    }

    @Override
    public Observable<CreateResult> create(CreateFilterArgs args) {
        return Observable.from(nettyBloomdClient.create(args));
    }

    @Override
    public Observable<Boolean> drop(String filterName) {
        return Observable.from(nettyBloomdClient.drop(filterName));
    }

    @Override
    public Observable<Boolean> close(String filterName) {
        return Observable.from(nettyBloomdClient.close(filterName));
    }

    @Override
    public Observable<ClearResult> clear(String filterName) {
        return Observable.from(nettyBloomdClient.clear(filterName));
    }

    @Override
    public Observable<StateResult> check(String filterName, String key) {
        return Observable.from(nettyBloomdClient.check(filterName, key));
    }

    @Override
    public Observable<StateResult> set(String filterName, String key) {
        return Observable.from(nettyBloomdClient.set(filterName, key));
    }

    @Override
    public Observable<List<StateResult>> multi(String filterName, String... keys) {
        return Observable.from(nettyBloomdClient.multi(filterName, keys));
    }

    @Override
    public Observable<List<StateResult>> bulk(String filterName, String... keys) {
        return Observable.from(nettyBloomdClient.bulk(filterName, keys));
    }

    @Override
    public Observable<BloomdInfo> info(String filterName) {
        return Observable.from(nettyBloomdClient.info(filterName));
    }

    @Override
    public Observable<Boolean> flush(String filterName) {
        return Observable.from(nettyBloomdClient.flush(filterName));
    }
}
