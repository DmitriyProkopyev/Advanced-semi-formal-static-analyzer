package iu.sna.cli.validator;

import iu.sna.cli.FileType;
import iu.sna.cli.validator.ValidationException;

import lombok.Getter;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class Validator {

    @Getter
    private static final Validator instance = new Validator();

    private Validator() {
    }

    public void validate() {
        System.out.println("[INFO]: validations are passed");
    }

    public Validator notEqualPaths(Path path1, Path path2) {
        if (!Objects.equals(path1, path2) || path1 == null) {
            return instance;
        }

        throw new ValidationException("Path are the same for " +
                path1.toAbsolutePath() +
                ", " +
                path2.toAbsolutePath()
        );
    }

    public Validator pathExists(Path path) {
        Optional<Path> optionalPath = Optional.ofNullable(path);
        if (optionalPath
                .map(Path::toFile)
                .map(File::exists)
                .orElse(true)) {
            return instance;
        }

        throw new ValidationException("No file or directory for path " +
                optionalPath
                        .map(Path::toAbsolutePath)
                        .orElse(null)
        );
    }

    public Validator isDirectory(Path path) {
        Optional<Path> optionalPath = Optional.ofNullable(path);
        if (optionalPath
                .map(Path::toFile)
                .map(File::isDirectory)
                .orElse(true)
        ) {
            return instance;
        }

        throw new ValidationException("This is not a directory for path " +
                optionalPath
                        .map(Path::toAbsolutePath)
                        .orElse(null)
        );
    }

    public Validator isFile(Path path) {
        Optional<Path> optionalPath = Optional.ofNullable(path);
        if (optionalPath
                .map(Path::toFile)
                .map(File::isFile)
                .orElse(true)
        ) {
            return instance;
        }

        throw new ValidationException("This is not a file for path " +
                optionalPath
                        .map(Path::toAbsolutePath)
                        .orElse(null)
        );
    }

    public Validator isValidFileType(Path path) {
        if (path == null) {
            return instance;
        }

        String fileName = path.getFileName().toString();
        int pointIndex = fileName.lastIndexOf('.');
        if (pointIndex++ == -1) {
            throw new ValidationException("File type is incorrect for path " +
                    path.toAbsolutePath()
            );
        }

        FileType fileType = FileType.getFileType(fileName.substring(pointIndex).toLowerCase());
        if (fileType != null) {
            return instance;
        }

        throw new ValidationException("File type is incorrect for path " +
                path.toAbsolutePath()
        );
    }

    public Validator eitherParamOrFileApiKeys(String[] keys, Path fileKeys) {
        if (keys == null && fileKeys != null ||
                keys != null && fileKeys == null) {
            return instance;
        }
        throw new ValidationException("Specify API keys either through a file or parameter");
    }

    public Validator gitFolderExists(Path path) {
        if (path == null) {
            return instance;
        }

        Path git = path.resolveSibling(".git");
        if (git.toFile().exists()) {
            return instance;
        }

        throw new ValidationException("Git folder does not exist for path " +
                path.toAbsolutePath()
        );
    }
}
