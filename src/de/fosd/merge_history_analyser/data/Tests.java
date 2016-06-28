package de.fosd.merge_history_analyser.data;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by martin on 09.05.16.
 */
public class Tests {
    @XStreamAsAttribute
    private int total;

    @XStreamAsAttribute
    private int failures;

    @XStreamAsAttribute
    private int passed;

    @XStreamAsAttribute
    private int skipped;

    @XStreamImplicit
    private List<TestCase> testCases;

    public Tests() {
        testCases = new LinkedList<>();
        total = 0;
        failures = 0;
        passed = 0;
        skipped = 0;
    }

    public void addTestCase(String name, String result, String duration) {
        testCases.add(new TestCase(name, result, duration));
        switch (result) {
            case "fail":
                failures++;
                break;
            case "pass":
                passed++;
                break;
            case "skip":
                skipped++;
                break;
        }
        total++;
    }
}
