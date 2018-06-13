package depserver;

import com.google.gson.Gson;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Depserver {
    private final ServerSocketChannel listener;
    private Logger log = LoggerFactory.getLogger(Depserver.class);
    private Set<SocketChannel> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Gson gson = new Gson();

    private Depserver() throws IOException {
        listener = ServerSocketChannel.open();
        listener.bind(InetSocketAddress.createUnresolved("0.0.0.0", 1666));
        Thread t = new Thread(this::acceptLoop);
        t.setDaemon(true);
        t.start();

        Undertow.builder().addHttpListener(Integer.parseInt(System.getenv("PORT")), "0.0.0.0")
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

                }
                c.read(buf);
            } catch (IOException e) {
                log.warn("error recving", e);
                connections.remove(c);
            }
        }
    }

    public static void main(String args[]) throws IOException {
        new Depserver();
        System.out.println("Hello, world.");
    }
}
