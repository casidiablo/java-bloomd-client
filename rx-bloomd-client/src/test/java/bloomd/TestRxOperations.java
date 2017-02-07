package bloomd;

import bloomd.replies.CreateResult;
import bloomd.replies.StateResult;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.Single;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.fail;

public class TestRxOperations {
    private static final String FILTER = "filter" + System.currentTimeMillis();

    private RxBloomdClient client;
    private String containerId;
    private int port;

    @Before
    public void setUp() throws Exception {
        // start bloomd service on a random port
        port = randomPort();
        containerId = startBloomdInDocker(port);
        assertThat(containerId).isNotNull();

        // allow a few seconds for the container to be up and running
        Thread.sleep(3000);

        client = new RxBloomdClientImpl("localhost", port);
    }

    @Test
    public void testBloomdOperations() throws Exception {
        // make sure filters can be created
        TestSubscriber<List<StateResult>> subscriber = new TestSubscriber<>();
        client.create(FILTER)
              .doOnSuccess(result -> assertThat(result).isEqualTo(CreateResult.DONE))


              // it should be present by listing it
              .flatMap(ignore -> client.list(FILTER))
              .doOnSuccess(filters -> {
                  assertThat(filters).hasSize(1);
                  assertThat(filters.get(0).getFilterName()).isEqualTo(FILTER);
              })

              // we should be able to set items
              .flatMap(ignore -> client.set(FILTER, "nishtiman"))
              .doOnSuccess(stateResult -> assertThat(stateResult).isEqualTo(StateResult.YES))

              // repeated item should return NO
              .flatMap(ignore -> client.set(FILTER, "nishtiman"))
              .doOnSuccess(stateResult -> assertThat(stateResult).isEqualTo(StateResult.NO))

              // the item we just added should be there when using check
              .flatMap(ignore -> client.check(FILTER, "nishtiman"))
              .doOnSuccess(stateResult -> assertThat(stateResult).isEqualTo(StateResult.YES))

              // checking for non-extant keys should return NO
              .flatMap(ignore -> client.check(FILTER, "non-extant-key"))
              .doOnSuccess(stateResult -> assertThat(stateResult).isEqualTo(StateResult.NO))

              // it should be possible to add items in bulk
              .flatMap(ignore -> client.bulk(FILTER, "fus", "dah"))
              .doOnSuccess(bulkResult -> {
                  assertThat(bulkResult).hasSize(2);
                  assertThat(bulkResult).isEqualTo(Arrays.asList(StateResult.YES, StateResult.YES));
              })

              // adding items that already exists should return "NO"
              .flatMap(ignore -> client.bulk(FILTER, "fus", "ro", "dah"))
              .doOnSuccess(bulkResult -> {
                  assertThat(bulkResult).hasSize(3);
                  assertThat(bulkResult).isEqualTo(Arrays.asList(StateResult.NO, StateResult.YES, StateResult.NO));
              })

              // we should be able to check the state of multiple keys
              .flatMap(ignore -> client.multi(FILTER, "fus", "ro", "dah", "non-extant-key"))
              .doOnSuccess(multiCheck -> assertThat(multiCheck).isEqualTo(
                      Arrays.asList(StateResult.YES, StateResult.YES, StateResult.YES, StateResult.NO)))


              // we should be able to inspect the filter stats
              .flatMap(ignore -> client.info(FILTER))
              .doOnSuccess(info -> {
                  assertThat(info.getCapacity()).isEqualTo(100000);
                  assertThat(info.getProbability()).isEqualTo(1f / 10000f);
                  assertThat(info.getSize()).isEqualTo(4);
                  assertThat(info.getStorage()).isEqualTo(300046);

                  assertThat(info.getChecks()).isEqualTo(6);
                  assertThat(info.getCheckHits()).isEqualTo(4);
                  assertThat(info.getCheckMisses()).isEqualTo(2);

                  assertThat(info.getSets()).isEqualTo(7);
                  assertThat(info.getSetHits()).isEqualTo(4);
                  assertThat(info.getSetMisses()).isEqualTo(3);

                  assertThat(info.isInMemory()).isTrue();
              })

              // we should be able to drop this filter
              .flatMap(ignore -> client.drop(FILTER))
              .doOnSuccess(dropResult -> assertThat(dropResult).isTrue())


              // the filter should not be found after this
              .flatMap(ignore -> client.list(FILTER))
              .doOnSuccess(list -> assertThat(list).isEmpty())


              // handle nonexistent filter check
              .flatMap(ignore -> client.bulk(FILTER, "something")
                      .doOnSuccess(__ -> fail("Should not have reached this closure"))
                      .onErrorResumeNext(err -> {
                          assertThat(err).isInstanceOf(FilterDoesNotExistException.class);
                          return Single.just(Collections.singletonList(StateResult.NO));
                      }))

              .subscribe(subscriber);

        subscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        subscriber.assertNoErrors();

        // stop bloomd server
        assertThat(stopBloomdInDocker(containerId))
                .isEqualTo(containerId);
    }

    @Test
    public void testDisconnection() throws Exception {
        TestSubscriber<StateResult> subscriber = new TestSubscriber<>();

        // ensure filter
        assertThat(client.create(FILTER).toBlocking().value()).isEqualTo(CreateResult.DONE);

        Observable.range(1, 100)
                  .flatMap(ignore -> {
                      try {
                          Thread.sleep(200);
                      } catch (InterruptedException e) {
                          e.printStackTrace();
                      }

                      return client
                              .check(FILTER, "foo")
                              .retry((integer, throwable) -> {
                                  assertThat(throwable).isInstanceOf(IllegalStateException.class);

                                  try {
                                      Thread.sleep(2000);
                                  } catch (InterruptedException e) {
                                      e.printStackTrace();
                                  }
                                  return true;
                              })
                              .toObservable();
                  })
                  .subscribeOn(Schedulers.io())
                  .subscribe(subscriber);

        // stop bloomd server
        assertThat(stopBloomdInDocker(containerId))
                .isEqualTo(containerId);

        // start the bloomd server again
        containerId = startBloomdInDocker(port);
        Thread.sleep(3000);

        // ensure filter
        assertThat(client.create(FILTER).toBlocking().value()).isEqualTo(CreateResult.DONE);

        subscriber.awaitTerminalEvent(30, TimeUnit.SECONDS);
        subscriber.assertCompleted();

        // should have finished processing all the elements
        subscriber.assertValueCount(100);

        // stop bloomd server for realz
        assertThat(stopBloomdInDocker(containerId))
                .isEqualTo(containerId);
    }

    public static int randomPort() {
        return 9000 + new Random().nextInt(5000);
    }

    public static String startBloomdInDocker(int port) {
        return runCommand("docker run -d -p " + port + ":8673 saidimu/bloomd:v0.7.4");
    }

    public static String stopBloomdInDocker(String containerId) {
        return runCommand("docker stop " + containerId);
    }

    private static String runCommand(String command) {
        //noinspection Duplicates
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            return stdInput.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
