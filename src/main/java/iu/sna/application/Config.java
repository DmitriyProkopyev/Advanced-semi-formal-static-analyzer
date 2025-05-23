package iu.sna.application;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Config {
    public static final Path profilesDirectory = Paths.get("src/main/resources/profiles");
    public static final Path tempDirectory = Paths.get("src/main/resources/temp");
    public static final File pythonRequirements = new File("requirements.txt");
    public static final File graphConfig;

    public static final String python;
    public static final Path pythonWrapper;

    public static final int maxClusters;
    public static final double targetOutputToInputProportion;
    public static final JSONObject gemini;

    public static final List<String> NFRs;
    public static final JSONObject NFRMapping;
    public static final double priorityFactor;

    public static final boolean developerMode = false;

    static {
        var appConfigPath = Paths.get("src/main/resources/config/git-warden-config.json");
        File configFile = appConfigPath.toFile();
        JSONObject appConfig;

        try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
            appConfig = new JSONObject(new JSONTokener(fileInputStream));
        } catch (IOException exception) {
            throw new RuntimeException("Config file not found: " + configFile.getAbsolutePath(), exception);
        }
        catch (JSONException exception) {
            throw new RuntimeException("Error reading config file: " + configFile.getAbsolutePath(), exception);
        }

        python = appConfig.getString("python");
        pythonWrapper = Paths.get("src/main/python/python_wrapper.py");
        graphConfig = Paths.get("src/main/python/graph.json").toFile();
        maxClusters = appConfig.getInt("max_clusters");
        targetOutputToInputProportion = appConfig.getDouble("target_output_to_input_proportion");
        gemini = appConfig.getJSONObject("gemini");

        var nfrTaxonomyPath = Paths.get("src/main/resources/config/NFRTaxonomy.json");
        var taxonomy = nfrTaxonomyPath.toFile();
        JSONObject taxonomyConfig;

        try (FileInputStream fileInputStream = new FileInputStream(taxonomy)) {
            taxonomyConfig = new JSONObject(new JSONTokener(fileInputStream));
        } catch (IOException exception) {
            throw new RuntimeException("Taxonomy file not found: " + configFile.getAbsolutePath(), exception);
        }
        catch (JSONException exception) {
            throw new RuntimeException("Error reading taxonomy file: " + configFile.getAbsolutePath(), exception);
        }

        var nfrs = new ArrayList<String>();
        var taxonomyArray = taxonomyConfig.getJSONArray("NFRs");
        for (int i = 0; i < taxonomyArray.length(); i++)
            nfrs.add(taxonomyArray.getString(i));

        NFRs = nfrs;
        NFRMapping = taxonomyConfig.getJSONObject("mapping");
        priorityFactor = appConfig.getDouble("priority_factor");
    }
}
