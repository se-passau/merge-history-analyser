import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.Arrays;

public class MergeHistoryAnalyser {

    public static void main(String[] args) {
        String USAGE = "Usage: MergeHistoryAnalyer [local Repo] [remote Repo] [build Script]\n";

        if (args.length != 3) {
            System.err.println(USAGE);
        } else {
            Project project = new Project(args[0], args[1], args[2]);
            //project.analyse(Arrays.asList("9c8e0119ba0e0114b0f2dd114e0e7337690df2f6"));
            //project.analyse(Arrays.asList("bb6064a6881c315befe238363e590e6d225ac07c"));
            //project.analyse(Arrays.asList("9c8e0119ba0e0114b0f2dd114e0e7337690df2f6", "bb6064a6881c315befe238363e590e6d225ac07c"));
            project.analyseFromTo(440, 445);


            XStream xstream = new XStream(new StaxDriver());
            xstream.processAnnotations(Project.class);

            //Object to XML Conversion
            String xml = UTIL.formatXml(xstream.toXML(project));
            UTIL.writeFile("log.xml", xml);

        }
    }
}
