package de.fosd.merge_history_analyser.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by martin on 09.05.16.
 */
public class Tests {
    private int total;
    private int failures;
    private int passed;
    private int skipped;
    private List<TestCase> testCases = new LinkedList<>();

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
