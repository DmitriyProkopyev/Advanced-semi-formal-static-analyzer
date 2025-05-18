package iu.sna.application;

import iu.sna.application.llm_stages.ValidationBlock;
import iu.sna.domain.repository_scanner.FileTechnologyStack;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

public class Profile {
    public final String name;
    private final Collection<ValidationBlock> validationBlocks;

    public Profile(String name, Collection<ValidationBlock> validationBlocks) {
        this.name = name;
        this.validationBlocks = validationBlocks;
    }

    public static Profile loadFrom(File file) {
        // load and parse the profile from a given file
        return null;
    }

    public void saveInto(Path directory) {
        // save all the validation blocks and profile settings into a new file in given directory
        // filename = profile name
    }

    public Map<ValidationBlock, Collection<FileTechnologyStack>> mapOntoValidationBlocks(Collection<FileTechnologyStack> files) {
        // map files (many-to-many) to validation blocks based on their stack
        return null;
    }
}
