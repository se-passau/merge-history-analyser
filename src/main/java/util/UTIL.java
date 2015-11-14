package util;

import java.io.*;

/**
 * Created by martin on 14.11.15.
 */
public class UTIL {


    public static void writeFile(String filename, String text) {
        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename), "utf-8"));
            writer.write(text);
        } catch (IOException ex) {
            // report
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {/*ignore*/}
        }
    }
}
