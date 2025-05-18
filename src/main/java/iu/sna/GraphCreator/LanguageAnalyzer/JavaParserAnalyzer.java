package iu.sna.GraphCreator.LanguageAnalyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
public class JavaParserAnalyzer implements  LanguageAnalyzer{
  @Override
  public List<Map.Entry<Path, Path>> analyzeDependencies(
          List<String> fileString) throws IOException {
    StringBuffer dependencies = new StringBuffer();
    JavaParser parser = new JavaParser();
    List <Map.Entry<Path, Path>> res = new ArrayList<>();
    for (String filepath: fileString) {
      try {


        Path p1 = Paths.get(filepath);
        CompilationUnit cu = parser.parse(p1)
                .getResult()
                .get();
        for (ImportDeclaration imp : cu.getImports()) {
          String importName = imp.getNameAsString();
          importName = importName.replace(".", "/") + ".java";
          // looking for the file which ends with import name
          // java parser parses Java utils imports
          // we need to verify that file presents in input
          for (String targetFile : fileString) {
            if (targetFile.endsWith(importName)) {
              Path p2 = Paths.get(targetFile);
              res.add(Map.entry(p1, p2));
            }
          }
        }
      }catch (NoSuchFileException e) {
        System.out.println("No such file: " + e);
      }
    }

    return res;
  }

  @Override
  public String getLanguage() {
    return "java";
  }
}
