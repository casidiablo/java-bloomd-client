package bloomd;

import io.netty.util.concurrent.Future;
import rx.Observable;

public class RxBloomdClientPool {

    private final BloomdClientPool clientPool;

    public RxBloomdClientPool(String host, int port, int maxConnections) {
        this.clientPool = new BloomdClientPool(host, port, maxConnections);
    }

    /**
     * Acquire a {@link BloomdClient} from this {@link RxBloomdClientPool}. The returned {@link Future} is notified once
     * the acquire is successful and failed otherwise.
     */
    public Observable<RxBloomdClient> acquire() {
        return Observable
                .from(clientPool.acquire())
                .map(RxBloomdClientImpl::new);
    }

    /**
     * Release a {@link BloomdClient} back to this {@link RxBloomdClientPool}. The returned {@link Future} is notified once
     * the release is successful and failed otherwise. When failed the {@link BloomdClient} connection will automatically closed.
     */
    public Observable<Void> release(RxBloomdClient client) {
        BloomdClient underlyingClient = ((RxBloomdClientImpl) client).getUnderlyingClient();
        return Observable.from(clientPool.release(underlyingClient));
    }

    /**
     * Closes the connections for all clients in the pool
     */
    public Observable<?> closeConnections() {
        return Observable.from(clientPool.closeConnections());
    }
}
