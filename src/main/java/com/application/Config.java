package com.application;

import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    public static final Path NFRTaxonomy = Paths.get("src/main/resources/config/NFRTaxonomy.json");
    public static final Path profilesDirectory = Paths.get("src/main/resources/profiles");
    public static final Path tempDirectory = Paths.get("src/main/resources/temp");

    public static final int maxClusters;
    public static final double targetOutputToInputProportion;
    public static final JSONObject gemini;

    static {
        JSONObject appConfig;

        try {
            appConfig = new JSONObject(new FileInputStream("src/main/resources/config/git-warden-config.json"));
        } catch (FileNotFoundException exception) {
            throw new RuntimeException(exception);
        }

        maxClusters = appConfig.getInt("max_clusters");
        targetOutputToInputProportion = appConfig.getDouble("target_output_to_input_proportion");
        gemini = appConfig.getJSONObject("gemini");
    }
}
