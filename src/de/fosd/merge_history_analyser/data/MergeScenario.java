package de.fosd.merge_history_analyser.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Martin Gruber
 */
@XStreamAlias("MergeScenario")
public class MergeScenario {
    private String commit;

    @XStreamAlias("parent")
    private String parent1;

    @XStreamAlias("parent")
    private String parent2;

    private Merge merge;
    private Build build;


    public MergeScenario(String commit, String parent1, String parent2) {
        this.commit = commit;
        this.parent1 = parent1;
        this.parent2 = parent2;
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
}
