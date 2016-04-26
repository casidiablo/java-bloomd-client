package bloomd.args;

import java.util.ArrayList;
import java.util.List;

/**
 * Args used for commands that set/check state of a filter
 */
public class StateArgs {
    private final String filterName;
    private final List<String> keys;

    public StateArgs(String filterName, List<String> keys) {
        this.filterName = filterName;
        this.keys = keys;
    }

    public String getFilterName() {
        return filterName;
    }

    public List<String> getKeys() {
        return keys;
    }

    public static class Builder {
        private String filterName;
        private List<String> keys = new ArrayList<>();

        public Builder setFilterName(String filterName) {
            this.filterName = filterName;
            return this;
        }

        public Builder addAllKeys(List<String> keys) {
            this.keys.addAll(keys);
            return this;
        }

        public Builder addKey(String key) {
            this.keys.add(key);
            return this;
        }

        public StateArgs build() {
            return new StateArgs(filterName, keys);
        }
    }
}
