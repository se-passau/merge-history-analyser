import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by martin on 14.11.15.
 */
public class Merge {

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

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Merge \n");
        builder.append("Status: " + state + "\n");
        builder.append("Exceptions: \n");
        Iterator<String> iterator = conflicts.iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next() + "\n");
        }
        return builder.toString();
    }
}
