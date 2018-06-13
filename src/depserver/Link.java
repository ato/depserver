package depserver;

import java.util.Objects;

public class Link {
    final String chost;
    final String cservice;
    final String shost;
    final String sservice;

    public Link(String chost, String cservice, String shost, String sservice) {
        this.chost = chost;
        this.cservice = cservice;
        this.shost = shost;
        this.sservice = sservice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(chost, link.chost) &&
                Objects.equals(cservice, link.cservice) &&
                Objects.equals(shost, link.shost) &&
                Objects.equals(sservice, link.sservice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chost, cservice, shost, sservice);
    }

    @Override
    public String toString() {
        return "Link{" +
                "chost='" + chost + '\'' +
                ", cservice='" + cservice + '\'' +
                ", shost='" + shost + '\'' +
                ", sservice='" + sservice + '\'' +
                '}';
    }

    public String client() {
        return chost + " " + cservice;
    }

    public String server() {
        return shost + " " + sservice;
    }
}
