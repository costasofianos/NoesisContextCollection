package org.noesis.codeanalysis.computations.kotlin.psi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInput;
import org.noesis.codeanalysis.interfaces.Computation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GitComputation extends Computation {
    public GitComputation(ContextCollectionInput contextCollectionInput) {
        super(contextCollectionInput);
        System.out.println("Searching for commits in "+contextCollectionInput.getRepoFolder().getPath());
        try {
            List<String> changedFiles = getRecentlyChangedFiles(contextCollectionInput.getRepoFolder().getPath(), 10);
            System.out.println("Found commits "+changedFiles.toString()+" in "+contextCollectionInput.getRepoFolder().getPath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("Done searching for commits");
    }

    public static List<String> getRecentlyChangedFiles(String repoPath, int numberOfCommits) throws Exception {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File(repoPath + "/.git"))
                .readEnvironment()
                .findGitDir()
                .build();

        List<String> recentFiles = new ArrayList<>();

        try (Git git = new Git(repository)) {
            Iterable<RevCommit> commits = git.log().setMaxCount(numberOfCommits).call();

            RevCommit previous = null;
            for (RevCommit commit : commits) {
                if (previous != null) {
                    try (DiffFormatter df = new DiffFormatter(new ByteArrayOutputStream())) {
                        df.setRepository(repository);
                        List<DiffEntry> diffs = df.scan(previous.getTree(), commit.getTree());

                        for (DiffEntry diff : diffs) {
                            String path = diff.getNewPath();
                            if (!recentFiles.contains(path)) {
                                recentFiles.add(path);
                            }
                        }
                    }
                }
                previous = commit;
            }
        }

        return recentFiles;
    }

    @Override
    public void close() throws Exception {

    }
}
