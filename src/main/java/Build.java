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

    public Build() {
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
        return builder.toString();
    }

}
