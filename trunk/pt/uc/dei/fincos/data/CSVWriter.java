package pt.uc.dei.fincos.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.Globals;


public class CSVWriter {	
	private BufferedWriter writer;
	
	private long lastFlushTimestamp = 0; 
	private int flushInterval;
	
	
	public CSVWriter(String path, int flushInterval) throws IOException {
		this.setFlushInterval(flushInterval);
		File f = new File(path);							
		
		if(!f.getParentFile().exists())
			f.getParentFile().mkdirs();
		
		writer = new BufferedWriter(new FileWriter(path));
	}
	
	public void setFlushInterval(int flushInterval) {
		if(flushInterval < 0) {
			flushInterval = 10; //default value: 10ms
		}
			this.flushInterval = flushInterval;
	}

	public int getFlushInterval() {		
		return flushInterval;
	}

	/**
	 * Writes an event along with its associated timestamp into a CSV file
	 * 
	 * @param e				An event, already in CSV representation
	 * @param timestamp		The timestamp of the event
	 * @throws IOException
	 */
	public synchronized void writeRecord(String e, long timestamp) throws IOException {
		writer.write(timestamp+Globals.CSV_SEPARATOR+e);
		writer.newLine();
		
		if(System.currentTimeMillis()-lastFlushTimestamp >= getFlushInterval()) {
			lastFlushTimestamp = System.currentTimeMillis();
			writer.flush();
		}
	}
	
	/**
	 * Writes an event into a CSV file
	 * 
	 * @param e				An event, already in CSV representation		
	 * @throws IOException
	 */
	public synchronized void writeRecord(String e) throws IOException {
		writer.write(e);
		writer.newLine();
		
		if(System.currentTimeMillis()-lastFlushTimestamp >= getFlushInterval()) {
			lastFlushTimestamp = System.currentTimeMillis();
			writer.flush();
		}
	}
	
	/**
	 * Writes an event along with its associated timestamp into a CSV file
	 * 
	 * @param e				An event, in an internal representation format
	 * @param timestamp		
	 * @throws IOException
	 */
	public synchronized void writeRecord(Event e, long timestamp) throws IOException {		
		if(timestamp != 0)		
			writeRecord(e.toCSV(), timestamp);
		else
			writeRecord(e.toCSV());
	}
		
	/**
	 * Writes an event into a CSV file
	 * 
	 * @param e				An event, in an internal representation format
	 * @throws IOException
	 */
	public synchronized void writeRecord(Event e) throws IOException {
		writeRecord(e, 0);
	}	
	
	public void closeFile() throws IOException{		
		writer.flush();
		writer.close();
	}
}
