package pt.uc.dei.fincos.driver;

import java.util.Random;

public class Scheduler {
	public enum ArrivalProcess{DETERMINISTIC, POISSON};
	
	private double initialRate;	// events/nanosecond
	private double finalRate;	// events/nanosecond
	private ArrivalProcess arrivalProcess;
	private long testDuration;     // nanoseconds
	private long currentTime = 0;  // nanoseconds
	private double rateFactor = 1.0; // used to control event submission speed
	
	private Random rnd;		
	 
	 /**
	  * 
	  * @param initialEventRate	The initial rate of submission of events (events/sec)
	  * @param finalEventRate	The final rate of submission of events (events/sec)
	  * @param testDuration		Total test duration, in seconds
	  * @param arrivalProcess 	Either DETERMINISTIC or POISSON
	  * @param seed				Seed for random number generation in case of Poisson Process
	  * @throws Exception
	  */
	 public Scheduler(double initialEventRate, double finalEventRate, long testDuration, 
			 		  ArrivalProcess arrivalProcess, Long seed) {
		 this.setInitialRate(initialEventRate/1E9); 		// converts to events/nanoseconds
		 this.setFinalRate(finalEventRate/1E9);			// converts to events/nanoseconds
		 this.arrivalProcess = arrivalProcess;
		 this.setTestDuration((long)(testDuration*1E9));	// converts to nanoseconds
		 if(seed != null)
			 this.rnd = new Random(seed);
		 else
			 this.rnd = new Random();
	}
	 
	 /**
	  * 
	  * @param eventRate		The rate of submission of events (events/sec)
	  * @param testDuration		Total test duration, in seconds
	  * @param arrivalProcess 	Either DETERMINISTIC or POISSON
	  * @param seed				Seed for random number generation in case of Poisson Process
	  * @throws Exception	
	  */
	 public Scheduler(double eventRate, int testDuration, ArrivalProcess arrivalProcess, Long seed) {
		 this(eventRate, eventRate, testDuration, arrivalProcess, seed);			 
	 }

	private void setInitialRate(double rate) {
		if(rate > 0) 
			this.initialRate = rate;
		else 
			this.initialRate = 1E-10; // 0.1 events/second
	}
	
	private void setFinalRate(double finalRate)  {
		if(finalRate > 0) 
			this.finalRate = finalRate;
		else 
			this.finalRate = 1E-10; //0.1 events/second
	}
	

	private void setTestDuration(long testDuration) {
		if(testDuration > 0)	
			this.testDuration = testDuration;
		else
			this.testDuration = 1;
	}

	/**
	 * Gets interarrival times of events 
	 * If the arrival process is DETERMINISTIC, T = 1/X.
	 * If the arrival process is POISSON, T = -ln(U)/X.
	 * @return	The time in nanoseconds
	 */
	public long getInterArrivalTime() {		
		switch (this.arrivalProcess) {
		case DETERMINISTIC:
			return this.getDeterministicInterArrivalTime(); 
		case POISSON:
			return this.getPoissonInterArrivalTime();
		default:
			return this.getDeterministicInterArrivalTime();
		}
	}
	
	/**
	 * Gets interarrival times of events 
	 * (deterministic: T = 1/X)
	 * 
	 * @return	The time in nanoseconds
	 */
	private long getDeterministicInterArrivalTime() {	
		long interTime = Math.round(1/this.getEventRate(this.currentTime));	
		this.currentTime = this.currentTime + interTime;
		return interTime;	
	}
	
	/**
	 * Gets interarrival times of events.
	 * (Poisson Process: interarrival times exponentially distributed)
	 * 
	 * @return	The time in nanoseconds
	 */
	private long getPoissonInterArrivalTime() {
		long interTime = Math.round(
									-Math.log(rnd.nextDouble())
											/
									this.getEventRate(this.currentTime)
									);	
		this.currentTime = this.currentTime + interTime;
		return interTime;	
	}	
	
	/**
	 * Computes current event rate (for X0 != Xf, variation is linear)
	 * 
	 * @param t		The current time, in nanoseconds
	 * @return		Current event rate, considering current time and the initial and final rates 
	 */
	private double getEventRate(long t) {
		return this.rateFactor*(this.initialRate + t*(this.finalRate-this.initialRate)
								/this.testDuration);
	}
	
	/**
	 * Sets the factor by which the event rate is multiplied
	 * 
	 * @param factor
	 */
	public synchronized void setRateFactor(double factor) {
		this.rateFactor = factor;
	}
	
	/**
	 * Gets the factor by which the event rate is multiplied
	 * 
	 */
	public double getRateFactor() {
		return rateFactor;
	}
}
