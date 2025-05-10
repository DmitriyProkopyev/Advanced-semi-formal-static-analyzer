package iu.sna.GraphCreator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Parse input git repository.
 * Has methods to find changed files
 * in commits
 */
public final class GitCommitParser implements GitUtils {
  /**
   * Wrapper of path to .git directory.
   */
  private final Repository repository;

  /**
   * Initialize repository
   * which will be parsed.
   *
   * @param inputRepository - wrapper path for .git directory
   */
  public GitCommitParser(final Repository inputRepository) {
    this.repository = inputRepository;

  }

  /**
   * Launch iteration over first N commits
   * For each commit call getChangedFilesInCommit.
   *
   * @param numberOfCommits number of commits to be analyzed
   * @return commitID:List of changed files
   */
  public Map<String, List<Path>> getChangeFilesInFirstNcommits(
          final int numberOfCommits) {
    Map<String, List<Path>> result = new LinkedHashMap<>();
    try (Git git = new Git(this.repository)) {
      Iterable<RevCommit> commits = git.log()
              .setMaxCount(numberOfCommits)
              .call();
      for (RevCommit commit : commits) {
        List<Path> changedFiles = getChangedFilesInCommit(

                commit.getId()
        );
        result.put(commit.getName(), changedFiles);
      }

    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  /**
   * Analyze changed files in commit.
   *
   * @param commitId commit id
   * @return List of changed files in the commit
   */
  public List<Path> getChangedFilesInCommit(
          final ObjectId commitId) {

    try (RevWalk revWalk = new RevWalk(this.repository)) {

      // Fetching commit
      RevCommit commit = revWalk.parseCommit(commitId);

      // check if commit has parent
      RevCommit parentCommit = commit.getParentCount() > 0
              // get parent commit
              ? revWalk
              .parseCommit(commit.getParent(0)
                      .getId())
              // otherwise it is first commit
              : null;

      // creating reader to
      // analyze objects in repository
      try (ObjectReader reader = this.repository.newObjectReader()) {
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();

        // create parser for commit
        newTreeIter.reset(reader, commit.getTree());

        // creat parser for parent commit
        // if exist
        CanonicalTreeParser oldTreeIter = null;
        if (parentCommit != null) {
          oldTreeIter = new CanonicalTreeParser();
          oldTreeIter.reset(reader, parentCommit.getTree());
        }


        try (Git git = new Git(this.repository)) {
          List<DiffEntry> diffs = git.diff()
                  .setOldTree(oldTreeIter)
                  .setNewTree(newTreeIter)
                  .call();

          return diffs.stream()
                  .map(diff -> {
                    String path = switch (diff.getChangeType()) {
                      // if file was deleted
                      // fetch path from parent commit
                      case DELETE -> diff.getOldPath();
                      // otherwise fetch
                      // file from child commti
                      default -> diff.getNewPath();
                    };
                    return path != null ? Paths.get(path) : null;
                  })
                  .filter(Objects::nonNull)
                  .toList();

        } catch (GitAPIException e) {
          throw new RuntimeException("Git diff failed", e);
        }
      }

    } catch (IOException e) {
      throw new RuntimeException("Error reading commit", e);
    }
  }
}

