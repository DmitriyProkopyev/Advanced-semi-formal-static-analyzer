package iu.sna.application;

import iu.sna.infrastructure.Tree;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

public interface ApplicationFacade {
    Collection<String> readNFRs();

    String createProfile(String name,
                               Collection<Path> context,
                               Tree<Path> filteredDirectories,
                               Iterable<Integer> priorities);

    String scan(String profileName,
                      Tree<Path> filteredDirectories,
                      File reportTargetLocation);
}
