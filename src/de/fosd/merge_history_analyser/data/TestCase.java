package de.fosd.merge_history_analyser.data;

/**
 * Created by martin on 09.05.16.
 */
public class TestCase {
    String name;
    String result;
    String duration;

    public TestCase(String name, String result, String duration) {
        this.name = name;
        this.result = result;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
