package bloomd;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import bloomd.args.CreateFilterArgs;
import bloomd.replies.BloomdFilter;
import bloomd.replies.BloomdInfo;
import bloomd.replies.ClearResult;
import bloomd.replies.CreateResult;
import bloomd.replies.StateResult;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class BloomdClientTest extends DockerBasedTest {

    public static final String FILTER = "testFilter" + System.currentTimeMillis();

    @Test
    public void testOperations() throws Exception {
        // get bloomd client implementation
        BloomdClient client = BloomdClient
                .newInstance("localhost", port)
                .get(1, TimeUnit.SECONDS);

        // make sure filters can be created
        assertThat(sync(client.create(FILTER))).isEqualTo(CreateResult.DONE);


        // it should be present by listing it
        List<BloomdFilter> filters = sync(client.list(FILTER));
        assertThat(filters).hasSize(1);
        assertThat(filters.get(0).getFilterName()).isEqualTo(FILTER);


        // we should be able to set items
        assertThat(sync(client.set(FILTER, "nishtiman"))).isEqualTo(StateResult.YES);
        assertThat(sync(client.set(FILTER, "nishtiman"))).isEqualTo(StateResult.NO); // repeated


        // the item we just added should be there when using check
        assertThat(sync(client.check(FILTER, "nishtiman"))).isEqualTo(StateResult.YES);
        assertThat(sync(client.check(FILTER, "non-extant"))).isEqualTo(StateResult.NO);


        // close and clear this filter
        assertThat(sync(client.close(FILTER))).isEqualTo(true);
        assertThat(sync(client.clear(FILTER))).isEqualTo(ClearResult.CLEARED);
        assertThat(sync(client.clear("non-extant-filter"))).isEqualTo(ClearResult.FILTER_DOES_NOT_EXISTS);

        // create the filter again
        CreateFilterArgs createArgs = new CreateFilterArgs.Builder()
                .setFilterName(FILTER)
                .setCapacity(100000)
                .setProb(1f / 10000f)
                .setInMemory(true)
                .build();
        CreateResult createResult = sync(client.create(createArgs));
        if (createResult == CreateResult.DELETE_IN_PROGRESS) {
            Thread.sleep(5000);
            assertThat(sync(client.create(createArgs))).isEqualTo(CreateResult.DONE);
        } else {
            assertThat(createResult).isEqualTo(CreateResult.DONE);
        }


        // it should be possible to add items in bulk
        List<StateResult> bulkResult = sync(client.bulk(FILTER, "fus", "dah"));
        assertThat(bulkResult).hasSize(2);
        assertThat(bulkResult).isEqualTo(Arrays.asList(StateResult.YES, StateResult.YES));


        // adding items that already exists should return "NO"
        List<StateResult> secondBulkResult = sync(client.bulk(FILTER, "fus", "ro", "dah"));
        assertThat(secondBulkResult).hasSize(3);
        assertThat(secondBulkResult).isEqualTo(Arrays.asList(StateResult.NO, StateResult.YES, StateResult.NO));


        // we should be able to check the state of multiple keys
        List<StateResult> multiCheck = sync(client.multi(FILTER, "fus", "ro", "dah", "non-extant-key"));
        assertThat(multiCheck).isEqualTo(Arrays.asList(StateResult.YES, StateResult.YES, StateResult.YES, StateResult.NO));


        // we should be able to inspect the filter stats
        BloomdInfo info = sync(client.info(FILTER));
        assertThat(info.getCapacity()).isEqualTo(100000);
        assertThat(info.getProbability()).isEqualTo(1f / 10000f);
        assertThat(info.getSize()).isEqualTo(4);
        assertThat(info.getStorage()).isEqualTo(300046);

        assertThat(info.getChecks()).isEqualTo(4);
        assertThat(info.getCheckHits()).isEqualTo(3);
        assertThat(info.getCheckMisses()).isEqualTo(1);

        assertThat(info.getSets()).isEqualTo(5);
        assertThat(info.getSetHits()).isEqualTo(3);
        assertThat(info.getSetMisses()).isEqualTo(2);

        assertThat(info.isInMemory()).isTrue();


        // we should be able to drop this filter
        assertThat(sync(client.drop(FILTER))).isTrue();


        // the filter should not be found after this
        assertThat(sync(client.list(FILTER))).isEmpty();
    }
}
