package structure;

/**
 * Simple distance holder between two nodes. Value is in meters.
 */
public record Distance(double meters) {


    @Override
    public String toString() {
        return String.format("%.1fm", meters);
    }
}
