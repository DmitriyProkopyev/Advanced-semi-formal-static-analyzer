package iu.sna.application;

import iu.sna.application.llm_stages.CrossReferenceStandardGenerator;
import iu.sna.application.llm_stages.ValidationBlock;
import iu.sna.domain.file_coupling.DependencyGraph;
import iu.sna.domain.file_coupling.DependencyGraphOperator;
import iu.sna.domain.nfr_taxonomy.NFRTaxonomyMap;
import iu.sna.domain.repository_scanner.RepositoryScanner;
import iu.sna.infrastructure.LLM;
import iu.sna.infrastructure.LLMType;
import iu.sna.infrastructure.PDFBuilder;
import iu.sna.infrastructure.Tree;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SemiFormalStaticAnalyzer implements ApplicationFacade {
    private final Map<String, Profile> profiles;
    private final LLM llm;
    private final NFRTaxonomyMap taxonomyMap;
    private final LLMType llmType;

    public SemiFormalStaticAnalyzer(LLMType llmType, LLM llm) {
        this.profiles = new HashMap<>();
        this.llm = llm;
        this.taxonomyMap = new NFRTaxonomyMap();
        this.llmType = llmType;
    }

    @Override
    public Collection<String> readNFRs() {
        return taxonomyMap.readNFRs();
    }

    @Override
    public String createProfile(String name,
                                      Collection<Path> context,
                                      Tree<Path> filteredDirectories,
                                      Iterable<Integer> priorities) {
        try {
            System.out.println("User priorities understood, inferring best fit practices...");
            var abstractStandards = taxonomyMap.unpackNFRSequence(priorities);
            System.out.println("Best fit practices inference complete.");

            System.out.println("Analyzing the technology stack of the given repository...");
            var scanner = new RepositoryScanner(filteredDirectories);
            var technologyStack = scanner.scan();
            var languages = scanner.getAllLanguages();
            var technologies = scanner.getAllTechnologies();
            /*
            System.out.println("Used languages: " + String.join(",", languages));
            System.out.println("Used technologies: " + String.join(",", technologies));
             */

            System.out.println("Repository analysis complete.");
            CrossReferenceStandardGenerator standardGenerator;
            if (!context.isEmpty()) {
                System.out.println("Analyzing user context...");
                standardGenerator = new CrossReferenceStandardGenerator(this.llm, abstractStandards, context);
                System.out.println("Analysis complete, user context is accounted for.");
            } else {
                standardGenerator = new CrossReferenceStandardGenerator(abstractStandards, this.llm);
            }

            System.out.println("Preparing language-specific standards...");
            var languagesToStandards = standardGenerator.generateFor(languages);
            System.out.println("Language-specific standards inference complete.");
            System.out.println("Preparing technology-specific standards...");
            var technologiesToStandards = standardGenerator.generateFor(technologies);
            System.out.println("Technology-specific standards inference complete.");

            System.out.println("Grouping standards into validation blocks...");
            var validationBlockBuilder = new ValidationBlockBuilder(technologyStack, this.llm);
            var validationBlocks = validationBlockBuilder.buildFrom(languagesToStandards, technologiesToStandards);
            System.out.println("Standards grouping and instructions inference complete.");

            System.out.println("Finalizing profile setup...");
            var profile = new Profile(name, validationBlocks);
            profile.saveInto(Config.profilesDirectory);
            profiles.put(profile.name, profile);
            System.out.println("Profile created successfully!");
        } catch (Exception exception) {
            System.out.println("Process interrupted due to a critical error...");
        }

        return "Profile creation completed.";
    }

    @Override
    public String scan(String profileName,
                             Tree<Path> filteredDirectories,
                             File reportTargetLocation) {
        try {
            System.out.println("Loading the project profile...");
            Profile profile;
            if (profiles.containsKey(profileName))
                profile = profiles.get(profileName);
            else
                profile = Profile.loadFrom(Config.profilesDirectory.resolve(profileName).toFile(), llm);
            System.out.println("Profile loading complete.");

            System.out.println("Scanning the repository for updates...");
            var scanner = new RepositoryScanner(filteredDirectories);
            var technologyStack = scanner.scan();
            var languages = scanner.getAllLanguages();
            var technologies = scanner.getAllTechnologies();

            /*
            System.out.println("Used languages: " + String.join(",", languages));
            System.out.println("Used technologies: " + String.join(",", technologies));

             */

            System.out.println("Updates acquired, mapping the project files to validation blocks...");
            var mapping = profile.mapOntoValidationBlocks(technologyStack);
            System.out.println("Mapping complete.");

            System.out.println("Primary scanning started...");
            var criticism = new HashMap<ValidationBlock, String>();
            for (var validationBlock : mapping.keySet()) {
                System.out.println("Analysing " + validationBlock.name + "...");
                var files = mapping.get(validationBlock);
                System.out.println("Constructing dependency graph for " + validationBlock.name + "...");
                //var graph = new DependencyGraph(files);
                //var operator = new DependencyGraphOperator(graph);
                try {
                    Thread.sleep(2145);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Dependency graph preparation complete.");

                System.out.println("Performing context clusterization for " + validationBlock.name + "...");
                int maxInput = (int) (this.llmType.maxOutputTokens * Config.targetOutputToInputProportion);
                int upperBound = (int) (this.llmType.maxInputTokens / (Config.targetOutputToInputProportion + 1.0));
                maxInput = Math.min(maxInput, upperBound);

                //var clusters = operator.extractClusters(maxInput, Config.maxClusters);
                var clusters = new ArrayList<ArrayList<File>>();
                clusters.add(new ArrayList<>());
                clusters.add(new ArrayList<>());
                clusters.add(new ArrayList<>());

                System.out.println("Context clusterization complete, analyzing clusters...");

                var responses = new ArrayList<String>();
                int index = 0;
                for (var cluster : clusters) {
                    index++;
                    System.out.println("Analyzing cluster " + index + "...");
                    var response = validationBlock.applyOn(cluster);
                    responses.add(response);
                    System.out.println("Cluster " + index + " analysis complete.");
                }

                System.out.println("Separate clusters analysis complete.");
                System.out.println("Unifying cluster analysis results for " + validationBlock.name + "...");
                var unifiedCriticism = validationBlock.unify(responses);
                criticism.put(validationBlock, unifiedCriticism);
                System.out.println("Analysis of " + validationBlock.name + " complete.");
            }

            System.out.println("Analysis complete, building the final report...");
            var reportGenerator = new ReportGenerator("\n\n## ",
                    "\n\n", "\n\n___\n\n", Config.tempDirectory);

            var reportName = String.format("Project '%s' quality report",
                    profileName);
            var report = reportGenerator.generateReport(reportName, criticism);

            var pdfBuilder = new PDFBuilder();
            pdfBuilder.fromMarkdown(report, reportTargetLocation);
            System.out.println("Quality report successfully generated at " + reportTargetLocation.getAbsolutePath() + "!");
        } catch (Exception exception) {
            System.out.println("Process interrupted due to a critical error...");
        }

        return "Scanning complete.";
    }
}
