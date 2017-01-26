package bloomd;

import bloomd.replies.BloomdFilter;
import org.junit.Test;

import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static bloomd.DockerHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ErrorSurfacingTest {
    @Test
    public void cantConnectTest() throws TimeoutException, InterruptedException {
        try {
            BloomdClient
                    .newInstance("localhost", 2222)
                    .get(2, TimeUnit.SECONDS);
            fail("Should have failed to connect");
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ConnectException.class);
        }
    }

    @Test
    public void disconnectionTest() throws Exception {
        // start bloomd service on a random port
        int port = randomPort();
        String containerId = startBloomdInDocker(port);
        assertThat(containerId).isNotNull();

        Thread.sleep(3000);

        // create a client that connects to the service
        BloomdClient client = BloomdClient.newInstance("localhost", port).get(1, TimeUnit.SECONDS);

        // make sure it can connect
        List<BloomdFilter> list = client.list().get(1, TimeUnit.MINUTES);
        assertThat(list).isEmpty();

        // kill the server
        assertThat(stopBloomdInDocker(containerId)).isEqualTo(containerId);

        try {
            client.list();
            fail("Should have failed to send list command");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("Client is not connected to the server");
        }
    }

}
