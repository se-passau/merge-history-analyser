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
                .required()
                .build());

        options.addOption(Option.builder("b")
                .longOpt("build-script")
                .desc("Path to the script used to build the project")
                .hasArg()
                .build());

        options.addOption(Option.builder("ft")
                .longOpt("from-to")
                .desc("analyses only an extract out of all merges")
                .hasArg()
                .numberOfArgs(2)
                .build());

        options.addOption("nv", "non-verbose", false, "Quiet output");
        options.addOption("o", "output", true, "store results in given file");
        options.addOption("log", true, "store log output in given file");
        options.addOption("h", "help", false, "Print this help page");

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                new HelpFormatter().printHelp("java ", options);
            } else {
                Project project = new Project(cmd.getOptionValue("l"), cmd.getOptionValue("r"), cmd.getOptionValue("b"), !cmd.hasOption("nv"));

                if (cmd.hasOption("ft")) {
                    project.analyseFromTo(Integer.parseInt(cmd.getOptionValues("ft")[0]), Integer.parseInt(cmd.getOptionValues("ft")[1]));
                } else {
                    project.analyse();
                }


                //TODO auch serialisieren wenn zwischendurch eine Excpetion fliegt.
                //Object to XML Conversion
                XStream xstream = new XStream(new StaxDriver());
                xstream.processAnnotations(Project.class);
                String xml = Util.formatXml(xstream.toXML(project));
                if(cmd.hasOption("o")) {
                    Util.writeFile(cmd.getOptionValue("o"), xml);
                } else {
                    Util.writeFile(project.getName() + ".xml", xml);
                }
                if(cmd.hasOption("log")) {
                    Util.writeFile(cmd.getOptionValue("log"), project.logger.toString());
                } else {
                    Util.writeFile("log.txt", project.logger.toString());
                }
            }
        } catch (ParseException e) {
            new HelpFormatter().printHelp("java ", options);
        }
    }
}
