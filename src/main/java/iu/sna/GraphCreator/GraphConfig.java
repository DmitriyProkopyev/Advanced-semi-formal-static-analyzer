package iu.sna.GraphCreator;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Spring configuration class for graph-related beans.
 * This class is responsible for:
 * 1. Creating and configuring the Git repository bean
 * 2. Setting up the GitCommitParser
 * 3. Managing project-wide configuration values.
 *
 * <p>The configuration values are automatically loaded from application.yaml:
 * - project.root: The root directory of the project to analyze
 * - constants.location_value_coefficient: Coefficient for location-based calculations
 * - constants.commit_importance_coefficient: Weight coefficient for commit importance
 *
 * @author Your Name
 * @version 1.0
 */
@Configuration
public class GraphConfig {
  private static final Logger logger =
          LoggerFactory.getLogger(GraphConfig.class);

  /**
   * The root directory of the project to analyze.
   * Defaults to the current working directory if not specified.
   */
  @Value("${project.root:${user.dir}}")
  private String projectRoot;

  /**
   * Creates and configures a Git repository instance for the project.
   * The repository is initialized from the .git directory in the project root.
   *
   * @return Configured Git Repository instance
   * @throws IOException if the repository cannot be accessed or .git directory is not found
   */
  @Bean
  public Repository gitRepository() throws IOException {
    if (projectRoot == null || projectRoot.isEmpty()) {
      projectRoot = System.getProperty("user.dir");
    }

    Path projectPath = Paths.get(projectRoot);


    File gitDir = new File(projectRoot + "/.git");

    if (!gitDir.exists()) {
      throw new IOException(
              "Git repository not found at " + gitDir.getAbsolutePath() +
                      ". Make sure the project.root property points to a Git repository.");
    }
    return new RepositoryBuilder()
            .setGitDir(gitDir)
            .build();
  }

  /**
   * Creates a GitCommitParser instance that will be used to analyze
   * the commit history of the project.
   *
   * @param repository The Git repository to analyze
   * @return Configured GitCommitParser instance
   */
  @Bean
  public GitCommitParser gitCommitParser(Repository repository) {
    return new GitCommitParser(repository);
  }
} 