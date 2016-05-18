package de.fosd.merge_history_analyser.data;

/**
 * Created by martin on 18.05.16.
 */
public class AnalysedCommit {
    String commitID;


    Build build;
    Tests tests;

    public AnalysedCommit(String commitID) {
        this.commitID = commitID;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public Tests getTests() {
        return tests;
    }

    public void setTests(Tests tests) {
        this.tests = tests;
    }

    public String getCommitID() {
        return commitID;
    }

    public void setCommitID(String commitID) {
        this.commitID = commitID;
    }
}
