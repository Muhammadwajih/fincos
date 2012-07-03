package pt.uc.dei.fincos.basic;

/** A representation for events read from data files. */
public class CSV_Event {

    /** The name of the type of this record. */
    private String type;

    /** The timestamp of this record. */
    private long timestamp;

    /** The payload of this record. */
    private String[] payload;


    /**
     *
     * @param type      The name of the type of this record
     * @param timestamp The timestamp of this record
     * @param payload   The payload of this record
     */
    public CSV_Event(String type, long timestamp, String[] payload) {
        this.setType(type);
        this.setTimestamp(timestamp);
        this.setPayload(payload);
    }


    public void setType(String type) {
        this.type = type;
    }


    public String getType() {
        return type;
    }


    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public long getTimestamp() {
        return timestamp;
    }


    public void setPayload(String[] payload) {
        this.payload = payload;
    }


    public String[] getPayload() {
        return payload;
    }

    /**
    *
    * @return  the event's data as a CSV record
    */
   public String toCSV() {
       StringBuilder sb = new StringBuilder("type:");
       sb.append(this.getType());

       for (String attributeValue : payload) {
           sb.append(Globals.CSV_SEPARATOR);
           sb.append(attributeValue.toString());
       }

       return sb.toString();
   }
}
