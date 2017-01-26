package bloomd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import bloomd.replies.BloomdInfo;
import bloomd.replies.StateResult;

public class InvalidOperationsTest extends DockerBasedTest {

    private BloomdClient client;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // get bloomd client implementation
        client = BloomdClient
                .newInstance("localhost", port)
                .get(1, TimeUnit.SECONDS);
    }

    @Test
    public void infoFilterThatDoesNotExists() throws Exception {
        Future<BloomdInfo> info = client.info("nonexistent_filter");
        try {
            info.get(5, TimeUnit.SECONDS);
            fail("It shouldn't make it to this point");
        } catch (Throwable e) {
            assertThat(e).isInstanceOf(ExecutionException.class);
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
        }
    }

    @Test(expected = ExecutionException.class)
    public void checkFilterThatDoesNotExists() throws Exception {
        Future<List<StateResult>> multi = client.multi("nonexistent_filter", "whatever");
        multi.get(5, TimeUnit.SECONDS);
    }
}
