package core;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * MyPoint is coordinate container able to hold both Integer and Double value of
 * a point x and y coordinate and compute the Integer value of the given Double
 * coordinate using the Math.rint method
 * 
 * @author Alex Rigano
 * 
 * @version 0.0, Stable
 * @since 0.3 of AIG
 */
public class MyPoint {
	Integer x;
	Integer y;
	BigDecimal xD;
	BigDecimal yD;

	/**
	 * Create an empty point, all the coordinate values will be zero
	 * 
	 * @since 1.0
	 */
	public MyPoint() {
		this(0.0, 0.0);
	}

	/**
	 * Create a new point with the given Double coordinate and compute the
	 * Integer coordinate using the {@link #computeIntegerValues()} method
	 * 
	 * @param xD
	 * @param yD
	 * 
	 * @since 1.0
	 */
	public MyPoint(final Double xD, final Double yD) {
		this.xD = BigDecimal.valueOf(xD).setScale(2, RoundingMode.HALF_UP);
		this.yD = BigDecimal.valueOf(yD).setScale(2, RoundingMode.HALF_UP);

		this.computeIntegerValues();
	}

	/**
	 * Compute the integer value of the coordinate using the Math.rint method
	 * 
	 * @since 1.0
	 */
	public void computeIntegerValues() {
		// this.x = ((Double) Math.rint(this.xD)).intValue();
		// this.y = ((Double) Math.rint(this.yD)).intValue();
		this.x = this.xD.setScale(0, RoundingMode.HALF_UP).intValue();
		this.y = this.yD.setScale(0, RoundingMode.HALF_UP).intValue();
	}
}
