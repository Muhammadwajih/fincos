package pt.uc.dei.fincos.adapters;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import pt.uc.dei.fincos.basic.Globals;
import pt.uc.dei.fincos.sink.Sink;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;


/**
 * Implementation of an event listener for Esper.
 *
 * @author Marcelo R.N. Mendes
 * @see OutputListener
 *
 */
public class EsperListener extends OutputListener implements UpdateListener {

	/** Reference to the Esper service provider. */
	private EPServiceProvider epService;

	/** The query this listener listens to. */
	EPStatement query;
	private String queryOutputName;
    private String queryText;

	/** The schema of the output produced by the query. */
	LinkedHashMap<String, String> querySchema;

	/** The format used to exchange events with the Esper engine. */
	private int eventFormat;

	/**
	 * This Listener runs at FINCoS Adapter and events received from Esper are forwarded to Sink through
	 * socket communication.
	 *
	 * @param epService			Esper Service Instance
	 * @param queryOutputName	Name of output stream which this listener subscribes to
	 * @param queryText			Query text, expressed in Esper's EPL
	 * @param querySchema		Schema of output stream
	 * @param sinksList			Address(es) and port(s) of Sink(s) to which received events must be forwarded
	 * @param eventFormat		Either MAP or POJO
	 */
	public EsperListener(EPServiceProvider epService, String queryOutputName,
						  String queryText, LinkedHashMap<String, String> querySchema,
						  ArrayList<InetSocketAddress> sinksList,
						  int eventFormat) {
		this.communicationMode = Globals.ADAPTER_CSV_COMMUNICATION;
		this.epService = epService;
		this.queryText = queryText;
		this.querySchema = querySchema;
		this.queryOutputName = queryOutputName;
		this.listenerID = queryOutputName;
		this.sinksList = sinksList;
		this.setEventFormat(eventFormat);
	}
	/**
	 * Constructor for direct communication between Esper and Sink. Events received from Esper are
	 * forwarded to Sink through method call.
	 *
	 * @param epService			Esper Service Instance
	 * @param queryOutputName	Name of output stream which this listener subscribes to
	 * @param queryText			Query text, expressed in Esper's EPL
	 * @param querySchema		Schema of output stream
	 * @param sinkInstance		An instance of Sink that must receive events from this listener
	 * @param eventFormat		Either MAP or POJO
	 */
	public EsperListener(EPServiceProvider epService, String queryOutputName,
			String queryText, LinkedHashMap<String, String> querySchema,
			Sink sinkInstance, int eventFormat) {
		this.communicationMode = Globals.DIRECT_API_COMMUNICATION;
		this.epService = epService;
		this.queryText = queryText;
		this.querySchema = querySchema;
		this.queryOutputName = queryOutputName;
		this.listenerID = queryOutputName;
		this.sinkInstance = sinkInstance;
		this.setEventFormat(eventFormat);
	}

	@Override
	public void load() throws Exception {
		try {
			System.out.println("Loading query: \n" + queryText);
			query = epService.getEPAdministrator().createEPL(queryText, queryOutputName);
		} catch (Exception e) {
			throw new Exception("Could not create EPL statement (" + e.getMessage() + ").");
		}
	}

	@Override
	public void run() {
			query.addListener(this);
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		String timestamp = "";
		if (this.rtMeasurementMode == Globals.ADAPTER_RT_NANOS) {
			timestamp += Globals.CSV_SEPARATOR + System.nanoTime();
		}

		for (int i = 0; i < newEvents.length; i++) {
			processIncomingEvent(newEvents[i], timestamp);
		}
	}

	private void processIncomingEvent(EventBean event, String timestamp) {
		if (this.communicationMode == Globals.DIRECT_API_COMMUNICATION) {
			forwardToSink(toFieldArray(event)); //TODO: Remove this or optimize code
		} else {
			forwardToSinks(toCSV(event) + timestamp);
		}


	}

	/**
	 * Translates the event from the Esper native format to Array of Objects.
	 *
	 * @param event	The event in Esper's native representation
	 * @return		The event as an array of objects
	 *
	 */
	private Object[] toFieldArray(EventBean event) {
		Object[] eventObj = null;

		if (querySchema != null) { ////Input events are MAPs
			eventObj = new Object[querySchema.size() + 1];
			eventObj[0] = queryOutputName;
			int i = 1;
			for (String att: querySchema.keySet()) {
					eventObj[i] = event.get(att);
					i++;
			}
		} else { //Input events are POJO
			try {
				Field[] fields = Class.forName(queryOutputName).getFields();
				eventObj = new Object[fields.length + 1];
				eventObj[0] = queryOutputName;
				int i = 1;
				for (Field f : fields) {
					eventObj[i] = event.get(f.getName());
					i++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return eventObj;
	}

	/**
	 *
	 * Translates the event from the Esper native format to the framework's CSV format.
	 *
	 * @param event	The event in Esper's native representation
	 * @return		The event in CSV representation
	 */
	public String toCSV(EventBean event) {
		StringBuilder sb = new StringBuilder("type:");
		sb.append(queryOutputName);
		if (eventFormat == EsperInterface.MAP_FORMAT) { // Input events are MAPs
			if (querySchema != null) {
				for (String att: querySchema.keySet()) {
					sb.append(Globals.CSV_SEPARATOR);
					sb.append(event.get(att));
				}
			}
		} else { //Input events are POJO
			try {
				for (Field f : Class.forName(queryOutputName).getFields()) {
					sb.append(Globals.CSV_SEPARATOR);
					sb.append(event.get(f.getName()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	@Override
	protected void disconnect() {
		if (query != null) {
			query.removeListener(this);
			query.stop();
		}

		disconnectFromAllSinks();
	}

	/**
	 * Sets the format used to exchange events with the Esper engine.
	 *
	 * @param eventFormat  the event format (either POJO or Map)
	 */
	public void setEventFormat(int eventFormat) {
		if (eventFormat == EsperInterface.POJO_FORMAT) {
			this.eventFormat = eventFormat;
		} else {
			this.eventFormat = EsperInterface.MAP_FORMAT;
		}
	}

	/**
	 *
	 * @return     the format used to exchange events with the Esper engine.
	 */
	public int getEventFormat() {
		return eventFormat;
	}

}
