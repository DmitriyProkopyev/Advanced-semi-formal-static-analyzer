package iu.sna.application;

import iu.sna.application.llm_stages.ValidationBlock;
import iu.sna.domain.repository_scanner.FileTechnologyStack;
import iu.sna.infrastructure.LLM;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Profile {
    public final String name;
    private final Collection<ValidationBlock> validationBlocks;

    private static final String languages = "languages";
    private static final String technologies = "technologies";
    private static final String standards = "standards";
    private static final String instructions = "instructions";

    public Profile(String name, Collection<ValidationBlock> validationBlocks) {
        this.name = name;
        this.validationBlocks = validationBlocks;
    }

    public static Profile loadFrom(File file, LLM llm) {
        JSONObject profileConfig;

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            profileConfig = new JSONObject(new JSONTokener(fileInputStream));
        } catch (IOException exception) {
            throw new RuntimeException("Profile config file not found: " + file.getAbsolutePath(), exception);
        } catch (JSONException exception) {
            throw new RuntimeException("Error reading profile config file: " + file.getAbsolutePath(), exception);
        }

        String profileName = profileConfig.getString("name");
        JSONArray validationBlocks = profileConfig.getJSONArray("validation_blocks");

        var decodedBlocks = new ArrayList<ValidationBlock>();
        for (int i = 0; i < validationBlocks.length(); i++) {
            var block = validationBlocks.getJSONObject(i);
            var blockName = block.getString("name");
            var blockLanguages = new HashSet<>(block.getJSONArray(languages).
                    toList().stream().map(Object::toString).toList());
            var blockTechnologies = new HashSet<>(block.getJSONArray(technologies).
                    toList().stream().map(Object::toString).toList());
            var blockStandards = block.getString(standards);
            var blockInstructions = block.getString(instructions);

            var decodedBlock = new ValidationBlock(blockName,
                    blockLanguages,
                    blockTechnologies,
                    blockStandards,
                    llm,
                    blockInstructions);
            decodedBlocks.add(decodedBlock);
        }

        return new Profile(profileName, decodedBlocks);
    }

    public void saveInto(Path directory) throws IOException {
        var blocks = new JSONArray();
        var file = directory.resolve(this.name);

        for (var block : this.validationBlocks) {
            var encodedBlock = new JSONObject();
            encodedBlock.put("name", block.name);
            encodedBlock.put(languages, block.languages);
            encodedBlock.put(technologies, block.technologies);
            encodedBlock.put(standards, block.standards);
            encodedBlock.put(instructions, block.instructions);

            blocks.put(encodedBlock);
        }

        var encodedProfile = new JSONObject();
        encodedProfile.put("name", this.name);
        encodedProfile.put("validation_blocks", blocks);

        Files.write(file, encodedProfile.toString().getBytes(StandardCharsets.UTF_8));
    }

    public Map<ValidationBlock, Collection<FileTechnologyStack>> mapOntoValidationBlocks(Collection<FileTechnologyStack> files) {
        var result = new HashMap<ValidationBlock, Collection<FileTechnologyStack>>();

        /*
        for (var block : validationBlocks) {
            var chosenFiles = new ArrayList<FileTechnologyStack>();
            int technologiesCount = block.technologies.size();

            for (var file : files) {
                if (!block.languages.contains(file.language()))
                    continue;

                long intersectionSize = file.technologies()
                        .stream()
                        .filter(block.technologies::contains)
                        .count();

                if (intersectionSize * 2 >= technologiesCount)
                    chosenFiles.add(file);
            }

            result.put(block, chosenFiles);
        }

         */

        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        for (var block : validationBlocks) {
            result.put(block, files);
        }
        return result;
    }
}
