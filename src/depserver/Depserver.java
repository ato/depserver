package depserver;

import com.google.gson.Gson;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Depserver {
    private final ServerSocketChannel listener;
    private Logger log = LoggerFactory.getLogger(Depserver.class);
    private Set<SocketChannel> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Gson gson = new Gson();
    private Set<Link> links = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private List<Record> tmpRecords = new ArrayList<>();


    private Depserver() throws IOException {
        listener = ServerSocketChannel.open();
        listener.bind(new InetSocketAddress("0.0.0.0", 1666));
        Thread t = new Thread(this::acceptLoop);
        t.setDaemon(true);
        t.start();

        HttpHandler handler = Handlers.routing()
                .get("/all", ex -> {
                    StringWriter sw = new StringWriter();
                    for (Record record: tmpRecords) {
                        sw.append(record.toString()).append('\n');
                    }
                    ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    ex.getResponseSender().send(sw.toString());
                })
                .get("/links", ex -> {
                    StringWriter sw = new StringWriter();
                    for (Link link: links) {
                        sw.append(link.toString()).append('\n');
                    }
                    ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    ex.getResponseSender().send(sw.toString());
                });
        Undertow.builder().addHttpListener(Integer.parseInt(System.getenv("PORT")), "0.0.0.0")
                .setHandler(handler)
                .build().start();
    }

    private void acceptLoop() {
        while (listener.isOpen()) {
            try {
                SocketChannel c = listener.accept();
                log.info("{} connected", c.getRemoteAddress());
                connections.add(c);
            } catch (IOException e) {
                log.warn("error accepting", e);
            }
        }
    }

    void poll() {
        Map<String,Record> all = new HashMap<>();
        Map<String,Record> servers = new HashMap<>();
        List<Record> records = new ArrayList<>();

        for (SocketChannel c: connections) {
            try {
                log.info("sending to {}", c.getRemoteAddress());
                c.write(ByteBuffer.wrap("sockets\n".getBytes(UTF_8)));
            } catch (IOException e) {
                log.warn("error sending", e);
                connections.remove(c);
            }
        }

        ByteBuffer buf = ByteBuffer.allocate(8192);
        for (SocketChannel c: connections) {
            try {
                log.info("recving from {}", c.getRemoteAddress());
                BufferedReader reader = new BufferedReader(Channels.newReader(c, "utf-8"));
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        connections.remove(c);
                        break;
                    } else if (line.isEmpty()) {
                        break;
                    }
                    Record record = gson.fromJson(line, Record.class);
                    if (record.type.equals("L")) continue; // TODO

                    record.clean();

                    if (record.type.equals("S")) {
                        servers.put(record.localaddr, record);
                    }
                    all.put(record.localaddr, record);
                    records.add(record);
                }
                c.read(buf);
            } catch (Exception e) {
                log.warn("error recving", e);
                connections.remove(c);
                try {
                    c.close();
                } catch (IOException e1) {}
            }
        }

        for (Record record: records) {
            Record peer = all.get(record.peeraddr);
            if (peer == null) continue;

            Link link;
            if (record.type.equals("C")) {
                link = new Link(record.host, record.service, peer.host, peer.service);
            } else {
                link = new Link(peer.host, peer.service, record.host, record.service);
            }
            links.add(link);
        }

        tmpRecords = records;
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        new Depserver().pollLoop();
        System.out.println("Hello, world.");
    }

    private void pollLoop() throws InterruptedException {
        while (listener.isOpen()) {
            poll();
            Thread.sleep(2000);
        }
    }
}
