package pt.uc.dei.fincos.validation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import pt.uc.dei.fincos.basic.Globals;

public class PerformanceStats implements Cloneable, Comparable<PerformanceStats>{
	// Controls the frequency at which stats are stored over time. Default: 1-second
	public static final int DEFAULT_TIME_BUCKET_IN_MILLIS = 1000;

	public String server;
	public Stream stream;
	public long timestamp;

	public int lastEventCount = 0; // Number of events processed since last stats refresh
	public int totalEventCount = 0;
	public double elapsedTime;

	public double avg_throughput;
	public double last_throughput;
	public double max_throughput = 0;
	public double min_throughput = Integer.MAX_VALUE;

	public double lastRT;
	public double avgRT;
	double totalRT = 0;
	public double maxRT = 0;
	public double minRT = Long.MAX_VALUE;
	public double stdevRT;
	double sumSqrRT = 0;

	public DecimalFormat statsFormat;


	public PerformanceStats(String server, Stream stream) {
		this.server = server;
		this.stream = stream;
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		statsFormat = new DecimalFormat("0.000", symbols);
	}

	/**
	 *  Computes periodic stats (average RT, average, last, min, and max throughput)
	 */
	void refreshPeriodicStats() {
		avg_throughput = (1.0E3*totalEventCount)/elapsedTime;
		last_throughput = (1.0E3*lastEventCount)/DEFAULT_TIME_BUCKET_IN_MILLIS;
		min_throughput = Math.min(min_throughput,last_throughput);
		max_throughput = Math.max(max_throughput,last_throughput);
		avgRT = totalRT/totalEventCount;
		stdevRT = Math.sqrt((sumSqrRT - totalRT*totalRT/totalEventCount) / (totalEventCount - 1));
	}


	@Override
	public String toString() {
		return  timestamp+Globals.CSV_SEPARATOR+
				server+Globals.CSV_SEPARATOR+
				stream+Globals.CSV_SEPARATOR+
				statsFormat.format(avg_throughput)+Globals.CSV_SEPARATOR+
				statsFormat.format(min_throughput)+Globals.CSV_SEPARATOR+
				statsFormat.format(max_throughput)+Globals.CSV_SEPARATOR+
				statsFormat.format(last_throughput)+Globals.CSV_SEPARATOR+
				statsFormat.format(avgRT)+Globals.CSV_SEPARATOR+
				statsFormat.format(minRT)+Globals.CSV_SEPARATOR+
				statsFormat.format(maxRT)+Globals.CSV_SEPARATOR+
				statsFormat.format(stdevRT)+Globals.CSV_SEPARATOR+
				statsFormat.format(lastRT);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		PerformanceStats ret = new PerformanceStats(this.server, this.stream);

		ret.lastEventCount = this.lastEventCount;
		ret.totalEventCount = this.totalEventCount;
		ret.elapsedTime = this.elapsedTime;

		ret.avg_throughput = this.avg_throughput;
		ret.last_throughput = this.last_throughput;
		ret.min_throughput = this.min_throughput;
		ret.max_throughput = this.max_throughput;

		ret.avgRT = this.avgRT;
		ret.lastRT = this.lastRT;
		ret.minRT = this.minRT;
		ret.maxRT = this.maxRT;
		ret.totalRT = this.totalRT;
		ret.stdevRT = this.stdevRT;
		ret.sumSqrRT = this.sumSqrRT;

		return ret;
	}

	@Override
	public int compareTo(PerformanceStats o) {
		return (this.timestamp<o.timestamp ? -1 :
			(this.timestamp>o.timestamp ? 1 : stream.name.compareTo(o.stream.name)));
	}

	/**
	 * If two stats refer to the same timestamp, to the same server address and
	 * to the same stream, they are considered the same
	 */
	@Override
	public boolean equals(Object o) {
		PerformanceStats comp;
		if(o instanceof PerformanceStats) {
			comp = (PerformanceStats) o;
			return this.timestamp == comp.timestamp &&
					this.server.equals(comp.server) &&
					this.stream.equals(comp.stream);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int)timestamp+this.server.hashCode()+this.stream.hashCode();
	}
}
