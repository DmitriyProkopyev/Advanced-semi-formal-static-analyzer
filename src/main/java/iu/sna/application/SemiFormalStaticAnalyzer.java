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
import reactor.core.publisher.FluxSink;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
    public Flux<String> createProfile(String name,
                                      Collection<Path> context,
                                      Tree<Path> filteredDirectories,
                                      Iterable<Integer> priorities) {
        return Flux.create(emitter -> new Thread(() -> {
            try {
                emitter.next("User priorities understood, inferring best fit practices...");
                var abstractStandards = taxonomyMap.unpackNFRSequence(priorities);
                emitter.next("Best fit practices inference complete.");

                emitter.next("Analyzing the technology stack of the given repository...");
                var scanner = new RepositoryScanner(filteredDirectories);
                var technologyStack = scanner.scan();
                var languages = scanner.getAllLanguages();
                emitter.next("Used languages: " + String.join(",", languages));
                var technologies = scanner.getAllTechnologies();
                emitter.next("Used technologies: " + String.join(",", technologies));
                emitter.next("Repository analysis complete.");

                CrossReferenceStandardGenerator standardGenerator;
                if (!context.isEmpty()) {
                    emitter.next("Analyzing user context...");
                    standardGenerator = new CrossReferenceStandardGenerator(this.llm, abstractStandards, context);
                    emitter.next("Analysis complete, user context is accounted for.");
                } else {
                    standardGenerator = new CrossReferenceStandardGenerator(abstractStandards, this.llm);
                }

                emitter.next("Preparing language-specific standards...");
                var languagesToStandards = standardGenerator.generateFor(languages);
                emitter.next("Language-specific standards inference complete.");
                emitter.next("Preparing technology-specific standards...");
                var technologiesToStandards = standardGenerator.generateFor(technologies);
                emitter.next("Technology-specific standards inference complete.");

                emitter.next("Grouping standards into validation blocks...");
                var validationBlockBuilder = new ValidationBlockBuilder(technologyStack);
                var validationBlocks = validationBlockBuilder.buildFrom(languagesToStandards, technologiesToStandards);
                emitter.next("Standards grouping and instructions inference complete.");

                emitter.next("Finalizing profile setup...");
                var profile = new Profile(name, validationBlocks);
                profile.saveInto(Config.profilesDirectory);
                profiles.put(profile.name, profile);
                emitter.next("Profile created successfully!");
                emitter.complete();
            } catch (Exception exception) {
                emitter.next("Process interrupted due to a critical error...");
                emitter.error(exception);
            }
        }).start(), FluxSink.OverflowStrategy.BUFFER);
    }

    // include profile updates later if possible
    @Override
    public Flux<String> scan(String profileName,
                             Tree<Path> filteredDirectories,
                             File reportTargetLocation) {
        return Flux.create(emitter -> new Thread(() -> {
            try {
                emitter.next("Loading the project profile...");
                Profile profile;
                if (profiles.containsKey(profileName))
                    profile = profiles.get(profileName);
                else
                    profile = Profile.loadFrom(Config.profilesDirectory.resolve(profileName).toFile());
                emitter.next("Profile loading complete.");

                emitter.next("Scanning the repository for updates...");
                var scanner = new RepositoryScanner(filteredDirectories);
                var technologyStack = scanner.scan();

                emitter.next("Updates acquired, mapping the project files to validation blocks...");
                var mapping = profile.mapOntoValidationBlocks(technologyStack);
                emitter.next("Mapping complete.");

                emitter.next("Primary scanning started...");
                var criticism = new HashMap<ValidationBlock, String>();
                for (var validationBlock : mapping.keySet()) {
                    emitter.next("Analysing " + validationBlock.name + "...");
                    var files = mapping.get(validationBlock);
                    emitter.next("Constructing dependency graph for " + validationBlock.name + "...");
                    var graph = new DependencyGraph(files);
                    var operator = new DependencyGraphOperator(graph);
                    emitter.next("Dependency graph preparation complete.");

                    emitter.next("Performing context clusterization for " + validationBlock.name + "...");
                    int maxInput = (int) (this.llmType.maxOutputTokens * Config.targetOutputToInputProportion);
                    int upperBound = (int) (this.llmType.maxInputTokens / (Config.targetOutputToInputProportion + 1.0));
                    maxInput = Math.min(maxInput, upperBound);

                    var clusters = operator.extractClusters(maxInput, Config.maxClusters);
                    emitter.next("Context clusterization complete, analyzing clusters...");

                    var responses = new ArrayList<String>();
                    int index = 0;
                    for (var cluster : clusters) {
                        index++;
                        emitter.next("Analyzing cluster " + index + "...");
                        var response = validationBlock.applyOn(cluster);
                        responses.add(response);
                        emitter.next("Cluster " + index + " analysis complete.");
                    }

                    emitter.next("Separate clusters analysis complete.");
                    emitter.next("Unifying cluster analysis results for " + validationBlock.name + "...");
                    var unifiedCriticism = validationBlock.unify(responses);
                    criticism.put(validationBlock, unifiedCriticism);
                    emitter.next("Analysis of " + validationBlock.name + " complete.");
                }

                emitter.next("Analysis complete, building the final report...");
                var reportGenerator = new ReportGenerator("\n\n## ",
                        "\n\n", Config.tempDirectory);

                var datetime = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").
                        format(LocalDateTime.now());
                var reportName = String.format("Project %s quality report from %s",
                        profileName, datetime);
                var report = reportGenerator.generateReport(reportName, criticism);

                var pdfBuilder = new PDFBuilder();
                pdfBuilder.fromMarkdown(report, reportTargetLocation);
                emitter.next("Quality report successfully generated at " + reportTargetLocation.getAbsolutePath() + "!");
                emitter.complete();
            } catch (Exception exception) {
                emitter.next("Process interrupted due to a critical error...");
                emitter.error(exception);
            }
        }).start(), FluxSink.OverflowStrategy.BUFFER);
    }
}
