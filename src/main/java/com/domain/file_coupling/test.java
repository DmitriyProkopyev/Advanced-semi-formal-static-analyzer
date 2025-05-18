package com.domain.file_coupling;

import java.io.File;
import java.util.Collection;

public class test {
  public static void main(String[] st) {
    DependencyGraph g = new DependencyGraph((Collection<File>) new File("asd"));
  }
}
