package bloomd;

import org.junit.Test;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static bloomd.DockerHelper.startBloomdInDocker;
import static bloomd.DockerHelper.stopBloomdInDocker;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.fail;

public class TestPoolingDisconnection extends DockerBasedTest {

    @Test
    public void testClientsPool() throws Exception {
        BloomdClientPool bloomdClientPool = new BloomdClientPool("localhost", port, 5, 2000, 10000);

        List<BloomdClient> clients = new ArrayList<>();

        // should be able to acquire 20 clients
        for (int i = 0; i < 5; i++) {
            Future<BloomdClient> acquire = bloomdClientPool.acquire();
            BloomdClient bloomdClient = acquire.get(1, TimeUnit.SECONDS);
            clients.add(bloomdClient);
        }

        // stop bloomd server
        assertThat(stopBloomdInDocker(containerId))
                .isEqualTo(containerId);

        try {
            clients.get(0).list();
            fail("Should have failed to list");
        } catch (IllegalStateException ignore) {
        }

        // release all clients
        clients.forEach(bloomdClientPool::release);

        // acquire another one
        try {
            bloomdClientPool.acquire().get(10, TimeUnit.SECONDS);
            fail("Should have failed to acquire client");
        } catch (Throwable e) {
            assertThat(e).isInstanceOf(ExecutionException.class);
            assertThat(e.getCause()).isInstanceOf(ConnectException.class);
        }

        // start server again
        assertThat(containerId = startBloomdInDocker(port)).isNotNull();
        Thread.sleep(3000);


        // once the server has started again we should be able to acquire things from the queue
        Future<BloomdClient> acquire = bloomdClientPool.acquire();
        BloomdClient client = acquire.get(10, TimeUnit.SECONDS);
        client.list().get(5, TimeUnit.SECONDS);
    }
}
