package com.application;

import com.infrastructure.Tree;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

public interface ApplicationFacade {
    Collection<String> readNFRs();

    Flux<String> createProfile(String name,
                               Collection<Path> context,
                               Tree<Path> filteredDirectories,
                               Iterable<Integer> priorities);

    Flux<String> scan(String profileName,
                      Tree<Path> filteredDirectories,
                      File reportTargetLocation);
}
