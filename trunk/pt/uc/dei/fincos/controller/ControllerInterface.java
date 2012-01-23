package pt.uc.dei.fincos.controller;

public interface ControllerInterface {	
	
	void openTestSetup();	
	void loadAllComponents();
	void loadAllDrivers();
	void loadAllSinks();
	void startAllDrivers();
	void startDriver(String driverID);
	void pauseAllDrivers();
	void pauseDriver(String driverID);
	void alterEventSubmissionRate();
	void stopAllComponents();
	void stopAllDrivers();
	void stopDriver(String driverID);
	void stopSink(String sinkID);
}
