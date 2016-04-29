package bloomd;

import org.junit.After;
import org.junit.Before;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static bloomd.DockerHelper.randomPort;
import static bloomd.DockerHelper.startBloomdInDocker;
import static bloomd.DockerHelper.stopBloomdInDocker;
import static org.assertj.core.api.Java6Assertions.assertThat;

public abstract class DockerBasedTest {

    protected String containerId;
    protected int port;

    @Before
    public void setUp() throws Exception {
        // start bloomd service on a random port
        port = randomPort();
        containerId = startBloomdInDocker(port);
        assertThat(containerId).isNotNull();

        // allow a few seconds for the container to be up and running
        Thread.sleep(3000);
    }

    @After
    public void tearDown() throws Exception {
        // stop bloomd server
        assertThat(stopBloomdInDocker(containerId))
                .isEqualTo(containerId);
    }

    public static <T> T sync(Future<T> future) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(5, TimeUnit.SECONDS);
    }
}
