package iu.sna.infrastructure.GraphCreator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse input git repository.
 * Has methods to find changed files
 * in commits
 */
public final class GitCommitParser {
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
    public List<List<ChangedFile>> getChangeFilesInFirstNcommits(
            final int numberOfCommits) {
        List<List<ChangedFile>> result = new ArrayList<>();
        try (Git git = new Git(this.repository)) {
            Iterable<RevCommit> commits = git.log()
                    .setMaxCount(numberOfCommits)
                    .call();
            for (RevCommit commit : commits) {
                List<ChangedFile> changedFiles = getChangedLinesInCommit(

                        commit.getId()
                );
                result.add(changedFiles);
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
     * @return List  of changed files and no of changes
     */
    public List<ChangedFile> getChangedLinesInCommit(final ObjectId commitId) {
        try (RevWalk revWalk = new RevWalk(this.repository)) {
            RevCommit commit = revWalk.parseCommit(commitId);
            if (commit == null) {
                throw new IllegalArgumentException("Invalid commit ID: " + commitId);
            }
            RevCommit parentCommit = commit.getParentCount() > 0
                    ? revWalk.parseCommit(commit.getParent(0)
                    .getId())
                    : null;

            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 DiffFormatter diffFormatter = new DiffFormatter(out)) {

                diffFormatter.setRepository(this.repository);
                diffFormatter.setDetectRenames(true);

                List<DiffEntry> diffs;
                if (parentCommit != null) {
                    diffs = diffFormatter.scan(parentCommit.getTree(), commit.getTree());
                } else {
                    diffs = diffFormatter.scan(
                            new EmptyTreeIterator(),
                            new CanonicalTreeParser(
                                    null, repository.newObjectReader(), commit.getTree())
                    );
                }

                List<ChangedFile> changedFiles = new ArrayList<>();
                for (DiffEntry diff : diffs) {
                    int linesChanged = 0;
                    diffFormatter.format(diff);
                    for (Edit edit : diffFormatter.toFileHeader(diff)
                            .toEditList()) {
                        linesChanged += edit.getEndA() - edit.getBeginA(); // Deleted lines
                        linesChanged += edit.getEndB() - edit.getBeginB(); // Added lines
                    }

                    String path = diff.getChangeType() == DiffEntry.ChangeType.DELETE
                            ? diff.getOldPath()
                            : diff.getNewPath();

                    if (path != null) {
                        changedFiles.add(new ChangedFile(Paths.get(path), linesChanged));
                    }
                }

                return changedFiles;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing commit diff  ", e);
        }
    }
}

