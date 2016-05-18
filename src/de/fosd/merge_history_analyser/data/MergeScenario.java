package de.fosd.merge_history_analyser.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Martin Gruber
 */
@XStreamAlias("MergeScenario")
public class MergeScenario {
    private String commitID;


    @XStreamAlias("parent")
    private AnalysedCommit parent1;

    @XStreamAlias("parent")
    private AnalysedCommit parent2;

    private Merge merge;
    private Build build;
    private Tests tests;

    public MergeScenario(String commit, String parent1ID, String parent2ID) {
        this.commitID = commit;
        this.parent1 = new AnalysedCommit(parent1ID);
        this.parent2 = new AnalysedCommit(parent2ID);
        merge = new Merge();
        build = new Build();
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

    public Tests getTests() {
        return tests;
    }

    public void setTests(Tests tests) {
        this.tests = tests;
    }

    public AnalysedCommit getParent1() {
        return parent1;
    }

    public void setParent1(AnalysedCommit parent1) {
        this.parent1 = parent1;
    }

    public AnalysedCommit getParent2() {
        return parent2;
    }

    public void setParent2(AnalysedCommit parent2) {
        this.parent2 = parent2;
    }
}
