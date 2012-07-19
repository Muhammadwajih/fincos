package pt.uc.dei.fincos.driver;

import java.io.IOException;
import java.util.LinkedHashMap;

import pt.uc.dei.fincos.basic.Attribute;
import pt.uc.dei.fincos.basic.Domain;
import pt.uc.dei.fincos.basic.Event;
import pt.uc.dei.fincos.basic.EventType;
import pt.uc.dei.fincos.basic.PredefinedListDomain;
import pt.uc.dei.fincos.data.CSVWriter;

/**
 * Class responsible for generating event's payload (Synthetic workload).
 *
 * @author Marcelo R.N. Mendes
 *
 */
public class DataGen {

    /** Used to choose the next event type to be generated according to event mix. */
    private PredefinedListDomain typeChooser;

    /** Random seed used when choosing the next event type to be generated according to event mix. */
    private Long mixSeed;

    /** Total number of events that must be generated. */
    private long totalEventCount;

    /** Total number of generated events so far. */
    private long generatedEvents = 0;

    /** A flag used to indicate if data generation must proceed. */
    private boolean keepGenerating = true;

    /**
     * Initializes DataGen's synthetic workload.
     *
     * @param workload  The synthetic workload specification
     */
    public DataGen(SyntheticWorkloadPhase workload) {
        this.totalEventCount = workload.getTotalEventCount();

        LinkedHashMap<EventType, Double> types = workload.getSchema();

        // Converts set of types to a format suitable for PredefinedListDomain (Object, Double)
        LinkedHashMap<Object, Double> objectTypes = new LinkedHashMap<Object, Double>(types.size());
        Double mix = 0.0;
        for (EventType type : types.keySet()) {
            mix = types.get(type);
            objectTypes.put(type, mix);
        }

        // Creates a  Domain to choose types iteratively according to the mix specified in the configuration file
        if (workload.isDeterministicEventMix()) { // Deterministic
            typeChooser = new PredefinedListDomain(objectTypes.keySet().toArray(new Object[0]));
        } else { // Stochastic
            mixSeed =  workload.getRandomSeed();
            typeChooser = new PredefinedListDomain(objectTypes, mixSeed);
        }
    }

    /**
     * Generates events according to a synthetic workload specified in the configuration file
     * and saves them into a data file.
     *
     * @param dataFilesDir      The directory where the data file(s) must be stored
     * @param fileCount         The number of files into which data will be stored
     * @throws IOException
     */
    public void generateData(String dataFilesDir, int fileCount) throws IOException {
        // clear stats
        this.generatedEvents = 0;

        // Used to create data file(s)
        CSVWriter[] writers;

        writers = new CSVWriter[fileCount];
        for (int i = 0; i < writers.length; i++) {
            writers[i] = new CSVWriter(dataFilesDir + "\\" + (i + 1) + ".csv", 10);
        }

        try {
            // Iterates over #events
            for (long j = 0; j < totalEventCount; j++) {
                if (keepGenerating) {
                    // Generates next event and writes it to disk
                    writers[(int)(j%fileCount)].writeRecord(getNextEvent());
                } else {
                    break;
                }
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
     * Generates the next event.
     *
     * @return  an event
     */
    public Event getNextEvent() {
        // Variables involved in data generation (the events, attributes, and their values)
        EventType type;
        Event ret;
        Attribute att;
        Domain d;
        Object value;

        synchronized (this) {
            this.generatedEvents++;
            if (generatedEvents > totalEventCount) {
                return null;
            }
        }

        // Choose a type according to the mix
        type = (EventType) typeChooser.generateValue(); // This method must be thread-safe

        // Iterates over attributes
        ret = new Event(type);
        for (int k = 0; k < type.getAttributes().length; k++) {
            att = type.getAttributes()[k];
            d = att.getDomain();
            if (d != null) {
                value = d.generateValue(); // This method must be thread-safe
                ret.setAttributeValue(k, value);
            }
        }

        return ret;
    }

    /**
     * Interrupts the generation of new events.
     */
    public void stopDataGeneration() {
        this.keepGenerating = false;

    }

    /**
     *
     * @return the number of events generated so far.
     */
    public long getGeneratedEventsCount() {
        return generatedEvents;
    }

    /**
     * Returns the overall progress of the data generation process.
     *
     * @return the ratio [generated events] / [events to generate]
     */
    public double getProgress() {
        return 1.0 * this.generatedEvents / this.totalEventCount;
    }
}
