package de.fosd.merge_history_analyser.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by martin on 09.05.16.
 */
@XStreamAlias("TestCase")
public class TestCase {
    String name;
    String result;
    String duration;

    public TestCase(String name, String result, String duration) {
        this.name = name;
        this.result = result;
        this.duration = duration;
    }
}
