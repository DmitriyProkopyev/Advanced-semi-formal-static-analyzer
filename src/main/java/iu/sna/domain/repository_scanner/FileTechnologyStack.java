package iu.sna.domain.repository_scanner;

import java.io.File;
import java.util.Collection;

public record FileTechnologyStack(File file,
                                  String language,
                                  Collection<String> technologies) {}
