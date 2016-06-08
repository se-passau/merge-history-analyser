package de.fosd.merge_history_analyser.util;

import java.io.*;

/**
 * Created by martin on 19.05.16.
 */
public class Logger {

    private static Writer writer;
    private static boolean v;

    public static void init(String filename, boolean verbose) {
        v = verbose;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(String message) {
        if(v) {
            System.out.println(message);
        }
        try {
            writer.write(message+"\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
