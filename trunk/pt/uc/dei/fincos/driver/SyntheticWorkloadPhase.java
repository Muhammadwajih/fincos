package pt.uc.dei.fincos.driver;

import java.util.LinkedHashMap;

import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.driver.Scheduler.ArrivalProcess;


public class SyntheticWorkloadPhase extends WorkloadPhase {	
	private static final long serialVersionUID = -2381610231886845725L;	
	
	private int duration;
	private double initialRate;
	private double finalRate;
	private ArrivalProcess arrivalProcess;
	
	private LinkedHashMap<EventType, Double> schema;
	private boolean deterministicEventMix;
	
	private int dataGenMode;
	public static final int RUNTIME = 0;
	public static final int DATASET = 1;
	private Long randomSeed;
	
	
	/**
	 * 
	 * @param duration				The duration of the phase, in seconds 
	 * @param initialRate			The initial event submission rate, in events/second 
	 * @param finalRate				The final event submission rate, in events/second
	 * @param arrivalProcess 		Either DETERMINISTIC or POISSON
	 * @param schema				The mix of event types to be generated during this phase
	 * @param deterministicEventMix	Indicates if event types must be generated in a predictable/repeatable way
	 * @param dataGenMode			Determines if events' data must be generated during test
	 * 								(RUNTIME) or before test starts (DATASET) 
	 * @param randomSeed			Seed for random number generation
	 */
	public SyntheticWorkloadPhase(int duration, double initialRate, double finalRate, 
								  ArrivalProcess arrivalProcess,
								  LinkedHashMap<EventType, Double> schema,
								  boolean deterministicEventMix,
								  int dataGenMode, Long randomSeed) {
		setDuration(duration);
		setInitialRate(initialRate);
		setFinalRate(finalRate);	
		setArrivalProcess(arrivalProcess);
		this.schema = schema;
		setDeterministicEventMix(deterministicEventMix);
		setDataGenMode(dataGenMode);
		setRandomSeed(randomSeed);
	}
	
	public int getDuration() {
		return duration;
	}
	
	public double getInitialRate() {
		return initialRate;
	}
	
	public double getFinalRate() {
		return finalRate;
	}
	
	public LinkedHashMap<EventType, Double> getSchema() {
		return schema;
	}
	
	private void setInitialRate(double rate) {
		if(rate > 0) 
			this.initialRate = rate;
		else {
			if (rate == 0)
				this.initialRate = 1E-4; // 0.1 events/second
			else
				System.err.println("Invalid event rate.");
		}
	}
	
	private void setFinalRate(double finalRate) {
		if(finalRate > 0) 
			this.finalRate = finalRate;
		else {
			if (finalRate == 0)
				this.finalRate = 1E-4; //0.1 events/second
			else
				System.err.println("Invalid event rate.");
		}
		
	}
		
	public void setArrivalProcess(ArrivalProcess arrivalProcess) {
		this.arrivalProcess = arrivalProcess;
	}

	public ArrivalProcess getArrivalProcess() {
		return arrivalProcess;
	}

	private void setDuration(int duration) {
		this.duration = duration;
	}

	public void setDataGenMode(int dataGenMode) {
		this.dataGenMode = dataGenMode;
	}

	public int getDataGenMode() {
		return dataGenMode;
	}

	public void setRandomSeed(Long randomSeed) {
		this.randomSeed = randomSeed;
	}

	public Long getRandomSeed() {
		return randomSeed;
	}
	
	public long getTotalEventCount() {
		double avgRate = (initialRate+finalRate)/2;		
		return Math.round(duration * avgRate);
	}

	public void setDeterministicEventMix(boolean deterministicEventMix) {
		this.deterministicEventMix = deterministicEventMix;
	}

	public boolean isDeterministicEventMix() {
		return deterministicEventMix;
	}

}
