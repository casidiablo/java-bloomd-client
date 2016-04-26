package bloomd.replies;

public class BloomdFilter {
    private final String filterName;
    private final float falsePositiveProbability;
    private final long sizeBytes;
    private final long capacity;
    private final long size;

    public BloomdFilter(String filterName, float falsePositiveProbability, long sizeBytes, long capacity, long size) {
        this.filterName = filterName;
        this.falsePositiveProbability = falsePositiveProbability;
        this.sizeBytes = sizeBytes;
        this.capacity = capacity;
        this.size = size;
    }

    public String getFilterName() {
        return filterName;
    }

    public float getFalsePositiveProbability() {
        return falsePositiveProbability;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BloomdFilter that = (BloomdFilter) o;

        if (Float.compare(that.falsePositiveProbability, falsePositiveProbability) != 0) return false;
        if (sizeBytes != that.sizeBytes) return false;
        if (capacity != that.capacity) return false;
        if (size != that.size) return false;
        if (filterName != null ? !filterName.equals(that.filterName) : that.filterName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = filterName != null ? filterName.hashCode() : 0;
        result = 31 * result + (falsePositiveProbability != +0.0f ? Float.floatToIntBits(falsePositiveProbability) : 0);
        result = 31 * result + (int) (sizeBytes ^ (sizeBytes >>> 32));
        result = 31 * result + (int) (capacity ^ (capacity >>> 32));
        result = 31 * result + (int) (size ^ (size >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "BloomdFilter{" +
                "filterName='" + filterName + '\'' +
                ", falsePositiveProbability=" + falsePositiveProbability +
                ", sizeBytes=" + sizeBytes +
                ", capacity=" + capacity +
                ", size=" + size +
                '}';
    }
}
