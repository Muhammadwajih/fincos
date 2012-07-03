package pt.uc.dei.fincos.perfmon;

import java.util.LinkedList;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.perfmon.gui.PerformanceMonitor.WindowEvaluationModel;



/**
 * Class used to estimate throughput of a given stream
 * over a specified time window (default 1 second).
 *
 * @author Marcelo R. N. Mendes
 *
 */
public class ThroughputEstimator {
	private LinkedList<Long> eventsTimestampsBuffer;
	private int windowSizeInMillis;
	private int modFactor;
	private WindowEvaluationModel throughputWindowModel;
	double rtFactor = Double.NaN;


	/**
	 *
	 * @param eventsTimestampsBuffer	Buffer for storing timestamps of last events
	 * @param windowSizeInSeconds		Time window size (in seconds). Timestamps of events will be kept during this time.
	 * @param samplingRate              The sampling rate used by the sink(s) when sending events to Perfmon
	 * @param throughputWindowModel		Either time-based or tuple-based
	 * @param rtMeasurementMode			Either END_TO_END_RT_MILLIS, ADAPTER_RT_NANOS, or NO_RT
	 */
	public ThroughputEstimator(LinkedList<Long> eventsTimestampsBuffer,
			int windowSizeInSeconds, double samplingRate,
			WindowEvaluationModel throughputWindowModel,
			int rtMeasurementMode) {
		this.eventsTimestampsBuffer = eventsTimestampsBuffer;
		this.windowSizeInMillis = windowSizeInSeconds * 1000;
		this.modFactor = (int) (1 / samplingRate);
		this.throughputWindowModel = throughputWindowModel;

		if (rtMeasurementMode == Globals.ADAPTER_RT) {
			rtFactor = 1E6;
		} else if (rtMeasurementMode == Globals.END_TO_END_RT
		        || rtMeasurementMode == Globals.NO_RT) {
			rtFactor = 1.0;
		}
	}

	/**
	 * Computes throughput of the associated stream.
	 *
	 * @return		The throughput in events/second
	 */
	public double computeCurrentThroughput() {
		double throughput = 0;
		synchronized (eventsTimestampsBuffer) {
			Double now;
			if (throughputWindowModel == WindowEvaluationModel.TIME_BASED) {
				now = 1.0 * System.currentTimeMillis();
			} else {
				if (eventsTimestampsBuffer != null && !eventsTimestampsBuffer.isEmpty()) {
					now = eventsTimestampsBuffer.peekLast() / rtFactor;
				} else {
					return 0.0;
				}
			}

			// Removes stale events
			for (int i = 0; i < eventsTimestampsBuffer.size() && now - eventsTimestampsBuffer.get(i) / rtFactor > windowSizeInMillis;) {
			    eventsTimestampsBuffer.remove(i);
			}

			throughput = modFactor * eventsTimestampsBuffer.size() * 1000.0 / windowSizeInMillis;
		}

		return throughput;
	}

	public void addTimestamp(long timestamp) {
		synchronized (eventsTimestampsBuffer) {
			eventsTimestampsBuffer.offer(timestamp);
		}

	}
}

