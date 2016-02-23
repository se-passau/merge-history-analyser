package de.fosd.merge_history_analyser.data;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author Martin Gruber
 */
public class Build {

    @XStreamAsAttribute
    private String state;

    @XStreamAsAttribute
    private double runtime;

    public Build() {
    }

    public Build(String state, int runtime) {
        this.state = state;
        this.runtime = runtime;
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
