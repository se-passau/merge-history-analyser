import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;

/**
 * Created by martin on 14.11.15.
 */
public class MergeScenario {
    RevCommit mergeCommit;
    RevCommit parent1;
    RevCommit parent2;
    Merge merge;
    Build build;


    public MergeScenario(RevCommit mergeCommit) {
        this.mergeCommit = mergeCommit;
        this.parent1 = mergeCommit.getParents()[0];
        this.parent2 = mergeCommit.getParents()[1];
    }

    public Merge getMerge() {
        return merge;
    }

    public void setMerge(Merge merge) {
        this.merge = merge;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MergeCommit: " + mergeCommit.getName() + "\n");
        builder.append("Parent: " + parent1.getName() + "\n");
        builder.append("Parent: " + parent2.getName() + "\n");
        builder.append(merge);
        builder.append(build);
    }
}
