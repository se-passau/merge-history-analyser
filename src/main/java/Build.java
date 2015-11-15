import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by martin on 14.11.15.
 */
public class Build {

    @XStreamAsAttribute
    String state;

    @XStreamAsAttribute
    double runtime;

    List<Exception> exceptions;

    public Build() {
        exceptions = new LinkedList<Exception>();
    }

    public void addException(Exception e) {
        exceptions.add(e);
    }

    public double getRuntime() {
        return runtime;
    }

    public void setRuntime(double runtime) {
        this.runtime = runtime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Build \n");
        builder.append("Status: " + state + "\n");
        builder.append("Runtime: " + runtime + "\n");
        builder.append("Exceptions: \n");
        Iterator<Exception> iterator = exceptions.iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next() + "\n");
        }
        return builder.toString();
    }

}
