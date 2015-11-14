import java.util.LinkedList;
import java.util.List;

/**
 * Created by martin on 14.11.15.
 */
public class Build {
    String buildMessage;
    double runtime;
    String state;
    List<Exception> exceptions;

    public Build(String buildMessage, double runtime) {
        this.buildMessage = buildMessage;
        this.runtime = runtime;
        exceptions = new LinkedList<Exception>();
    }

    public void analyseBuildMessage() {
        if(buildMessage.contains("BUILD SUCCESSFUL")) {
            state = "PASSED";
        } else {
            state = "Problem";
        }
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

    public String getBuildMessage() {
        return buildMessage;
    }

    public void setBuildMessage(String buildMessage) {
        this.buildMessage = buildMessage;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


}
