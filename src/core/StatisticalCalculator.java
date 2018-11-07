package core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StatisticalCalculator {
	
	private final Collection<BigDecimal> datas;
	private final BigDecimal dataSize;
	
	int precision;
	
	public StatisticalCalculator(final Collection<BigDecimal> datas,
			final int precision) {
		this.datas = datas;
		this.dataSize = new BigDecimal(datas.size());
		this.precision = precision;
	}
	
	public BigDecimal getMean() {
		BigDecimal temp = BigDecimal.ZERO;
		for (final BigDecimal data : this.datas) {
			temp = temp.add(data);
		}
		BigDecimal mean;
		try {
			mean = temp.divide(this.dataSize);
		} catch (final ArithmeticException ex) {
			temp.setScale(this.precision, BigDecimal.ROUND_HALF_UP);
			mean = new BigDecimal(temp.doubleValue()
					/ this.dataSize.doubleValue());
		}
		return mean;
	}
	
	public double getDoubleMean() {
		return this.getMean()
				.setScale(this.precision, BigDecimal.ROUND_HALF_UP)
				.doubleValue();
	}
	
	public BigDecimal getVariance() {
		final BigDecimal mean = this.getMean();
		BigDecimal temp = BigDecimal.ZERO;
		for (final BigDecimal data : this.datas) {
			temp = temp
					.add((mean.subtract(data)).multiply(mean.subtract(data)));
		}
		BigDecimal variance;
		try {
			variance = temp.divide(this.dataSize);
		} catch (final ArithmeticException ex) {
			temp.setScale(this.precision, BigDecimal.ROUND_HALF_UP);
			variance = new BigDecimal(temp.doubleValue()
					/ this.dataSize.doubleValue());
		}
		return variance;
	}
	
	public double getDoubleVariance() {
		return this.getVariance()
				.setScale(this.precision, BigDecimal.ROUND_HALF_UP)
				.doubleValue();
	}
	
	public double getDoubleStdDev() {
		return Math.sqrt(this.getVariance()
				.setScale(this.precision, BigDecimal.ROUND_HALF_UP)
				.doubleValue());
	}
	
	public BigDecimal getMedian() {
		final List<BigDecimal> sorted = new ArrayList<BigDecimal>();
		sorted.addAll(this.datas);
		Collections.sort(sorted);
		
		final int dataSize = this.dataSize.intValue();
		
		BigDecimal median;
		if ((dataSize % 2) == 0) {
			final BigDecimal val1 = sorted.get((dataSize / 2) - 1);
			final BigDecimal val2 = sorted.get(dataSize / 2);
			final BigDecimal sum = val1.add(val2);
			try {
				final BigDecimal two = new BigDecimal(2);
				median = sum.divide(two);
			} catch (final ArithmeticException ex) {
				sum.setScale(this.precision, BigDecimal.ROUND_HALF_UP);
				median = new BigDecimal(sum.doubleValue() / 2);
			}
		} else {
			median = sorted.get(dataSize / 2);
		}
		
		return median;
	}
	
	public double getDoubleMedian() {
		return this.getMedian()
				.setScale(this.precision, BigDecimal.ROUND_HALF_UP)
				.doubleValue();
	}
}
