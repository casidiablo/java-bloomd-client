package bloomd;

import bloomd.args.CreateFilterArgs;
import bloomd.replies.BloomdInfo;
import bloomd.replies.CreateResult;
import bloomd.replies.StateResult;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.fail;

public class TestPooling extends DockerBasedTest {

    public static final String FILTER = "testFilter" + System.currentTimeMillis();
    public static final Random RAND = new Random();

    @Test
    public void testClientsPool() throws Exception {
        BloomdClientPool bloomdClientPool = new BloomdClientPool("localhost", port, 20, 2000, 10000);

        List<BloomdClient> clients = new ArrayList<>();

        // should be able to acquire 20 clients
        for (int i = 0; i < 20; i++) {
            Future<BloomdClient> acquire = bloomdClientPool.acquire();
            BloomdClient bloomdClient = acquire.get(1, TimeUnit.SECONDS);
            clients.add(bloomdClient);
        }

        // create filter with at least 200000 capacity
        CreateFilterArgs createArgs = new CreateFilterArgs.Builder()
                .setFilterName(FILTER)
                .setCapacity(200000)
                .setProb(1f / 10000f)
                .build();
        assertThat(sync(clients.get(0).create(createArgs))).isEqualTo(CreateResult.DONE);
        Thread.sleep(1000);

        // build a list of keys to save into the filter
        List<Callable<StateResult>> tasks = new ArrayList<>();

        for (int i = 0; i < 100000; i++) {
            String key = UUID.randomUUID().toString();
            BloomdClient randomClient = clients.get(RAND.nextInt(clients.size()));
            tasks.add(() -> randomClient.set(FILTER, key).get(3, TimeUnit.SECONDS));
        }

        // save keys in parallel
        ExecutorService executor = Executors.newFixedThreadPool(100);
        List<Future<StateResult>> results = executor.invokeAll(tasks);

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        //noinspection Duplicates
        results.forEach(result -> {
            try {
                StateResult stateResult = result.get(3, TimeUnit.SECONDS);
                assertThat(stateResult).isIn(Arrays.asList(StateResult.YES, StateResult.NO));
            } catch (Throwable e) {
                e.printStackTrace();
                fail("Failed to get result");
            }
        });

        // should timeout if trying to wait on another client
        Future<BloomdClient> clientFuture = bloomdClientPool.acquire();
        try {
            clientFuture.get(3, TimeUnit.SECONDS);
            fail("Should have failed");
        } catch (TimeoutException ignored) {
        }

        // should be able to release all clients
        for (BloomdClient client : clients) {
            bloomdClientPool.release(client).get(500, TimeUnit.MILLISECONDS);
        }

        // since the clients were already released the previous acquire should succeed
        BloomdClient bloomdClient = clientFuture.get(1, TimeUnit.SECONDS);

        BloomdInfo bloomdInfo = bloomdClient.info(FILTER).get(1, TimeUnit.SECONDS);
        assertThat(bloomdInfo.getSize()).isNotZero();


        // release client
        bloomdClientPool.release(bloomdClient).get(1, TimeUnit.SECONDS);

        try {
            bloomdClient.list();
            fail("Should have failed because the client was released already");
        } catch (IllegalStateException ignored) {
        }

        bloomdClientPool.closeConnections();
    }
}
