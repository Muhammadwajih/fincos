package pt.uc.dei.fincos.driver;

import java.util.Random;

/**
 * Schedules events creation and submission on synthetic workloads.
 */
public class Scheduler {

    /** Distribution of event's inter-arrival time. */
    public enum ArrivalProcess {
        /** constant inter-arrival time (1/rate). */
        DETERMINISTIC,
        /** exponentially distributed inter-arrival time. */
        POISSON
    };

    /** The initial event submission rate (in events/nanosec). */
    private double initialRate;

    /** The final event submission rate (in events/nanosec). */
    private double finalRate;

    /** The arrival process of events (either deterministic or Poisson process). */
    private ArrivalProcess arrivalProcess;

    /** Total test duration, in nanoseconds. */
    private long testDuration;

    /** The "current time" in the scheduled simulation (in nanoseconds). */
    private long currentTime = 0;

    /** A multiplier factor, used to control event submission speed. */
    private double rateFactor = 1.0;

    /** Random number generator. */
    private final Random rnd;

    /**
     *
     * @param initialEventRate  The initial event submission rate (events/sec)
     * @param finalEventRate    The final event submission rate (events/sec)
     * @param testDuration      Total test duration, in seconds
     * @param arrivalProcess    Either DETERMINISTIC or POISSON
     * @param seed              Seed for random number generation in case of Poisson Process
     * @throws Exception
     */
    public Scheduler(double initialEventRate, double finalEventRate, long testDuration,
            ArrivalProcess arrivalProcess, Long seed) {
        this.setInitialRate(initialEventRate / 1E9);        // converts to events/nanoseconds
        this.setFinalRate(finalEventRate / 1E9);            // converts to events/nanoseconds
        this.arrivalProcess = arrivalProcess;
        this.setTestDuration((long) (testDuration * 1E9));  // converts to nanoseconds
        if (seed != null) {
            this.rnd = new Random(seed);
        } else {
            this.rnd = new Random();
        }
    }

    /**
     *
     * @param eventRate         The event submission rate (in events/sec)
     * @param testDuration      Total test duration, in seconds
     * @param arrivalProcess    Either DETERMINISTIC or POISSON
     * @param seed              Seed for random number generation in case of Poisson Process
     * @throws Exception
     */
    public Scheduler(double eventRate, int testDuration, ArrivalProcess arrivalProcess, Long seed) {
        this(eventRate, eventRate, testDuration, arrivalProcess, seed);
    }

    /**
     * Sets the initial event submission rate to the specified value,
     * or to 0.1 events/sec if the value is non-positive.
     *
     * @param rate          the new initial event submission rate
     */
    private void setInitialRate(double rate) {
        if (rate > 0) {
            this.initialRate = rate;
        } else {
            this.initialRate = 1E-10; // 0.1 events/second
        }

    }

    /**
     * Sets the final event submission rate to the specified value,
     * or to 0.1 events/sec if the value is non-positive.
     *
     * @param rate          the new final event submission rate
     */
    private void setFinalRate(double finalRate)  {
        if (finalRate > 0) {
            this.finalRate = finalRate;
        } else {
            this.finalRate = 1E-10; //0.1 events/second
        }
    }

    /**
     * Sets the test duration to the specified value,
     * or to 1 nanosecond if the value is non-positive.
     *
     * @param rate          the new final event submission rate
     */
    private void setTestDuration(long testDuration) {
        if (testDuration > 0) {
            this.testDuration = testDuration;
        } else {
            this.testDuration = 1;
        }
    }

    /**
     * Gets the inter-arrival times of events
     * If the arrival process is DETERMINISTIC, T = 1/X.
     * If the arrival process is POISSON, T = -ln(U)/X.
     *
     * @return  The time in nanoseconds
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
     * Gets the inter-arrival times of events
     * (deterministic: T = 1/X).
     *
     * @return  The time in nanoseconds
     */
    private long getDeterministicInterArrivalTime() {
        long interTime = Math.round(1 / this.getEventRate(this.currentTime));
        this.currentTime = this.currentTime + interTime;
        return interTime;
    }

    /**
     * Gets the inter-arrival times of events
     * (Poisson Process: inter-arrival times exponentially distributed).
     *
     * @return The time in nanoseconds
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
     * Computes current event rate (for X0 != Xf, variation is linear).
     *
     * @param t     The current time, in nanoseconds
     * @return      Current event rate, considering current time and the initial and final rates
     */
    private double getEventRate(long t) {
        return this.rateFactor * (this.initialRate + t * (this.finalRate - this.initialRate) / this.testDuration);
    }

    /**
     * Sets the factor by which the event rate is multiplied.
     *
     * @param factor    the new value for the multiplier factor
     */
    public synchronized void setRateFactor(double factor) {
        this.rateFactor = factor;
    }

    /**
     * Gets the factor by which the event rate is multiplied.
     *
     * @return  the multiplier factor
     */
    public double getRateFactor() {
        return rateFactor;
    }
}
