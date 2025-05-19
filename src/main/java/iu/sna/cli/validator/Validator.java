package iu.sna.cli.validator;

import iu.sna.cli.config.FileType;
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

    public Validator notEqualPaths(Path path, Path[] paths) {
        if (paths == null) {
            return instance;
        }

        for (Path path2 : paths) {
            if (!Objects.equals(path, path2) || path == null) {
                continue;
            }

            throw new ValidationException("Path are the same for " +
                    path.toAbsolutePath() +
                    ", " +
                    path2.toAbsolutePath()
            );
        }

        return instance;
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

    public Validator pathExists(Path[] paths) {
        if (paths == null) {
            return instance;
        }

        for (Path path : paths) {
            pathExists(path);
        }

        return instance;
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

    public Validator isValidFileType(Path[] paths) {
        if (paths == null) {
            return instance;
        }

        for (Path path : paths) {
            if (path == null) {
                continue;
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
                continue;
            }

            throw new ValidationException("File type is incorrect for path " +
                    path.toAbsolutePath()
            );
        }

        return instance;
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

        Path git = path.resolve(".git");
        if (git.toFile().exists()) {
            return instance;
        }

        throw new ValidationException("Git folder does not exist for path " +
                path.toAbsolutePath()
        );
    }
}
