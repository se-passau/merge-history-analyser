package de.fosd.merge_history_analyser.main;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import de.fosd.merge_history_analyser.util.Util;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        //TODO search for url in .git folder
        options.addOption(Option.builder("r")
                .longOpt("remote-repo")
                .desc("The url to the repository")
                .hasArg()
                .build());

        OptionGroup buildGroup = new OptionGroup();

        buildGroup.addOption(Option.builder("b")
                .longOpt("build-script")
                .desc("Path to the script used to build the project")
                .hasArg()
                .build());

        buildGroup.addOption(Option.builder("nb")
                .longOpt("no-build")
                .desc("Skip build step")
                .build());

        options.addOptionGroup(buildGroup);

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
                //Check for build script
                String buildScript = null;
                String projectName = cmd.getOptionValue("l").substring(cmd.getOptionValue("l").lastIndexOf("/") + 1).toLowerCase();
                File searchPath = new File("scripts/build/");
                if(!cmd.hasOption("nb")) {
                    if(cmd.hasOption("b")) {
                        File optionB = new File(cmd.getOptionValue("b"));
                        if(optionB.isDirectory()) {
                            searchPath = optionB;
                        } else {
                            buildScript = cmd.getOptionValue("b");
                        }
                    }
                    //buildscript not jet set because option "b" was directory or not set -> Search in folder
                    if(buildScript == null) {
                        for (String file : searchPath.list()) {
                            if (file.toLowerCase().contains(projectName)) {
                                buildScript = searchPath.toString() + "/" + file;
                            }
                        }
                    }
                    //no buildscript found
                    if(buildScript == null) {
                        //TODO log
                    } else {
                        File buildFile = new File(buildScript);
                        if(!buildFile.exists()) {
                            throw new IllegalArgumentException("Specified buildscript does not exist");
                        } else {
                            buildScript = buildFile.getAbsolutePath();
                        }
                    }
                }

                Project project = new Project(cmd.getOptionValue("l"), cmd.getOptionValue("r"), buildScript, !cmd.hasOption("nv"));
                String start = cmd.getOptionValue("f");
                String end = cmd.getOptionValue("t");

                if (cmd.hasOption("f") || cmd.hasOption("t")) {
                    try {
                        //Interpret FROM TO as numbers
                        project.analyseFromTo(Integer.parseInt(start), Integer.parseInt(end));
                    } catch (NumberFormatException e) {
                        //Interpret FROM TO as commitIDs
                        project.analyseFromTo(cmd.getOptionValue("f"), cmd.getOptionValue("t"));
                    }
                } else {
                    project.analyse();
                }

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
