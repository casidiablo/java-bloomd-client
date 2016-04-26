package bloomd.args;

public class CreateFilterArgs {
    private final String filterName;
    private final Integer capacity;
    private final Float falsePositiveProbability;
    private final Boolean inMemory;

    public CreateFilterArgs(String filterName, Integer capacity, Float falsePositiveProbability, Boolean inMemory) {
        this.filterName = filterName;
        this.capacity = capacity;
        this.falsePositiveProbability = falsePositiveProbability;
        this.inMemory = inMemory;
    }

    public String getFilterName() {
        return filterName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public Float getFalsePositiveProbability() {
        return falsePositiveProbability;
    }

    public Boolean getInMemory() {
        return inMemory;
    }

    public static class Builder {
        private String filterName;
        private Integer capacity;
        private Float prob;
        private Boolean inMemory;

        public Builder setFilterName(String filterName) {
            this.filterName = filterName;
            return this;
        }

        public Builder setCapacity(Integer capacity) {
            if (capacity != null && capacity < 100000) {
                throw new IllegalArgumentException("Capacity has to be greater than 100000");
            }
            this.capacity = capacity;
            return this;
        }

        public Builder setProb(Float prob) {
            this.prob = prob;
            return this;
        }

        public Builder setInMemory(Boolean inMemory) {
            this.inMemory = inMemory;
            return this;
        }

        public CreateFilterArgs build() {
            return new CreateFilterArgs(filterName, capacity, prob, inMemory);
        }
    }
}
