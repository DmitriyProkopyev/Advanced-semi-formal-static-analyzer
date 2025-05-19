package iu.sna.cli.config;

import iu.sna.application.ApplicationFacade;
import iu.sna.application.SemiFormalStaticAnalyzer;
import iu.sna.cli.validator.ValidationException;
import iu.sna.infrastructure.LLM;
import iu.sna.infrastructure.LLMType;
import iu.sna.infrastructure.Tree;
import iu.sna.infrastructure.Tree.TreeNode;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@UtilityClass
public class CommandUtils {

    private final String SETUP_DIRECTORY = "src/main/resources/setup";
    private final String API_KEYS_FILE = "api_keys.txt";
    private final String SETUP_TYPE_FILE = "setup_type.txt";

    public ApplicationFacade createApplicationFacade(SetupType setupType, Collection<String> keys) {
        return new SemiFormalStaticAnalyzer(
                LLMType.valueOf(setupType.name()),
                new LLM(keys, LLMType.valueOf(setupType.name()))
        );
    }

    public void setApiKeys(String[] keys, Path fileKeys) throws IOException {
        Path resourcesPath = Paths.get(SETUP_DIRECTORY);
        Path apiKeysPath = resourcesPath.resolve(API_KEYS_FILE);

        if (!Files.exists(resourcesPath)) {
            Files.createDirectories(resourcesPath);
        }

        if (keys != null && keys.length > 0) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(apiKeysPath.toFile()))) {
                for (String key : keys) {
                    writer.write(key);
                    writer.newLine();
                }
            }
        } else if (fileKeys != null) {
            Files.copy(fileKeys, apiKeysPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public SetupType getSetupType() throws IOException {
        Path setupTypePath = Paths.get(SETUP_DIRECTORY, SETUP_TYPE_FILE);

        if (!Files.exists(setupTypePath)) {
            throw new ValidationException("No " + SETUP_TYPE_FILE + " exists");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(setupTypePath.toFile()))) {
            String typeString = reader.readLine();
            if (typeString != null && !typeString.isEmpty()) {
                return SetupType.valueOf(typeString);
            }
        }

        throw new ValidationException("SetupType is null");
    }

    public void setSetupType(SetupType type) throws IOException {
        Path resourcesPath = Paths.get(SETUP_DIRECTORY);
        Path setupTypePath = resourcesPath.resolve(SETUP_TYPE_FILE);

        if (!Files.exists(resourcesPath)) {
            Files.createDirectories(resourcesPath);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(setupTypePath.toFile()))) {
            writer.write(type.name());
        }
    }

    public List<String> getApiKeys() throws IOException {
        Path apiKeysPath = Paths.get(SETUP_DIRECTORY, API_KEYS_FILE);
        List<String> keys = new ArrayList<>();

        if (!Files.exists(apiKeysPath)) {
            throw new ValidationException("No " + API_KEYS_FILE + " exists");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(apiKeysPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    keys.add(line.trim());
                }
            }
        }

        return keys;
    }

    public static Tree<Path> createDirectoryTree(Path repository, Path[] directories) {
        Tree<Path> tree = new Tree<>(repository);
        if (directories == null) {
            return tree;
        }

        for (Path directory : directories) {
            Path currentPath = directory;
            TreeNode<Path> currentNode = null;

            while (currentPath != null && !currentPath.equals(repository)) {
                final Path pathToFind = currentPath;
                TreeNode<Path> existingNode = tree.findNode(path -> path.equals(pathToFind));

                if (existingNode != null) {
                    currentNode = existingNode;
                } else {
                    if (currentNode == null) {
                        currentNode = tree.addChild(tree.getRoot(), currentPath);
                    } else {
                        TreeNode<Path> parentNode = tree.addChild(tree.getRoot(), currentPath);
                        tree.removeNode(currentNode);
                        tree.addChild(parentNode, currentNode.getData());
                        currentNode = parentNode;
                    }
                }

                currentPath = currentPath.getParent();
            }
        }

        return tree;
    }
}
