package depserver;

public class Record {
    public String host;
    public String type;
    public String localaddr;
    public String peeraddr;
    public String service;

    void clean() {
        localaddr = clean(localaddr);
        peeraddr = clean(peeraddr);
    }

    String clean(String addr) {
        if (addr.startsWith("::ffff:")) {
            addr = addr.substring("::ffff:".length());
        }
        if (addr.startsWith("127.0.0.1:") || addr.startsWith(":::")) {
            addr = host + "_" + addr;
        }
        return addr;
    }

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
