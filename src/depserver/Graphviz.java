package depserver;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

public class Graphviz {
    static void build(Set<Link> links, Writer out) throws IOException {
        out.write("digraph services {\n" +
                "  node [color=lightblue2, style=filled];\n"
        );

        for (Link link: links) {
            out.write("\"" + link.client() + "\" -> " + link.server() + "\";\n");
        }
        out.write("\n");
    }
}
