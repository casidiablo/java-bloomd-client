package bloomd;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import bloomd.args.CreateFilterArgs;
import bloomd.replies.BloomdInfo;
import bloomd.replies.CreateResult;
import bloomd.replies.StateResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.fail;

public class ParallelismTest extends DockerBasedTest {
    private static final String FILTER = "filterName" + System.currentTimeMillis();

    @Test
    public void testParallelRequests() throws Exception {
        BloomdClient client = BloomdClient
                .newInstance("localhost", port)
                .get(1, TimeUnit.SECONDS);

        // create filter with at least 200000 capacity
        CreateFilterArgs createArgs = new CreateFilterArgs.Builder()
                .setFilterName(FILTER)
                .setCapacity(200000)
                .setProb(1f / 10000f)
                .build();
        assertThat(sync(client.create(createArgs))).isEqualTo(CreateResult.DONE);
        Thread.sleep(1000);

        // build a list of keys to save into the filter
        List<String> keys = new ArrayList<>();
        List<Callable<StateResult>> tasks = new ArrayList<>();

        for (int i = 0; i < 100000; i++) {
            String key = UUID.randomUUID().toString();

            tasks.add(() -> client.set(FILTER, key).get(3, TimeUnit.SECONDS));
            keys.add(key);
        }

        // save keys in parallel
        ExecutorService executor = Executors.newFixedThreadPool(100);
        List<Future<StateResult>> results = executor.invokeAll(tasks);

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        results.forEach(result -> {
            try {
                StateResult stateResult = result.get(3, TimeUnit.SECONDS);
                assertThat(stateResult).isIn(Arrays.asList(StateResult.YES, StateResult.NO));
            } catch (Throwable e) {
                e.printStackTrace();
                fail("Failed to get result");
            }
        });

        // check that keys are there
        executor = Executors.newFixedThreadPool(100);

        List<Callable<Object>> getTasks = keys
                .stream()
                .map(key -> (Callable<Object>) (() -> {
                    Object stateResult = client.check(FILTER, key).get(20, TimeUnit.SECONDS);
                    assertThat(stateResult).isInstanceOf(StateResult.class);
                    return stateResult;
                }))
                .collect(Collectors.toList());

        // mix the "get tasks" with random info commands
        for (int i = 0; i < 100; i++) {
            Callable<Object> objectCallable = () -> {
                Object bloomdInfo = client.info(FILTER).get(20, TimeUnit.SECONDS);
                assertThat(bloomdInfo).isInstanceOf(BloomdInfo.class);
                return bloomdInfo;
            };
            getTasks.add(objectCallable);
        }
        Collections.shuffle(getTasks);

        // execute gets in parallel
        List<Future<Object>> resultChecks = executor.invokeAll(getTasks);
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        // all keys must be found in the filter
        resultChecks.forEach(result -> {
            try {
                Object obj = result.get(10, TimeUnit.SECONDS);
                if (obj instanceof StateResult) {
                    StateResult stateResult = (StateResult) obj;
                    assertThat(stateResult).isEqualTo(StateResult.YES);
                } else {
                    BloomdInfo info = (BloomdInfo) obj;
                    assertThat(info.getSize()).isEqualTo(100000);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                fail("Failed to get result");
            }
        });
    }
}
