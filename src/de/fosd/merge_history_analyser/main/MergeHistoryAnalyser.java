package de.fosd.merge_history_analyser.main;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import de.fosd.merge_history_analyser.util.Util;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

//        RxJava
//        -l "/home/martin/hiwi_job/projekte/RxJava" -r "https://github.com/ReactiveX/RxJava" -b "./buildRxJava.sh"

//        Voldemort
//        -l "/home/martin/hiwi_job/projekte/voldemort" -r "https://github.com/voldemort/voldemort.git" -b "./buildVoldemort.sh"


/**
 * @author Martin Gruber
 */
public class MergeHistoryAnalyser {

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        options.addOption(Option.builder("l")
                .longOpt("local-repo")
                .desc("The path to where the repository is stored on this machine")
                .hasArg()
                .required()
                .build());

        options.addOption(Option.builder("r")
                .longOpt("remote-repo")
                .desc("The url to the repository")
                .hasArg()
                .build());

        options.addOption(Option.builder("b")
                .longOpt("build-script")
                .desc("Path to the script used to build the project")
                .hasArg()
                .build());

        options.addOption(Option.builder("f")
                .longOpt("from")
                .desc("Skip all merges before a specified commit")
                .hasArg()
                .build());

        options.addOption(Option.builder("t")
                .longOpt("to")
                .desc("Skip all merges after a specified commit")
                .hasArg()
                .build());

        options.addOption("s", "merge-strategy", true, "Use the given merge strategy");
        options.addOption("nv", "non-verbose", false, "Quiet output");
        options.addOption("o", "output", true, "Store results in given file");
        options.addOption("log", true, "Store logging output in given file. The default is log.txt");
        options.addOption("h", "help", false, "Print this help page");

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                new HelpFormatter().printHelp("java ", options);
            } else {
                Project project = new Project(cmd.getOptionValue("l"), cmd.getOptionValue("r"), cmd.getOptionValue("b"), !cmd.hasOption("nv"));
                String start = cmd.getOptionValue("f");
                String end = cmd.getOptionValue("t");

                if (cmd.hasOption("f") || cmd.hasOption("t")) {
                    try {
                        project.analyseFromTo(Integer.parseInt(start), Integer.parseInt(end));
                    } catch (NumberFormatException e) {
                        project.analyseFromTo(cmd.getOptionValue("f"), cmd.getOptionValue("t"));
                    }
                } else {
                    project.analyse();
                }

                //Object to XML Conversion
                XStream xstream = new XStream(new StaxDriver());
                xstream.processAnnotations(Project.class);
                String projectInXML = Util.formatXml(xstream.toXML(project));

                String filename = project.getName() + ".xml";
                if (cmd.hasOption("o")) {
                    filename = cmd.getOptionValue("o");
                }
                Util.writeFile(filename, projectInXML);

                String logFilename = "log.txt";
                if (cmd.hasOption("log")) {
                    logFilename = cmd.getOptionValue("log");
                }
                Util.writeFile(logFilename, project.logger.toString());
            }
        } catch (ParseException e) {
            new HelpFormatter().printHelp("java ", options);
        }
    }
}
