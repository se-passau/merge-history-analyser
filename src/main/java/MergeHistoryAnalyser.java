import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.util.Arrays;

public class MergeHistoryAnalyser {

    public static void main(String[] args) {
        String USAGE = "Usage: MergeHistoryAnalyer [local Repo] [remote Repo] [build Script]\n";

        //RxJava
        args[0] = "/home/martin/hiwi_job/projekte/RxJava";
        args[1] = "https://github.com/ReactiveX/RxJava";
        args[2] = "./buildRxJava.sh";

        //Voldemort
//        args[0] = "/home/martin/hiwi_job/projekte/voldemort";
//        args[1] = "https://github.com/voldemort/voldemort.git";
//        args[2] = "./buildVoldemort.sh";


        if (args.length != 3) {
            System.err.println(USAGE);
        } else {
            Project project = new Project(args[0], args[1], args[2]);

            long startTime = System.currentTimeMillis();

            //Voldemort
//           project.analyse(Arrays.asList("9c8e0119ba0e0114b0f2dd114e0e7337690df2f6"));
//            project.analyse(Arrays.asList("bb6064a6881c315befe238363e590e6d225ac07c"));
//            project.analyse(Arrays.asList("9c8e0119ba0e0114b0f2dd114e0e7337690df2f6", "bb6064a6881c315befe238363e590e6d225ac07c"));
//            project.analyseFromTo(440, 446);
            project.analyseFromTo(909, 910);

            //RxJava
            //project.analyseFromTo(910, 915);


            //TODO auch serialisieren wenn zwischendurch eine Excpetion fliegt.
            //Object to XML Conversion
            XStream xstream = new XStream(new StaxDriver());
            xstream.processAnnotations(Project.class);
            String xml = UTIL.formatXml(xstream.toXML(project));
            UTIL.writeFile(project.name + ".xml", xml);

            System.out.println("Total time: " + (System.currentTimeMillis() - startTime));
        }
    }
}
