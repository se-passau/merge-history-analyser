import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.eclipse.jgit.lib.ObjectId;
import org.xml.sax.InputSource;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Arrays;

/**
 * Created by martin on 30.09.15.
 */
public class MergeHistoryAnalyser {


    public static void main(String[] args) {
        String USAGE = "Usage: MergeHistoryAnalyer [local Repo] [remote Repo] [build Script]\n";

        if (args.length != 3) {
            System.err.println(USAGE);
        } else {
            Project project = new Project(args[0], args[1], args[2]);
            project.analyse(Arrays.asList("6f63a3e274a75a553595d787b3e11f6ef19e573b"));
            //project.analyse(2);

            XStream xstream = new XStream(new StaxDriver());
            xstream.processAnnotations(Project.class);

            //Object to XML Conversion
            String xml = UTIL.formatXml(xstream.toXML(project));
            UTIL.writeFile("log.xml", xml);

        }
    }
}
