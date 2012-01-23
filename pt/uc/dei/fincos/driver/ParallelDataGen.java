package pt.uc.dei.fincos.driver;


import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.Datatype;
import pt.uc.dei.fincos.basic.Domain;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.PredefinedListDomain;
import pt.uc.dei.fincos.basic.RandomDomain;
import pt.uc.dei.fincos.basic.SequentialDomain;
import pt.uc.dei.fincos.data.CSVWriter;
import pt.uc.dei.fincos.driver.Scheduler.ArrivalProcess;
import pt.uc.dei.fincos.random.ConstantVariate;
import pt.uc.dei.fincos.random.RandomUniformVariate;

/**
 * Class responsible for generating event's payload (Synthetic workload). 
 * This class is NOT synchronized
 * 
 * @author Marcelo R.N. Mendes
 *
 */
//TODO: Remove this test class
public class ParallelDataGen {	

	private long generatedEvents=0;
	private long totalEventCount;
	private boolean keepGenerating = true;	
	private PredefinedListDomain typeChooser; // used to choose the next event type to be generated according to event mix
	private Long mixSeed; // random seed used when choosing the next event type to be generated according to event mix
	
	private int maxDegreeOfParallelism;
	private ExecutorService threadPool;
	private Thread workerThreads[];
	CyclicBarrier barrier;
	
	
	/**
	 * Initializes DataGen's synthetic workload
	 * 
	 * @param workload	The synthetic workload specification
	 * @param maxDop	Maximum Degree of Parallelism (number of threads used in data generation)
	 */
	public ParallelDataGen(SyntheticWorkloadPhase workload, int maxDoP) {													
		this.totalEventCount = workload.getTotalEventCount(); 
		this.maxDegreeOfParallelism = maxDoP;
		workerThreads = new Thread[maxDegreeOfParallelism];
		this.threadPool =  Executors.newFixedThreadPool(maxDegreeOfParallelism);				
		
		LinkedHashMap<EventType, Double> types = workload.getSchema();
		
		// Converts set of types to a format suitable for PredefinedListDomain (Object, Double)
		LinkedHashMap<Object, Double> objectTypes = new LinkedHashMap<Object, Double>(types.size());
				
		Double mix=0.0;
		
		for (EventType type : types.keySet()) {
			mix = types.get(type);					
			objectTypes.put(type, mix);								
		}		
		// Creates a  Domain to choose types iteratively according to the mix specified in the configuration file
		if(workload.isDeterministicEventMix()) { // Deterministic			
			typeChooser = new PredefinedListDomain(objectTypes.keySet().toArray(new Object[0]));
		}
		else { // Stochastic			
			mixSeed =  workload.getRandomSeed();
			typeChooser = new PredefinedListDomain(objectTypes, mixSeed);	
		}	
	}
	
	class AttGen implements Runnable {	
		Event e;
		int beginIndex ,endIndex;
		CyclicBarrier barrier;
		
		public AttGen(Event e, int beginIndex, int endIndex, CyclicBarrier barrier) {			
			this.e = e;
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.barrier = barrier;					
		}
		
		@Override
		public void run() {		
			EventType type;			
			Attribute att;
			Domain d;	
			Object value;
			
			type = e.getType();
			
			for (int k = beginIndex; k <= endIndex; k++) {
				att = type.getAttributes()[k];
				d = att.getDomain();
				if (d != null) {
					value = d.generateValue(); 
					e.setAttributeValue(att, value);
				}				
			}			
			
			try {
				barrier.await();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
		}
		
	}
	
	/**
	 * Generates events according to a synthetic workload specified in the configuration file
	 * and saves them into a data file.
	 * 
	 * @param dataFilesDir		The directory where the data file(s) must be stored
	 * @param fileCount			The number of files into which data will be stored
	 * @throws IOException
	 */
	public void generateData(String dataFilesDir, int fileCount) throws IOException {
		// clear stats
		this.generatedEvents = 0;
		
		// Used to create data file(s)
		CSVWriter writers[];
								
		writers = new CSVWriter[fileCount];
		for (int i = 0; i < writers.length; i++) {
			writers[i] = new CSVWriter(dataFilesDir +"\\" + (i+1) + ".csv", 10);
		}
		
		try {	
			// Iterates over #events
			for (long j = 0; j < totalEventCount; j++) {				
				if(keepGenerating) {
					// Generates next event and writes it to disk
					writers[(int)(j%fileCount)].writeRecord(getNextEvent());									
				}
				else
					break;
			}

		} catch (IOException ioe) {			
			throw ioe;
		} finally {
			// close the files
			for (int i = 0; i < writers.length; i++) {
				writers[i].closeFile();
			}
		}
	}
	
	/**
	 * Generates the next event
	 * 
	 * @return
	 */
	public Event getNextEvent() {			
			// Variables involved in data generation (the events, attributes, and their values)
			EventType type;
			Event ret;		
			
			this.generatedEvents++;
			if(generatedEvents > totalEventCount)
				return null;

			
			// Choose a type according to the mix
			type = (EventType) typeChooser.generateValue(); // This method must be thread-safe				
			
			
			// Iterates over attributes
			ret = new Event(type);				
			
			int numThreads = Math.min(type.getAttributeCount(), maxDegreeOfParallelism);
			if(barrier == null)
				barrier = new CyclicBarrier(numThreads+1);
			else
				barrier.reset();
			for (int i = 0; i < numThreads; i++) {				
				this.threadPool.execute(new AttGen(ret, 
													i*(type.getAttributeCount()/numThreads), 
													(i+1)*(type.getAttributeCount()/numThreads)-1,
													barrier));	
		/*		workerThreads[i] = new Thread(new AttGen(ret, 
						i*(type.getAttributeCount()/numThreads), 
						(i+1)*(type.getAttributeCount()/numThreads)-1,
						barrier));
				workerThreads[i].start();*/
			}
			
			
			try {
			/*	for (Thread t : workerThreads) {
					if(t != null)
						t.join();
				}*/
			barrier.await();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return ret;			
	}
	
	public void stopDataGeneration() {
		this.keepGenerating = false;

	}
	
	public long getGeneratedEventsCount() {
		return generatedEvents;
	}	
	
	public double getProgress() {
		return 1.0*this.generatedEvents/this.totalEventCount;
	}
	
	public static void main(String[] args) {
		int maxDoP = Math.max(1,Runtime.getRuntime().availableProcessors()-1);
		LinkedHashMap<EventType, Double> schema = new LinkedHashMap<EventType, Double>();
		Domain idDomain = new SequentialDomain(new ConstantVariate(1), new ConstantVariate(1));
		Domain aDomain = new RandomDomain(new RandomUniformVariate(123l, 1d, 100d));
		schema.put(new EventType("A", new Attribute[] {
									new Attribute(Datatype.LONG, "ID", idDomain),
									new Attribute(Datatype.LONG, "A1", aDomain),
									new Attribute(Datatype.LONG, "A2", aDomain),
									new Attribute(Datatype.LONG, "A3", aDomain),
									new Attribute(Datatype.LONG, "A4", aDomain),
									new Attribute(Datatype.LONG, "A5", aDomain),
									new Attribute(Datatype.LONG, "A6", aDomain),
									new Attribute(Datatype.LONG, "A7", aDomain),
									new Attribute(Datatype.LONG, "A8", aDomain),
									new Attribute(Datatype.LONG, "A9", aDomain),
									new Attribute(Datatype.LONG, "A10", aDomain),
									new Attribute(Datatype.LONG, "A11", aDomain),
									new Attribute(Datatype.LONG, "A12", aDomain),
									new Attribute(Datatype.LONG, "A13", aDomain),
									new Attribute(Datatype.LONG, "A14", aDomain),
									new Attribute(Datatype.LONG, "A15", aDomain),
									new Attribute(Datatype.LONG, "A16", aDomain),
									new Attribute(Datatype.LONG, "A17", aDomain),
									new Attribute(Datatype.LONG, "A18", aDomain),
									new Attribute(Datatype.LONG, "A19", aDomain),
									new Attribute(Datatype.LONG, "A20", aDomain),
									new Attribute(Datatype.LONG, "A21", aDomain),
									new Attribute(Datatype.LONG, "A22", aDomain),
									new Attribute(Datatype.LONG, "A23", aDomain)									
									}
								),	1.0);
		SyntheticWorkloadPhase workload = new SyntheticWorkloadPhase(
				60, 1000000, 1000000, ArrivalProcess.DETERMINISTIC, schema, true, 1, null);
		DataGen dg = new DataGen(workload);
		ParallelDataGen parallelDG = new ParallelDataGen(workload, maxDoP);
		System.out.println("MaxDoP: "+ maxDoP);
		Event e=null;
		//warmup
		for (int i = 0; i < 100000; i++) {
			e = parallelDG.getNextEvent();
			//e = dg.getNextEvent();
		}
		
		// MI
		int loopCount = 100000;		
		long t0 = System.nanoTime();
		for (int i = 0; i < loopCount; i++) {
			e = parallelDG.getNextEvent();
			//e = dg.getNextEvent();
		}
		long t1 = System.nanoTime();
		System.err.println(e);
		System.out.println("Elapsed: " + (t1-t0)/1E6 + " ms.");
		System.out.println("Throughput: " + 1E9*loopCount/(t1-t0) + " events/sec.");
		
		System.exit(0);
	}
}
