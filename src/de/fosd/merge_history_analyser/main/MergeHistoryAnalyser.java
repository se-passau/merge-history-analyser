package de.fosd.merge_history_analyser.main;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import de.fosd.merge_history_analyser.util.Logger;
import de.fosd.merge_history_analyser.util.Util;
import org.apache.commons.cli.*;

import java.io.File;

/**
 * @author Martin Gruber
 */
public class MergeHistoryAnalyser {

    public static String searchFile(File directory, String buzzword) {
        for (File file : directory.listFiles()) {
            if (file.getName().toLowerCase().contains(buzzword)) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

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
                .desc("The url to the repository. Just for documentation, not necessary.")
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

        OptionGroup buildGroup = new OptionGroup();
        buildGroup.addOption(Option.builder("bs")
                .longOpt("build-script")
                .desc("Path to the script used to build the project")
                .hasArg()
                .build());
        buildGroup.addOption(Option.builder("bd")
                .longOpt("build-directory")
                .desc("Path to the directory where a build-script will be searched")
                .hasArg()
                .build());
        buildGroup.addOption(Option.builder("nb")
                .longOpt("no-build")
                .desc("Skip build step, otherwise build-script will be searched in scripts/build")
                .build());
        options.addOptionGroup(buildGroup);

        OptionGroup testGroup = new OptionGroup();
        testGroup.addOption(Option.builder("ts")
                .longOpt("test-script")
                .desc("Path to the script used to test the project")
                .hasArg()
                .build());
        testGroup.addOption(Option.builder("td")
                .longOpt("test-directory")
                .desc("Path to the directory where a test-script will be searched")
                .hasArg()
                .build());
        testGroup.addOption(Option.builder("nt")
                .longOpt("no-test")
                .desc("Skip test step, otherwise test-script will be searched in scripts/test")
                .build());
        options.addOptionGroup(testGroup);


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
                String projectName = cmd.getOptionValue("l").substring(cmd.getOptionValue("l").lastIndexOf("/") + 1).toLowerCase();

                //Init logging
                Logger.init((cmd.hasOption("log") ? cmd.getOptionValue("log") : ("log_" + projectName + ".txt")), !cmd.hasOption("nv"));

                //Check for build script
                String buildScriptPath = null;
                if (!cmd.hasOption("nb")) {
                    if (cmd.hasOption("bs")) {
                        File optionB = new File(cmd.getOptionValue("bs"));
                        if (optionB.exists() && optionB.isFile()) {
                            buildScriptPath = optionB.getAbsolutePath();
                        } else {
                            Logger.log("Specified build-script does not exist");
                            throw new IllegalArgumentException("Specified build-script does not exist");
                        }
                    } else if (cmd.hasOption("bd")) {
                        File optionB = new File(cmd.getOptionValue("td"));
                        if (optionB.exists() && optionB.isDirectory()) {
                            buildScriptPath = searchFile(optionB, projectName);
                        } else {
                            Logger.log("Argument of option -bd has to be a directory");
                            throw new IllegalArgumentException("Argument of option -bd has to be a directory");
                        }
                    } else {
                        Logger.log("Automatically search build-script in scripts/build");
                        buildScriptPath = searchFile(new File("scripts/build"), projectName);
                    }
                    if(buildScriptPath == null) {
                        Logger.log("No build-script found");
                    } else {
                        Logger.log("Using build-script: " + buildScriptPath);
                    }
                } else {
                    Logger.log("Build disabled");
                }
                Logger.log("");

                //Check for test script
                String testScriptPath = null;
                if (!cmd.hasOption("nt")) {
                    if (cmd.hasOption("ts")) {
                        File optionB = new File(cmd.getOptionValue("ts"));
                        if (optionB.exists() && optionB.isFile()) {
                            testScriptPath = optionB.getAbsolutePath();
                        } else {
                            Logger.log("Specified test-script does not exist");
                            throw new IllegalArgumentException("Specified test-script does not exist");
                        }
                    } else if (cmd.hasOption("td")) {
                        File optionB = new File(cmd.getOptionValue("td"));
                        if (optionB.exists() && optionB.isDirectory()) {
                            testScriptPath = searchFile(optionB, projectName);
                        } else {
                            Logger.log("Argument of option -td has to be a directory");
                            throw new IllegalArgumentException("Argument of option -td has to be a directory");
                        }
                    } else {
                        Logger.log("Automatically search test-script in scripts/build");
                        testScriptPath = searchFile(new File("scripts/test"), projectName);
                    }
                    if(buildScriptPath == null) {
                        Logger.log("No test-script found");
                    } else {
                        Logger.log("Using test-script: " + buildScriptPath);
                    }
                } else {
                    Logger.log("Tests disabled");
                }
                Logger.log("");


                //Check local Repo
                String localRepoPath;
                File localRepoFile = new File(cmd.getOptionValue("l"));
                if (localRepoFile.exists()) {
                    localRepoPath = localRepoFile.getAbsolutePath();
                } else {
                    throw new IllegalArgumentException("Specified local path does not exist");
                }



                //START ANALYSE
                Project project = new Project(localRepoPath, cmd.getOptionValue("r"), buildScriptPath, testScriptPath);
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
                Util.writeFile(cmd.hasOption("o") ? cmd.getOptionValue("o") : project.getName() + ".xml", xml);

                //Close logger
                Logger.close();
            }
        } catch (ParseException e) {
            new HelpFormatter().printHelp("java", options);
        }
    }
}
