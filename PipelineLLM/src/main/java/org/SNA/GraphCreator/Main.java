package org.SNA.GraphCreator;

import java.io.IOException;

public class Main {

  final static String cpp = "/home/aziz/Projects/D-Bus-Configuration-Managed";
  final static String java = "/home/aziz/Projects/Advanced-semi-formal-static-analyzer/PipelineLLM/src";
  public static void main(final String[] qwe) throws IOException {
    GraphBuilder graphBuilder = new GraphBuilder(cpp);
    graphBuilder.parseFiles();
  }

}
