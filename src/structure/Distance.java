package structure;

/**
 * Simple distance holder between two nodes. Value is in meters.
 */
public class Distance {
	private final double meters;

	public Distance(double meters) {
		this.meters = meters;
	}

	public double getMeters() { return meters; }

	@Override
	public String toString() { return String.format("%.1fm", meters); }
}
