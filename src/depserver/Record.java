package depserver;

public class Record {
    public String type;
    public String localaddr;
    public String peeraddr;
    public String service;

    @Override
    public String toString() {
        return "Record{" +
                "type='" + type + '\'' +
                ", localaddr='" + localaddr + '\'' +
                ", peeraddr='" + peeraddr + '\'' +
                ", service='" + service + '\'' +
                '}';
    }
}
