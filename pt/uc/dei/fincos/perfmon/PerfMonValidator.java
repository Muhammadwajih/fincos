package pt.uc.dei.fincos.perfmon;

import java.util.LinkedList;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.data.CSVReader;
import pt.uc.dei.fincos.perfmon.gui.PerformanceMonitor.WindowEvaluationModel;


/**
 * A sample Validator. Computes performance metrics in runtime 
 * from streams of events received from Drivers and Sinks. 
 * 
 * @author Marcelo R.N. Mendes
 *
 */
public class PerfMonValidator{

	// Stats
	private long receivedCount=0;
	private long processedCount=0;
	private double elapsedTime=0;
	
	private double avgThroughput=0;
	private double currentThroughput=0;
	private double maxThroughput = 0;
	private double minThroughput = Double.MAX_VALUE;
	
	private double totalRT=0;
	private double maxRT=0;
	private double minRT=Double.MAX_VALUE;
	private double avgRT=0;
	private double currentRT=0;
	private double stdevRT=0;
			
	// Parameters for computing stats
	private int modFactor;		
	private ThroughputEstimator txnEstimator;
	private WindowEvaluationModel throughputWindowModel;
	private int rtMeasurementMode;
	static final int DEFAULT_OUTPUT_WINDOW_SIZE = 1; // In seconds	
	
	// Auxiliary variables
	private long firstOutputEventTS;			
	private double sumSqrRT = 0;
	double rtFactor = Double.NaN;
	
	/**
	 * 
	 * @param samplingRate				The sampling rate used by the Sink responsible for receiving events of this output stream
	 * @param eventsTimestampBuffer		A buffer containing events timestamps
	 * @param throughputWindowModel		Either TUPLE-BASED or TIME-BASED
	 * @param rtMeasurementMode			Either END_TO_END_RT_MILLIS, ADAPTER_RT_NANOS, or NO_RT
	 */
	public PerfMonValidator(double samplingRate, 
							LinkedList<Long> eventsTimestampBuffer,
							WindowEvaluationModel throughputWindowModel,
							int rtMeasurementMode) {
		this.modFactor = (int) (1/samplingRate);
		this.throughputWindowModel  = throughputWindowModel;
		this.txnEstimator = new ThroughputEstimator(eventsTimestampBuffer, DEFAULT_OUTPUT_WINDOW_SIZE, 
													samplingRate, throughputWindowModel, 
													rtMeasurementMode);
		this.rtMeasurementMode = rtMeasurementMode;
				
		if(rtMeasurementMode == Globals.ADAPTER_RT) {			
			rtFactor = 1E6;
		}				
		else if (rtMeasurementMode == Globals.END_TO_END_RT || 
				rtMeasurementMode == Globals.NO_RT
				 ) {			
			rtFactor=1.0;
		}							
	}

	/**
	 * Recomputes performance stats (e.g. average, maximum and minimum throughput and 
	 * response time) for a given output stream upon the arrival of an event 
	 * 
	 * @param outpuEvent	The new event to be included in the performance stats
	 * 
	 */
	public void recomputeStats(String outpuEvent) {
		receivedCount++;			
		
		String [] splitEvent = CSVReader.split(outpuEvent, Globals.CSV_SEPARATOR);
		
		// Timestamp of the output event must be the last field
		long outputEventTimestamp = Long.parseLong(splitEvent[splitEvent.length-1]);
		try {
			if(rtMeasurementMode != Globals.NO_RT) {										
				// Timestamp of event that caused the output event must be the penultimate field
				long causerTimestamp = Long.parseLong(splitEvent[splitEvent.length-2]);

				processedCount++;

				// Computes current response time
				this.setCurrentRT((outputEventTimestamp-causerTimestamp)/rtFactor);

				if(getCurrentRT() >= 0) {
					this.totalRT += this.getCurrentRT();
					this.sumSqrRT += (getCurrentRT()*getCurrentRT());			
					this.setStdevRT(Math.sqrt((sumSqrRT - totalRT * totalRT / processedCount) / (processedCount - 1)));
				}

				else { // Ignores negative response times
					processedCount--;
					setCurrentRT(getMinRT());
					System.err.println("WARNING: Negative RT (" + getCurrentRT() + 
							" ; Causer event's TS: " + causerTimestamp +
							" ; Output event's TS: " +outputEventTimestamp + ")");
				}

				// Computes average, maximum and minimun response time
				this.setAvgRT(this.totalRT/processedCount);
				this.setMaxRT(Math.max(getCurrentRT(), getMaxRT()));
				this.setMinRT(Math.min(getCurrentRT(), getMinRT()));			
			}
			else {
				setCurrentRT(Double.NaN);
				setAvgRT(Double.NaN);
				setMinRT(Double.NaN);
				setMaxRT(Double.NaN);
				setStdevRT(Double.NaN);				
			}
							
			// If window evaluation model is tuple-based, recompute throughput for every new event
			if(throughputWindowModel == WindowEvaluationModel.TUPLE_BASED)
				this.setCurrentThroughput(this.txnEstimator.computeCurrentThroughput());
			
			this.setMaxThroughput(Math.max(getCurrentThroughput(), getMaxThroughput()));
			this.setMinThroughput(Math.min(getCurrentThroughput(), getMinThroughput()));
			
			// Computes Average Throughput
			if(firstOutputEventTS == 0)
				firstOutputEventTS = outputEventTimestamp;
			elapsedTime = (outputEventTimestamp-firstOutputEventTS)/rtFactor;
			if(elapsedTime > 0)
				this.setAvgThroughput(1000*receivedCount*modFactor/elapsedTime);
			else
				this.setAvgThroughput(getCurrentThroughput());
			
		} catch (NumberFormatException e) {
			System.err.println("WARNING: Invalid event format: [" + outpuEvent + "]");
		}		
	}
	
	/**
	 * Allows Time-based evaluation of throughput, by calling this method upon clock ticks 
	 * Tuple-based evaluation can be obtained by computing the throughput upon the arrival
	 * of each event, but it is more susceptible to bursts than time-based one.
	 */
	public void computeCurrentThroughput() {
		this.setCurrentThroughput(this.txnEstimator.computeCurrentThroughput());
	}
	
	public void clear() {	
		System.out.println("clearing stats...");
		receivedCount=0;
		processedCount=0;
		elapsedTime=0;
		totalRT=0;
		setMaxRT(0);
		setMinRT(Double.MAX_VALUE);
		setAvgRT(0);
		setCurrentRT(0);
		setAvgThroughput(0);
		firstOutputEventTS = 0;
		System.out.println("Done!");
	}

	private void setCurrentThroughput(double currentThroughput) {
		this.currentThroughput = currentThroughput;
	}

	public double getCurrentThroughput() {
		return currentThroughput;
	}

	private void setAvgThroughput(double avgThroughput) {
		this.avgThroughput = avgThroughput;
	}

	public double getAvgThroughput() {
		return avgThroughput;
	}

	private void setCurrentRT(double currentRT) {
		this.currentRT = currentRT;
	}

	public double getCurrentRT() {
		return currentRT;
	}

	private void setAvgRT(double avgRT) {
		this.avgRT = avgRT;
	}

	public double getAvgRT() {
		return avgRT;
	}

	private void setMinRT(double minRT) {
		this.minRT = minRT;
	}

	public double getMinRT() {
		return minRT;
	}

	private void setMaxRT(double maxRT) {
		this.maxRT = maxRT;
	}

	public double getMaxRT() {
		return maxRT;
	}

	private void setStdevRT(double stdevRT) {
		this.stdevRT = stdevRT;
	}

	public double getStdevRT() {
		return stdevRT;
	}

	private void setMaxThroughput(double maxThroughput) {
		this.maxThroughput = maxThroughput;
	}

	public double getMaxThroughput() {
		return maxThroughput;
	}

	private void setMinThroughput(double minThroughput) {
		this.minThroughput = minThroughput;
	}

	public double getMinThroughput() {
		return minThroughput;
	}
}

