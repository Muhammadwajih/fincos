package pt.uc.dei.fincos.validation;
import java.util.TreeMap;

/**
 * Class representing a equi-width histogram.
 * 
 * @author Marcelo R.N. Mendes
 *
 */
public class Histogram {	
	private double min;
	private double max;
	private double binWidth;
	private int bucketCount;
	private long histogram[];
	private boolean cumulative;		
	private long processedCount;
	
	/**
	 * 
	 * @param binWidth		Size of buckets
	 * @param min			Min value of the histogram (smaller values will be part of the first bucket)
	 * @param max			Max value of the histogram (greater values will be part of the last bucket)
	 * @param cumulative	Indicates if frequencies of the histogram are cumulative or not
	 */
	public Histogram(double binWidth, double min, double max, boolean cumulative) {	
		this.binWidth = binWidth;
		this.bucketCount = (int) Math.round((max-min)/binWidth)+2;
		this.min = min;
		this.max = max;
		this.cumulative = cumulative;	
		this.clear();
	}
	
	/**
	 * 
	 * @param bucketCount	Number of bins
	 * @param min			Min value of the histogram (smaller values will be part of the first bucket)
	 * @param max			Max value of the histogram (greater values will be part of the last bucket)
	 * @param cumulative	Indicates if frequencies of the histogram are cumulative or not
	 */
	public Histogram(int bucketCount, double min, double max, boolean cumulative) {				
		this.bucketCount = bucketCount;
		this.binWidth = (max-min)/bucketCount;
		this.min = min;
		this.max = max;
		this.cumulative = cumulative;
		this.clear();
	}
	
	/**
	 * Resets the histogram
	 */
	private void clear() {
		histogram = new long[bucketCount];
		processedCount = 0;
	}

	/**
	 * Places the item passed as argument in the appropriate bin in the histogram
	 * 
	 * @param item
	 */
	public void addItem(double item) {				
		int index;
		index = (int) Math.ceil((item-min)/binWidth)+1;			
		if(index < 0) // Add items that fall beyond the specified min to the first bucket
			index = 0;
		if(index >= histogram.length) // Add items that fall beyond the specified max to the last bucket
			index = histogram.length-1;
		
		histogram[index]++;
		
		processedCount++;
	}
	
	/**
	 * Builds a histogram represented as an ordered Map structure. 
	 * Keys are the upper boundary of the bins and the values are
	 * the corresponding frequencies.
	 * 
	 * @return
	 */
	public TreeMap<Double, Double> getHistogram() {		
		TreeMap<Double, Double> ret = new TreeMap<Double, Double>();		
				
		for (int i = 0; i < histogram.length; i++) {	
			if(!cumulative || i== 0) {
				if(i<histogram.length-1)
					ret.put(min+(i)*binWidth, 1.0*histogram[i]/processedCount);
				else
					ret.put(Double.POSITIVE_INFINITY, 1.0*histogram[i]/processedCount);
			}
			else {	
				if(i<histogram.length-1)
					ret.put(min+(i)*binWidth, ret.get(min+(i-1)*binWidth)+1.0*histogram[i]/processedCount);
				else
					ret.put(Double.POSITIVE_INFINITY, ret.get(min+(i-1)*binWidth)+1.0*histogram[i]/processedCount);
			}
		}	

		return ret;
	}

	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}
	
	public double getBinWidth() {
		return binWidth;
	}
	
	public int getBucketCount() {
		return bucketCount;
	}
	
}
