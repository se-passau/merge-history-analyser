package de.fosd.merge_history_analyser.data;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.Set;

/**
 * @author Martin Gruber
 */
public class Merge {

    @XStreamAsAttribute
    private String state;
    private Set<String> conflicts;

    public Merge() {
    }


    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Set<String> getConflicts() {
        return conflicts;
    }

    public void setConflicts(Set<String> conflicts) {
        this.conflicts = conflicts;
    }
}
