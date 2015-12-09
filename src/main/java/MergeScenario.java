import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by martin on 14.11.15.
 */
public class MergeScenario {
    String commit;

    @XStreamAlias("parent")
    String parent1;

    @XStreamAlias("parent")
    String parent2;

    Merge merge;
    Build build;


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
