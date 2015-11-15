import org.eclipse.jgit.api.MergeResult;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by martin on 14.11.15.
 */
public class Merge {

    String status;
    List<Exception> exceptions;

    public Merge() {
        exceptions = new LinkedList<Exception>();
        //Iterator conflictIterator = mergeResult.getConflicts().keySet().iterator();

        /*
        if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
            while(conflictIterator.hasNext()) {
                System.out.println(conflictIterator.next());
            }
        } else {
        }
        */
    }

    public void addException(Exception e) {
        exceptions.add(e);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Merge \n");
        builder.append("Status: " + status + "\n");
        builder.append("Exceptions: \n");
        Iterator<Exception> iterator = exceptions.iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next() + "\n");
        }
        return builder.toString();
    }
}
