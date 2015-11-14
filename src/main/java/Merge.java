import org.eclipse.jgit.api.MergeResult;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by martin on 14.11.15.
 */
public class Merge {
    MergeResult mergeResult;
    String status;
    List<Exception> exceptions;

    public Merge(MergeResult mergeResult) {
        this.mergeResult = mergeResult;
        exceptions = new LinkedList<Exception>();
        Iterator conflictIterator = mergeResult.getConflicts().keySet().iterator();

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

    public MergeResult getMergeResult() {
        return mergeResult;
    }

    public void setMergeResult(MergeResult mergeResult) {
        this.mergeResult = mergeResult;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
    }
}
