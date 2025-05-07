package org.SNA.GraphCreator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
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

public class GitUtils {

  /**
   * @param repository path to .git folder
   * @param n          number of commits to be analyze
   * @return commitID:List of changed files
   */
  public static Map<String, List<Path>> getChangeFilesInFirstNCommits(
          final Repository repository, final int n) {
    Map<String, List<Path>> result = new LinkedHashMap<>();

    try (Git git = new Git(repository)) {
      Iterable<RevCommit> commits = git.log()
              .setMaxCount(n)
              .call();
      for (RevCommit commit : commits) {
        List<Path> changedFiles = getChangedFilesInCommit(
                repository,
                commit.getName()
        );
        result.put(commit.getName(), changedFiles);
      }

    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  public static List<Path> getChangedFilesInCommit(
          final Repository repository, final String commitId) {

    try (RevWalk revWalk = new RevWalk(repository)) {
      RevCommit commit = revWalk.parseCommit(repository.resolve(commitId));
      RevCommit parentCommit = commit.getParentCount() > 0
              ? revWalk.parseCommit(commit.getParent(0)
              .getId())
              : null;


      try (ObjectReader reader = repository.newObjectReader()) {
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, commit.getTree());

        CanonicalTreeParser oldTreeIter = null;
        if (parentCommit != null) {
          oldTreeIter = new CanonicalTreeParser();
          oldTreeIter.reset(reader, parentCommit.getTree());
        }

        newTreeIter.reset(reader, commit.getTree());

        try (Git git = new Git(repository)) {
          List<DiffEntry> diffs = git.diff()
                  .setOldTree(oldTreeIter)
                  .setNewTree(newTreeIter)
                  .call();

          return diffs.stream()
                  .map(diff -> {
                    String path = switch (diff.getChangeType()) {
                      case DELETE -> diff.getOldPath();
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

