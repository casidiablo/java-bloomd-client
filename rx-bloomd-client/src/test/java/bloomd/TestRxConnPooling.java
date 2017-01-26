package bloomd;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.Single;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static bloomd.TestRxOperations.*;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class TestRxConnPooling {
    private static final Random RAND = new Random();
    private static final List<Function<RxBloomdClient, Single<?>>> OPS = new ArrayList<>();

    static {
        OPS.add(RxBloomdClient::list);
        OPS.add(client -> client.create(randomFilterName()));
        OPS.add(client -> client.drop(randomFilterName()));
        OPS.add(client -> client.close(randomFilterName()));
        OPS.add(client -> client.clear(randomFilterName()));
        OPS.add(client -> client.check(randomFilterName(), getRandomElem()));
        OPS.add(client -> client.set(randomFilterName(), getRandomElem()));
        OPS.add(client -> client.multi(randomFilterName(), getRandomElem(), getRandomElem(), getRandomElem()));
        OPS.add(client -> client.bulk(randomFilterName(), getRandomElem(), getRandomElem(), getRandomElem()));
        OPS.add(client -> client.info(randomFilterName()));
        OPS.add(client -> client.flush(randomFilterName()));
    }

    private String containerId;
    private ExecutorService executor;
    private RxBloomdClientImpl client;

    @Before
    public void setUp() throws Exception {
        // start bloomd service on a random port
        int port = randomPort();
        containerId = startBloomdInDocker(port);
        assertThat(containerId).isNotNull();

        // allow a few seconds for the container to be up and running
        Thread.sleep(3000);

        executor = Executors.newFixedThreadPool(50);
        client = new RxBloomdClientImpl(new BloomdClientPool("localhost", port, 20, 2000));
    }

    @Test
    public void rxPoolingTest() throws Exception {
        TestSubscriber subscriber = new TestSubscriber();
        Observable.range(1, 500)
                .flatMap(ignore -> randomOp(client)
                        .subscribeOn(Schedulers.from(executor))
                        .toObservable())
                .doOnNext(result -> assertThat(result).isIn("OK", "ERROR"))
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent(30, TimeUnit.SECONDS);
        subscriber.assertCompleted();
    }

    @After
    public void tearDown() throws Exception {
        assertThat(stopBloomdInDocker(containerId))
                .isEqualTo(containerId);

        executor.shutdown();
        client.closeConnections().timeout(10, TimeUnit.SECONDS).toBlocking().value();
    }

    private Single<String> randomOp(RxBloomdClient client) {
        return OPS
                .get(RAND.nextInt(OPS.size()))
                .apply(client)
                .map(ok -> "OK")
                .onErrorReturn(err -> "ERROR");
    }

    private static String getRandomElem() {
        return "random_elem_" + RAND.nextInt(5);
    }

    private static String randomFilterName() {
        return "random_filter_" + RAND.nextInt(3);
    }
}
