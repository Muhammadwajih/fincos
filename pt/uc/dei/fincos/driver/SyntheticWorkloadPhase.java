/* FINCoS Framework
 * Copyright (C) 2013 CISUC, University of Coimbra
 *
 * Licensed under the terms of The GNU General Public License, Version 2.
 * A copy of the License has been included with this distribution in the
 * fincos-license.txt file.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 */


package pt.uc.dei.fincos.driver;

import java.util.LinkedHashMap;

import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.driver.Scheduler.ArrivalProcess;

/**
 * Basic class representing a synthetic workload phase.
 *
 * @author  Marcelo R.N. Mendes
 *
 * @see     ExternalFileWorkloadPhase
 */
public final class SyntheticWorkloadPhase extends WorkloadPhase {

    /** serial id. */
    private static final long serialVersionUID = -2381610231886845725L;

    /** The phase duration, in seconds. */
	private final int duration;

	/** The initial event submission rate of this phase, in events per second. */
	private final double initialRate;

	/** The final event submission rate of this phase, in events per second. */
	private final double finalRate;

	/** The arrival process of this phase (either POISSION or DETERMINISTIC). */
	private ArrivalProcess arrivalProcess;

	/** The event types defined for this phase and their corresponding mixes. */
	private LinkedHashMap<EventType, Double> schema;

	/** Indicates if event types are generated in a predictable order. */
	private boolean deterministicEventMix;

	/** Indicates when data is generated.*/
	private int dataGenMode;

	/** Data is generated in runtime, during load submission. */
	public static final int RUNTIME = 0;

	/** Data is generated before load submission starts. */
	public static final int DATASET = 1;

	/** Random-number generation seed (used for repeatability).*/
	private Long randomSeed;


	/**
	 *
	 * @param duration                 The duration of the phase, in seconds
	 * @param initialRate			   The initial event submission rate, in events/second
	 * @param finalRate				   The final event submission rate, in events/second
	 * @param arrivalProcess 		   Either DETERMINISTIC or POISSON
	 * @param schema				   The event types defined for this phase and
	 *                                 their corresponding mixes
	 * @param deterministicEventMix    Indicates if event types are generated
	 *                                 in a predictable order
	 * @param dataGenMode			   Determines if events' data must be generated during
	 *                                 test (RUNTIME) or before test starts (DATASET)
	 * @param randomSeed			   Seed for random number generation
	 */
	public SyntheticWorkloadPhase(int duration, double initialRate, double finalRate,
								  ArrivalProcess arrivalProcess,
								  LinkedHashMap<EventType, Double> schema,
								  boolean deterministicEventMix,
								  int dataGenMode, Long randomSeed) {
		this.duration = duration;
		if (initialRate > 0) {
            this.initialRate = initialRate;
        } else {
            if (initialRate == 0) {
                this.initialRate = 1E-4; // 0.1 events/second
            } else {
                throw new IllegalArgumentException("Invalid event rate ("
                                                 + initialRate + ").");
            }
        }

		if (finalRate > 0) {
            this.finalRate = finalRate;
        } else {
            if (finalRate == 0) {
                this.finalRate = 1E-4; //0.1 events/second
            } else {
                throw new IllegalArgumentException("Invalid event rate ("
                                                  + finalRate + ").");
            }
        }

		this.arrivalProcess = arrivalProcess;
		this.schema = schema;
		this.deterministicEventMix = deterministicEventMix;
		this.dataGenMode = dataGenMode;
		this.randomSeed = randomSeed;
	}

	/**
	 *
	 * @return the phase duration, in seconds
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 *
	 * @return the initial event submission rate of this phase, in events per second
	 */
	public double getInitialRate() {
		return initialRate;
	}

	/**
	 *
	 * @return the final event submission rate of this phase, in events per second
	 */
	public double getFinalRate() {
		return finalRate;
	}

	/**
	 *
	 * @return the event types defined for this phase and their corresponding mixes
	 */
	public LinkedHashMap<EventType, Double> getSchema() {
		return schema;
	}

	/**
	 *
	 * @return the arrival process of this phase (either POISSION or DETERMINISTIC)
	 */
	public ArrivalProcess getArrivalProcess() {
		return arrivalProcess;
	}

	/**
	 * Indicates when data is generated.
	 *
	 * @return Either 0 (During load submission)
	 *         or 1 (Before test starts)
	 */
	public int getDataGenMode() {
		return dataGenMode;
	}

	/**
	 *
	 * @return the random-number generation seed
	 */
	public Long getRandomSeed() {
		return randomSeed;
	}

	/**
	 *
	 * @return the total number of events of this phase
	 */
	public long getTotalEventCount() {
		double avgRate = (initialRate + finalRate) / 2;
		return Math.round(duration * avgRate);
	}

	/**
	 * Indicates if the event types are generated in a predictable order.
	 *
	 * @return <tt>true</tt> if the event mix is deterministic,
	 *         <tt>false</tt> otherwise
	 */
	public boolean isDeterministicEventMix() {
		return deterministicEventMix;
	}

}
