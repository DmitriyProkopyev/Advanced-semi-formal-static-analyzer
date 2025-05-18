package iu.sna.infrastructure;

import java.util.List;
import java.util.Map;

public class Runner {
    public static void main(String[] args) throws Exception {
        String python = "python3";
        String script = "src/main/python/python_wrapper.py";

        Wrapper w = new Wrapper(python, script);

        List<Map<String, Object>> stats = w.token(List.of("text1.md", "text2.txt"));
        System.out.println("Token stats: " + stats);

        List<List<String>> clusters = w.cluster("graph.json");
        System.out.println("Clusters: " + clusters);

        boolean ok = w.convert("input.md", "output.pdf");
        System.out.println("Conversion succeeded: " + ok);
    }
}
