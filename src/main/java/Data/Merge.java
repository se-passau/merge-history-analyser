package Data;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.Set;

/**
 * Created by martin on 14.11.15.
 */
public class Merge {

    @XStreamAsAttribute
    String state;
    Set<String> conflicts;

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
