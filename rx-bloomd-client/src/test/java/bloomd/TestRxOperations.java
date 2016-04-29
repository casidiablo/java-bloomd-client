package bloomd;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import bloomd.replies.BloomdFilter;
import bloomd.replies.CreateResult;
import bloomd.replies.StateResult;
import rx.observers.TestSubscriber;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class TestRxOperations {
    private static final String FILTER = "filter" + System.currentTimeMillis();

    @Test
    public void testBloomdOperations() throws Exception {
        // start bloomd service on a random port
        int port = randomPort();
        String containerId = startBloomdInDocker(port);
        assertThat(containerId).isNotNull();

        // allow a few seconds for the container to be up and running
        Thread.sleep(3000);

        RxBloomdClient client = RxBloomdClient.newInstance("localhost", port);

        // make sure filters can be created
        TestSubscriber<List<BloomdFilter>> subscriber = new TestSubscriber<>();
        client.create(FILTER)
              .doOnNext(result -> assertThat(result).isEqualTo(CreateResult.DONE))


              // it should be present by listing it
              .flatMap(ignore -> client.list(FILTER))
              .doOnNext(filters -> {
                  assertThat(filters).hasSize(1);
                  assertThat(filters.get(0).getFilterName()).isEqualTo(FILTER);
              })

              // we should be able to set items
              .flatMap(ignore -> client.set(FILTER, "nishtiman"))
              .doOnNext(stateResult -> assertThat(stateResult).isEqualTo(StateResult.YES))

              // repeated item should return NO
              .flatMap(ignore -> client.set(FILTER, "nishtiman"))
              .doOnNext(stateResult -> assertThat(stateResult).isEqualTo(StateResult.NO))

              // the item we just added should be there when using check
              .flatMap(ignore -> client.check(FILTER, "nishtiman"))
              .doOnNext(stateResult -> assertThat(stateResult).isEqualTo(StateResult.YES))

              // checking for non-extant keys should return NO
              .flatMap(ignore -> client.check(FILTER, "non-extant-key"))
              .doOnNext(stateResult -> assertThat(stateResult).isEqualTo(StateResult.NO))

              // it should be possible to add items in bulk
              .flatMap(ignore -> client.bulk(FILTER, "fus", "dah"))
              .doOnNext(bulkResult -> {
                  assertThat(bulkResult).hasSize(2);
                  assertThat(bulkResult).isEqualTo(Arrays.asList(StateResult.YES, StateResult.YES));
              })

              // adding items that already exists should return "NO"
              .flatMap(ignore -> client.bulk(FILTER, "fus", "ro", "dah"))
              .doOnNext(bulkResult -> {
                  assertThat(bulkResult).hasSize(3);
                  assertThat(bulkResult).isEqualTo(Arrays.asList(StateResult.NO, StateResult.YES, StateResult.NO));
              })

              // we should be able to check the state of multiple keys
              .flatMap(ignore -> client.multi(FILTER, "fus", "ro", "dah", "non-extant-key"))
              .doOnNext(multiCheck -> assertThat(multiCheck).isEqualTo(
                      Arrays.asList(StateResult.YES, StateResult.YES, StateResult.YES, StateResult.NO)))


              // we should be able to inspect the filter stats
              .flatMap(ignore -> client.info(FILTER))
              .doOnNext(info -> {
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
              .doOnNext(dropResult -> assertThat(dropResult).isTrue())


              // the filter should not be found after this
              .flatMap(ignore -> client.list(FILTER))
              .doOnNext(list -> assertThat(list).isEmpty())

              .subscribe(subscriber);

        subscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        subscriber.assertNoErrors();

        // stop bloomd server
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
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            return stdInput.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
