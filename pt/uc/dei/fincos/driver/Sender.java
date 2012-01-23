package pt.uc.dei.fincos.driver;

import java.io.IOException;

import pt.uc.dei.fincos.adapters.CEPEngineInterface;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.basic.Status;
import pt.uc.dei.fincos.basic.Step;
import pt.uc.dei.fincos.communication.ClientSocketInterface;
import pt.uc.dei.fincos.controller.Logger;
import pt.uc.dei.fincos.data.DataFileReader;


/**
 * Sends Events to CEP engines or FINCoS Adapter 
 * 
 * @author Marcelo R.N. Mendes
 * @see Driver
 *
 */
public class Sender extends Thread{
	// Setup
	private int communicationMode;
	private int rtMeasurementMode;
	private boolean useCreationTime;
	private boolean containsTimestamps; 
	private int timestampUnit;
	private int validSamplMod;
	private int loopCount;
	
	// Run and control
	private Scheduler scheduler;
	private DataGen datagen;
	private DataFileReader dataFileReader;
	private Logger logger;	
	private long timeInPause = 0;  // Time that the Thread was paused since last pause, in nanoseconds
	
	private static final long SLEEP_TIME_RESOLUTION = (long) 1E6; /* Highest resolution for 
																	Thread.sleep method, in nanoseconds.
																	Configured value: 1ms.
																	*/
	
	// Communication
	private ClientSocketInterface server, validator; 
	private CEPEngineInterface cepEngineInterface;
	
	// Status and Statistics
	private long sentEventCount = 0;
	private Status status;
	
	/**
	 * Constructor for sending events to both FINCoS Adapter and FINCoS Perfmon.
	 * Event submission is controlled by the Scheduler passed as argument
	 * Events' data is generated in runtime. [ADAPTER_CSV communication]
	 * 
	 * @param server					A socket interface to the server/adapter 
	 * @param validator					A socket interface to a validator
	 * @param scheduler					Schedules event submission according to a given rate
	 * @param dataGen					Events' data generator 
	 * @param group						A thread group
	 * @param id						A thread ID
	 * @param validationSamplingRate	The percentage of events to forward to a validator
	 * @param loopCount					Number of repetitions of the workload
	 */
	public Sender(ClientSocketInterface server, ClientSocketInterface validator, 
				  Scheduler scheduler, DataGen dataGen, ThreadGroup group, String id, 
				  double validationSamplingRate, int loopCount) {
		super(group, id);
		this.communicationMode = Globals.ADAPTER_CSV_COMMUNICATION;
		this.server = server;
		this.validator = validator;
		this.scheduler = scheduler;
		this.datagen = dataGen;
		this.containsTimestamps = false; // No data file -> No timestamps
		this.status = new Status(Step.STOPPED, 0);
		this.validSamplMod = (int) (1/validationSamplingRate);
		this.loopCount = loopCount;
	}
	
	/**
	 * Constructor for sending events only to FINCoS Adapter (no online validation).
	 * Event submission is controlled by the Scheduler passed as argument.
	 * Events' data is generated in runtime. [ADAPTER_CSV communication].
	 * 
	 * @param server				A socket interface to the server/adapter 
	 * @param scheduler				Schedules event submission according to a given rate
	 * @param datagen				Events' data generator
	 * @param group					A thread group
	 * @param id					A thread ID
	 * @param loopCount					Number of repetitions of the workload
	 */
	public Sender(ClientSocketInterface server, Scheduler scheduler, DataGen datagen, 
			ThreadGroup group, String id, int loopCount) {
		this(server, null, scheduler, datagen, group, id, 1, loopCount);
	}
	
	/**
	 * Constructor for sending events to both FINCoS Adapter and FINCoS Perfmon.
	 * Event submission is controlled by the Scheduler passed as argument.
	 * Events' data is loaded from data file. [ADAPTER_CSV communication]
	 * 
	 * @param server					A socket interface to the server/adapter 
	 * @param validator					A socket interface to an instance of FINCoS Perfmon
	 * @param scheduler					Schedules event submission according to a given rate
	 * @param dataReader				Reads data sets from disk
	 * @param containsTimestamps		Indicates if the events in the data file are associated to timestamps
	 * @param group						A thread group
	 * @param id						A thread ID
	 * @param validationSamplingRate	The percentage of events to forward to a FINCoS Perfmon
	 * @param loopCount					Number of repetitions of the workload
	 */
	public Sender(ClientSocketInterface server, ClientSocketInterface validator, 
				  Scheduler scheduler, DataFileReader dataReader, boolean containsTimestamps, 
				  ThreadGroup group, String id, double validationSamplingRate, int loopCount) 
	{
		super(group, id);
		this.communicationMode = Globals.ADAPTER_CSV_COMMUNICATION;
		this.server = server;
		this.validator = validator;
		this.scheduler = scheduler;
		this.dataFileReader = dataReader;
		this.containsTimestamps = containsTimestamps;
		this.status = new Status(Step.STOPPED, 0);
		this.validSamplMod = (int) (1/validationSamplingRate);
		this.loopCount = loopCount;
	}
	
	/**
	 * Constructor for sending events only to FINCoS Adapter (no validation).
	 * Event submission is controlled by the Scheduler passed as argument.
	 * Events' data is loaded from data file. [ADAPTER_CSV communication] 
	 * 
	 * @param server				A socket interface to the server/adapter 
	 * @param scheduler				Schedules event submission according to a given rate
	 * @param dataReader			Reads data sets from disk
	 * @param containsTimestamps	Indicates if the events in the data file are associated to timestamps
	 * @param group					A thread group
	 * @param id					A thread ID
	 * @param loopCount					Number of repetitions of the workload
	 */
	public Sender(ClientSocketInterface server, Scheduler scheduler, DataFileReader dataReader, 
			boolean containsTimestamps, ThreadGroup group, String id, int loopCount) {
		this(server, null, scheduler, dataReader, containsTimestamps, group, id, 1, loopCount);
	}
	
	/**
	 * Constructor for sending events to both FINCoS Adapter and FINCoS Perfmon.
	 * Event submission is controlled by the Scheduler passed as argument.
	 * [ADAPTER_CSV communication].
	 * 
	 * @param server					A socket interface to the server/adapter 
	 * @param validator					A socket interface to a validator
	 * @param scheduler					Schedules event submission according to a given rate
	 * @param dataReader				Reads data sets from disk
	 * @param containsTimestamps		Indicates if the events in the data file are associated to timestamps
	 * @param validationSamplingRate	The percentage of events to forward to a validator
	 * @param loopCount					Number of repetitions of the workload
	 */
	public Sender(ClientSocketInterface server, ClientSocketInterface validator, Scheduler scheduler, 
			DataFileReader dataReader, boolean containsTimestamps, double validationSamplingRate,
			int loopCount) {
		this.communicationMode = Globals.ADAPTER_CSV_COMMUNICATION;
		this.server = server;	
		this.validator = validator;
		this.scheduler = scheduler;
		this.dataFileReader = dataReader;		
		this.containsTimestamps = containsTimestamps;
		this.status = new Status(Step.STOPPED, 0);
		this.validSamplMod = (int) (1/validationSamplingRate);
		this.loopCount = loopCount;
	}
	
	/**
	 * Constructor for sending events only to FINCoS Adapter (no online validation).
	 * Event submission is controlled by the Scheduler passed as argument. 
	 * [ADAPTER_CSV communication].
	 * 
	 * @param server				A socket interface to the server/adapter
	 * @param scheduler				Schedules event submission according to a given rate
	 * @param dataReader			Reads data sets from disk
	 * @param containsTimestamps	Indicates if the events in the data file are associated to timestamps
	 * @param loopCount					Number of repetitions of the workload
	 */
	public Sender(ClientSocketInterface server, Scheduler scheduler, 
				  DataFileReader dataReader, boolean containsTimestamps, int loopCount) {
		this(server, null, scheduler, dataReader, containsTimestamps, 1, loopCount);	
	}
	
	/**
	 * Constructor for sending events to both FINCoS Adapter and FINCoS Perfmon.
	 * Event submission is controlled by timestamps in the datafile.
	 * [ADAPTER_CSV communication].
	 * 
	 * @param server					A socket interface to the server/adapter
	 * @param validator					A socket interface to a validator
	 * @param dataReader				Reads data sets from disk
	 * @param timestampUnit				Time Unit of timestamps in data file 
	 * @param validationSamplingRate	The percentage of events to forward to a validator
	 * @param loopCount					Number of repetitions of the workload
	 */
	public Sender(ClientSocketInterface server, ClientSocketInterface validator, 
			DataFileReader dataReader, int timestampUnit, double validationSamplingRate,
			int loopCount) {
		this.communicationMode = Globals.ADAPTER_CSV_COMMUNICATION;
		this.scheduler = null;
		this.server = server;
		this.validator = validator;
		this.dataFileReader = dataReader;
		this.validSamplMod = (int) (1/validationSamplingRate);
		this.containsTimestamps = true; // Event submission controlled by timestamps 
		this.timestampUnit = timestampUnit;
		this.status = new Status(Step.STOPPED, 0);
		this.loopCount = loopCount;
	}
	
	/**
	 * Constructor for sending events only to FINCoS Adapter (no online validation).
	 * Event submission is controlled by timestamps in the datafile.
	 * [ADAPTER_CSV communication].
	 * 
	 * @param server				A socket interface to the server/adapter
	 * @param dataReader			Reads data sets from disk
	 * @param timestampUnit			Time Unit of timestamps in data file
	 * @param loopCount					Number of repetitions of the workload
	 */
	public Sender(ClientSocketInterface server, DataFileReader dataReader, int timestampUnit, int loopCount) {
		this(server, null, dataReader, timestampUnit, 1, loopCount);
	}
	
	/**
	 * 
	 * Constructor for sending events to both CEP Engine and FINCoS Perfmon.
	 * Event submission is controlled by the Scheduler passed as argument.
	 * Events' data is loaded from data file. [DIRECT_API communication].
	 * 
	 * @param cepEngineInterface		A direct interface with a CEP engine using its proprietary API 
	 * @param validator					A socket interface to an instance of FINCoS Perfmon
	 * @param scheduler					Schedules event submission according to a given rate
	 * @param dataReader				Reads data sets from disk
	 * @param containsTimestamps		Indicates if the events in the data file are associated to timestamps
	 * @param group						A thread group
	 * @param id						A thread ID
	 * @param validationSamplingRate	The percentage of events to forward to a FINCoS Perfmon
	 * @param loopCount					Number of repetitions of the workloads
	 */
	public Sender(CEPEngineInterface cepEngineInterface,
			ClientSocketInterface validator, Scheduler scheduler,
			DataFileReader dataReader, boolean containsTimestamps,
			ThreadGroup group, String id, double validationSamplingRate, int loopCount) {
		super(group, id);
		this.communicationMode = Globals.DIRECT_API_COMMUNICATION;
		this.cepEngineInterface = cepEngineInterface;
		this.validator = validator;
		this.scheduler = scheduler;
		this.dataFileReader = dataReader;
		this.containsTimestamps = containsTimestamps;
		this.status = new Status(Step.STOPPED, 0);
		this.validSamplMod = (int) (1/validationSamplingRate);
		this.loopCount = loopCount;
	}

	/**
	 * 
     * Constructor for sending events only to CEP Engine (no online validation).
	 * Event submission is controlled by the Scheduler passed as argument.
	 * Events' data is loaded from data file. [DIRECT_API communication]
	 * 
	 * @param cepEngineInterface	A direct interface with a CEP engine using its proprietary API 
	 * @param scheduler				Schedules event submission according to a given rate
	 * @param dataReader			Reads data sets from disk
	 * @param containsTimestamps	Indicates if the events in the data file are associated to timestamps
	 * @param group					A thread group
	 * @param id					A thread ID
	 * @param loopCount					Number of repetitions of the workload
	 * 
	 */
	public Sender(CEPEngineInterface cepEngineInterface, Scheduler scheduler,
			DataFileReader dataReader, boolean containsTimestamps, ThreadGroup group,
			String id, int loopCount) {
		this(cepEngineInterface, null, scheduler, dataReader, containsTimestamps, group, id, 1, loopCount);	
	}

	/**
	 * 
	 * Constructor for sending events to both CEP Engine and FINCoS Perfmon.
	 * Event submission is controlled by the Scheduler passed as argument
	 * Events' data is generated in runtime. [DIRECT_API communication]
	 * 
	 * @param cepEngineInterface		A direct interface with a CEP engine using its proprietary API 
	 * @param validator					A socket interface to a validator
	 * @param scheduler					Schedules event submission according to a given rate
	 * @param dataGen					Events' data generator
	 * @param group						A thread group
	 * @param id						A thread ID
	 * @param validationSamplingRate	The percentage of events to forward to a validator
	 * @param loopCount					Number of repetitions of the workload
	 * 
	 */
	public Sender(CEPEngineInterface cepEngineInterface,
			ClientSocketInterface validator, Scheduler scheduler, DataGen dataGen,
			ThreadGroup group, String id,
			double validationSamplingRate, int loopCount) {
		super(group, id);
		this.communicationMode = Globals.DIRECT_API_COMMUNICATION;
		this.cepEngineInterface = cepEngineInterface;
		this.validator = validator;
		this.scheduler = scheduler;
		this.datagen = dataGen;
		this.containsTimestamps = false; // No data file -> No timestamps
		this.status = new Status(Step.STOPPED, 0);
		this.validSamplMod = (int) (1/validationSamplingRate);
		this.loopCount = loopCount;
	}

	/**
	 * 
	 * Constructor for sending events only to CEP Engine (no online validation).
	 * Event submission is controlled by the Scheduler passed as argument.
	 * Events' data is generated in runtime. [DIRECT_API communication].
	 * 
	 * @param cepEngineInterface	A direct interface with a CEP engine using its proprietary API 
	 * @param scheduler				Schedules event submission according to a given rate
	 * @param datagen				Events' data generator
	 * @param group					A thread group
	 * @param id					A thread ID
	 * @param loopCount				Number of repetitions of the workload
	 * 
	 */
	public Sender(CEPEngineInterface cepEngineInterface, Scheduler scheduler,
			DataGen datagen, ThreadGroup group, String id, int loopCount) {
		this(cepEngineInterface, null, scheduler, datagen, group, id, 1, loopCount);
	}
	
	/**
	 * Constructor for sending events to both FINCoS Adapter and FINCoS Perfmon.
	 * Event submission is controlled by the Scheduler passed as argument.
	 * [DIRECT_API communication].
	 * 
	 * @param cepEngineInterface		A direct interface with a CEP engine using its proprietary API 
	 * @param validator					A socket interface to a validator
	 * @param scheduler					Schedules event submission according to a given rate
	 * @param dataReader				Reads data sets from disk
	 * @param containsTimestamps		Indicates if the events in the data file are associated to timestamps
	 * @param validationSamplingRate	The percentage of events to forward to a validator
	 * @param loopCount					Number of repetitions of the workload
	 */
	public Sender(CEPEngineInterface cepEngineInterface, ClientSocketInterface validator, Scheduler scheduler, 
			DataFileReader dataReader, boolean containsTimestamps, double validationSamplingRate,
			int loopCount) {
		this.communicationMode = Globals.DIRECT_API_COMMUNICATION;
		this.cepEngineInterface= cepEngineInterface;	
		this.validator = validator;
		this.scheduler = scheduler;
		this.dataFileReader = dataReader;		
		this.containsTimestamps = containsTimestamps;
		this.status = new Status(Step.STOPPED, 0);
		this.validSamplMod = (int) (1/validationSamplingRate);
		this.loopCount = loopCount;
	}
	
	/**
	 * Constructor for sending events only to FINCoS Adapter (no online validation).
	 * Event submission is controlled by the Scheduler passed as argument. 
	 * [DIRECT_API communication].
	 * 
	 * @param cepEngineInterface	A direct interface with a CEP engine using its proprietary API
	 * @param scheduler				Schedules event submission according to a given rate
	 * @param dataReader			Reads data sets from disk
	 * @param containsTimestamps	Indicates if the events in the data file are associated to timestamps
	 * @param loopCount				Number of repetitions of the workload
	 */
	public Sender(CEPEngineInterface cepEngineInterface, Scheduler scheduler, 
				  DataFileReader dataReader, boolean containsTimestamps, int loopCount) {
		this(cepEngineInterface, null, scheduler, dataReader, containsTimestamps, 1, loopCount);	
	}
	
	/**
	 * Constructor for sending events to both FINCoS Adapter and FINCoS Perfmon.
	 * Event submission is controlled by timestamps in the datafile.
	 * [DIRECT_API communication].
	 * 
	 * @param cepEngineInterface		A direct interface with a CEP engine using its proprietary API
	 * @param validator					A socket interface to a validator
	 * @param dataReader				Reads data sets from disk
	 * @param timestampUnit				Time Unit of timestamps in data file 
	 * @param validationSamplingRate	The percentage of events to forward to a validator
	 * @param loopCount					Number of repetitions of the workload
	 */
	public Sender(CEPEngineInterface cepEngineInterface, ClientSocketInterface validator, 
			DataFileReader dataReader, int timestampUnit, double validationSamplingRate,
			int loopCount) {
		this.communicationMode = Globals.DIRECT_API_COMMUNICATION;
		this.scheduler = null;
		this.cepEngineInterface = cepEngineInterface;
		this.validator = validator;
		this.dataFileReader = dataReader;
		this.validSamplMod = (int) (1/validationSamplingRate);
		this.containsTimestamps = true; // Event submission controlled by timestamps 
		this.timestampUnit = timestampUnit;
		this.status = new Status(Step.STOPPED, 0);
		this.loopCount = loopCount;
	}
	
	/**
	 * Constructor for sending events only to FINCoS Adapter (no online validation).
	 * Event submission is controlled by timestamps in the datafile.
	 * [DIRECT_API communication].
	 * 
	 * @param cepEngineInterface	A direct interface with a CEP engine using its proprietary API
	 * @param dataReader			Reads data sets from disk
	 * @param timestampUnit			Time Unit of timestamps in data file
	 * @param loopCount				Number of repetitions of the workload
	 * 
	 */
	public Sender(CEPEngineInterface cepEngineInterface, DataFileReader dataReader, 
				  int timestampUnit, int loopCount) {
		this(cepEngineInterface, null, dataReader, timestampUnit, 1, loopCount);
	}

	/**
	 * Return the current status of this sender thread
	 * 
	 * @return
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * Return the number of events sent so far by this sender thread
	 * 
	 * @return
	 */
	public long getSentEventCount() {
		return sentEventCount;
	}
	
	/**
	 * Stops sending events until a call to 'resume' method be done
	 */
	public void pauseLoad() {		
		this.status.setStep(Step.PAUSED);
	}
	
	/**
	 * Restarts event submission, if previously paused
	 */
	public void resumeLoad() {
		synchronized (this) {
			this.status.setStep(Step.RUNNING);
			notifyAll();
		}
	}
	
	/**
	 * Stops permanently event submission
	 */
	public void stopLoad() {		
		synchronized (this) {
			this.status.setStep(Step.STOPPED);
			// Case it is paused
			notifyAll();
		}
		
	}
	
	/**
	 * Method used to increase or decrease the rate at which events are submitted
	 * 
	 * @param factor		The multiplication factor (e.g. 2 = 2x faster than original rate)
	 */
	public void setRateFactor(double factor){
		this.scheduler.setRateFactor(factor);
	}
	
	public void run() {
		if(this.scheduler != null)
			scheduledRun();
		else
			timestampedRun();
	}
	
	private void scheduledRun() {
		long elapsedTime, interTime;			
		long sleepTime;	
		long pauseT0;				

		Event event=null;
		
		try {			
			this.status.setStep(Step.RUNNING);			
			String csvEvent=null; 
			
			if(dataFileReader != null) 
				csvEvent = dataFileReader.getNextCSVEvent();
			else if(datagen != null) {
				synchronized (datagen) {
					event = datagen.getNextEvent();	
				}	
				if(communicationMode == Globals.ADAPTER_CSV_COMMUNICATION) 
					csvEvent = event.toCSV();
			}									
							
			for (int i = 0; i < loopCount; i++) {
				long expectedElapsedTime = 0; // in nanoseconds
				long cumulateInterTime = 0;  // in nanoseconds
				long t0 = System.currentTimeMillis();
				
				// Used when timestamping mode is based on creation time -----------------
				long firstTimestamp = t0;
				double sendTimestamp = 0;	
				long t00, t11;
				double diffSleep;
				//------------------------------------------------------------------------
				
				while(csvEvent != null || event != null) {
					// Removes event's timestamp (First field), if it exists in the data file
					if(containsTimestamps)
						csvEvent = csvEvent.substring(csvEvent.indexOf(Globals.CSV_SEPARATOR)+1);							
					
					try {					
						/* Sleeps for a interval defined by scheduler according to event rate.
						 * The effective sleep time is adjusted to compensate for possible delays: 
						 *  Actual Sleep time = interarrival time minus the difference between the 
						 *  actual elapsed time and the expected elapsed time, including the time 
						 *  during which the thread was in pause.
						 */					
						interTime = this.scheduler.getInterArrivalTime();
						cumulateInterTime += interTime;		
						diffSleep = 0;
						if(cumulateInterTime >= SLEEP_TIME_RESOLUTION) {					
							elapsedTime = (System.currentTimeMillis()-t0)*SLEEP_TIME_RESOLUTION;
							sleepTime = (cumulateInterTime-(elapsedTime-expectedElapsedTime-timeInPause))
										/SLEEP_TIME_RESOLUTION;																	
							cumulateInterTime = cumulateInterTime%SLEEP_TIME_RESOLUTION;						
							if(sleepTime > 0) {	
								t00 = System.nanoTime();
								Thread.sleep(sleepTime);
								t11 = System.nanoTime();
								diffSleep = sleepTime-1.0*(t11-t00)/SLEEP_TIME_RESOLUTION;
							}							
						}			
						expectedElapsedTime += interTime;					
						//Checks if driver has been paused and waits if so
						synchronized (this) {
							pauseT0 = 0;
							while(this.status.getStep() == Step.PAUSED) {
								if(pauseT0 == 0)
									pauseT0 = System.nanoTime();
								this.wait();
							}
							if(pauseT0 != 0)
								timeInPause+=(System.nanoTime()-pauseT0);					
						}
						
						//Checks if driver was stopped
						if(this.status.getStep() == Step.STOPPED)
							return;															
						
						sendTimestamp = firstTimestamp + (1.0*(expectedElapsedTime-timeInPause)/SLEEP_TIME_RESOLUTION+diffSleep);
						
						// Sends event to Adapter/CEP engine
						if(communicationMode == Globals.DIRECT_API_COMMUNICATION &&
						   datagen != null) {
							this.sendEvent(event, (long)sendTimestamp);	
						}
						else
							this.sendEvent(csvEvent, (long)sendTimestamp);
						
					}
					catch (Exception exc) {
						System.err.println("Cannot send event (" + exc.getMessage() + ")");
						exc.printStackTrace();
						if(this.status.getStep() == Step.RUNNING)
							this.status.setStep(Step.ERROR);
					}
						
					if(dataFileReader != null) // read event from data file 
						csvEvent = dataFileReader.getNextCSVEvent();
					else if(datagen != null)  { // generate event					
						event = datagen.getNextEvent();	
						// if events are exchanged through FINCoS Adapter, convert to CSV
						if(communicationMode == Globals.ADAPTER_CSV_COMMUNICATION) 
							csvEvent = event != null ? event.toCSV() : null;										
					}				
				}
				if(dataFileReader != null) {
					dataFileReader.reOpen();
					csvEvent = dataFileReader.getNextCSVEvent();
				}
			}
								
			this.status.setStep(Step.FINISHED);
		}	
		catch (IOException ioe) {
			System.err.println("Cannot read datafile (" + ioe.getMessage() + ")");
			this.status.setStep(Step.ERROR);
		}
		catch (Exception exc) {
			System.err.println("Unexpected exception. (" + exc.getClass() + "-" + exc.getMessage() + ")\n load submisson will abort.");			
			this.status.setStep(Step.ERROR);
			exc.printStackTrace();
			return;
		}	
		finally {
			if(dataFileReader != null)
				dataFileReader.closeFile();
		}
	}
	
	private void timestampedRun() {
		long elapsedTime, interTime;			
		long sleepTime;	
		long pauseT0;		
		long currentTS, lastTS;
		long timeResolution;		
		
		if(this.timestampUnit == ExternalFileWorkloadPhase.MILLISECONDS)
			timeResolution = 1;
		else // seconds and date/time
			timeResolution = 1000;
			
		try {
			this.status.setStep(Step.RUNNING);
			String event = dataFileReader.getNextCSVEvent();			
			String timestamp;
			
			// initializes lastTS variable
			if(event != null) {
				timestamp = event.substring(0, event.indexOf(Globals.CSV_SEPARATOR));				
				if(timestampUnit == ExternalFileWorkloadPhase.DATE_TIME)
					lastTS = Globals.DATE_TIME_FORMAT.parse(timestamp).getTime();
				else
					lastTS = Long.parseLong(timestamp);
			}
			else
				return;					
						
			for (int i = 0; i < loopCount; i++) {
				long expectedElapsedTime = 0;
				long t0 = System.currentTimeMillis();
				
				// Used when timestamping mode is based on creation time -----------------
				long creationTime; 
				long firstTimestamp = t0;
				//------------------------------------------------------------------------
				
				while(event != null) {
					timestamp = event.substring(0, event.indexOf(Globals.CSV_SEPARATOR));
					// Removes event's timestamp				
					event = event.substring(event.indexOf(Globals.CSV_SEPARATOR)+1);				
					
					if(timestampUnit == ExternalFileWorkloadPhase.DATE_TIME)
						currentTS = Globals.DATE_TIME_FORMAT.parse(timestamp).getTime();
					else
						currentTS = Long.parseLong(timestamp);							
					
					try {					
						/* Sleeps for a interval between consecutive events in the data file.
						 * The effective sleep time is adjusted to compensate for possible delays; 
						 * Actual Sleep time = interarrival time minus the difference between 
						 * the actual elapsed time and the expected elapsed time.
						 */					
						interTime = timeResolution*(currentTS-lastTS);
													
						elapsedTime = System.currentTimeMillis()-t0;
						sleepTime = interTime - (elapsedTime-timeInPause-expectedElapsedTime);
						if(sleepTime > 0) {							
							Thread.sleep(sleepTime);
						}
			
						expectedElapsedTime += interTime;
						lastTS = currentTS;		
						
						//Checks if driver was paused and waits if so
						synchronized (this) {
							pauseT0 = 0;
							while(this.status.getStep() == Step.PAUSED) {
								if(pauseT0 == 0)
									pauseT0 = System.currentTimeMillis();
								this.wait();
							}
							if(pauseT0 != 0)
								timeInPause+=(System.currentTimeMillis()-pauseT0);					
						}
						
						//Checks if driver was stopped
						if(this.status.getStep() == Step.STOPPED)
							return;

						creationTime = firstTimestamp+(expectedElapsedTime-timeInPause);					
						
						// Sends event to Adapter/CEP engine
						this.sendEvent(event, creationTime);
					}
					catch (Exception ioe2) {
						System.err.println("Cannot send event (" + ioe2.getMessage() + ")");
						if(this.status.getStep() == Step.RUNNING)
							this.status.setStep(Step.ERROR);
					}
						
					event = dataFileReader.getNextCSVEvent();
				}
				
				dataFileReader.reOpen();
				event = dataFileReader.getNextCSVEvent();
			}
							
			this.status.setStep(Step.FINISHED);
		}	
		catch (IOException ioe) {
			System.err.println("Cannot read datafile (" + ioe.getMessage() + ")");
			this.status.setStep(Step.ERROR);
		}
		catch (Exception exc) {
			System.err.println("Unexpected exception. (" + exc.getClass() + "-" + exc.getMessage() + ")\n load submisson will abort.");
			exc.printStackTrace();
			this.status.setStep(Step.ERROR);
			return;
		}
		finally {
			if(dataFileReader != null)
				dataFileReader.closeFile();
		}
	}
	
	private void sendEvent(String event, long timestamp) throws Exception {
		// If not use creation time...
		if(!useCreationTime)			
			timestamp = System.currentTimeMillis(); // use send time
		
		// Send the event to server
		if(communicationMode == Globals.ADAPTER_CSV_COMMUNICATION) {
			if(rtMeasurementMode == Globals.END_TO_END_RT_MILLIS)
				this.server.sendTimestampedCSVEvent(event, timestamp);
			else if (rtMeasurementMode == Globals.ADAPTER_RT_NANOS ||
					 rtMeasurementMode == Globals.NO_RT)
				this.server.sendCSVEvent(event);	
		}
		else if(communicationMode == Globals.DIRECT_API_COMMUNICATION) {
			if(rtMeasurementMode == Globals.END_TO_END_RT_MILLIS)
				this.cepEngineInterface.sendEvent(event+Globals.CSV_SEPARATOR+timestamp);				
			else if (rtMeasurementMode == Globals.NO_RT)
				this.cepEngineInterface.sendEvent(event);
		}	
		
		sentEventCount++;
		
		if(logger != null) {
			logger.log(event, timestamp);
		}
		
		try {
			// Send the event to validator
			if(this.validator != null && sentEventCount%this.validSamplMod == 0) {				
					if(rtMeasurementMode == Globals.ADAPTER_RT_NANOS)
						validator.sendCSVEvent(event+Globals.CSV_SEPARATOR+System.nanoTime());
					else if(rtMeasurementMode == Globals.END_TO_END_RT_MILLIS ||
							rtMeasurementMode == Globals.NO_RT)
						validator.sendTimestampedCSVEvent(event, timestamp);						
			}
		} catch (Exception ioe1) {
			System.err.println("Cannot send event to validator(" + ioe1.getMessage() + ")");
		}
	}
		
	private void sendEvent(Event event, long timestamp) throws Exception {				
		// If not use creation time...
		if(!useCreationTime)			
			timestamp = System.currentTimeMillis(); // use send time
		
		// Send the event to server;
		if(communicationMode == Globals.ADAPTER_CSV_COMMUNICATION) {
			String csvEvent = event.toCSV();
			if(rtMeasurementMode == Globals.END_TO_END_RT_MILLIS)
				this.server.sendTimestampedCSVEvent(csvEvent, timestamp);
			else if (rtMeasurementMode == Globals.ADAPTER_RT_NANOS ||
					 rtMeasurementMode == Globals.NO_RT)
				this.server.sendCSVEvent(csvEvent);
			
			if(logger != null) {
				logger.log(csvEvent, timestamp);
			}
		}
		else if(communicationMode == Globals.DIRECT_API_COMMUNICATION) {			
			if(rtMeasurementMode == Globals.END_TO_END_RT_MILLIS) {
				event.setTimestamp(timestamp);				
					cepEngineInterface.sendEvent(event);								
			}								
			else if (rtMeasurementMode == Globals.NO_RT) {				
					cepEngineInterface.sendEvent(event);					
			}
					
			
			if(logger != null) {
				logger.log(event.toCSV(), timestamp);
			}
		}

		sentEventCount++;
		
		try {
			// Send the event to validator
			if(this.validator != null && sentEventCount%this.validSamplMod == 0) {				
					if(rtMeasurementMode == Globals.ADAPTER_RT_NANOS)
						validator.sendCSVEvent(event+Globals.CSV_SEPARATOR+System.nanoTime());
					else if(rtMeasurementMode == Globals.END_TO_END_RT_MILLIS ||
							rtMeasurementMode == Globals.NO_RT)
						validator.sendTimestampedCSVEvent(event.toCSV(), timestamp);						
			}
		} catch (Exception ioe1) {
			System.err.println("Cannot send event to validator(" + ioe1.getMessage() + ")");
		}			
	}
	
	public void setRTMeasurementMode(int rtMeasurementMode) {
		this.rtMeasurementMode = rtMeasurementMode;
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setUseCreationTime(boolean useCreationTime) {
		this.useCreationTime = useCreationTime;
	}

	public boolean isUseCreationTime() {
		return useCreationTime;
	}
}
