package de.fosd.merge_history_analyser.main;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import de.fosd.merge_history_analyser.data.*;

import de.fosd.merge_history_analyser.util.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Martin Gruber
 */
@XStreamAlias("Project")
public class Project {

    @XStreamAsAttribute
    private String name;

    @XStreamAlias("url")
    @XStreamAsAttribute
    private String remotePath;

    @XStreamOmitField
    private String localPath;

    @XStreamOmitField
    private Repository localRepo;

    @XStreamOmitField
    private Git git;

    @XStreamImplicit
    private List<MergeScenario> mergeScenarios;

    @XStreamAsAttribute
    private String buildScript;

    @XStreamAsAttribute
    private String testScript;

    public Project(String localPath, String remotePath, String buildScript, String testScript) {
        if (localPath == null || !(new File(localPath).isDirectory())) {
            throw new RuntimeException("Local repository does not exist: " + localPath);
        }
        this.name = localPath.substring(localPath.lastIndexOf("/") + 1);
        this.localPath = localPath;
        this.buildScript = buildScript;
        this.testScript = testScript;
        mergeScenarios = new LinkedList<>();
        //init
        try {
            localRepo = new RepositoryBuilder().findGitDir(new File(localPath)).build();
            git = new Git(localRepo);
        } catch (IOException e) {
            Logger.log(e.getMessage());
        }
        this.remotePath = remotePath != null ? remotePath : localRepo.getConfig().getString("remote", "origin", "url");
    }

    public String getName() {
        return name;
    }

    /**
     * Analyses all merges found in the project.
     */
    public void analyse() {
        checkoutMaster();
        List<RevCommit> mergeCommits = getMergeCommits();
        this.mergeScenarios = analyseMergeScenarios(mergeCommits);
        checkoutMaster();
    }

    /**
     * Analyses commits with given commit IDs.
     *
     * @param commitIDs commit IDs of the commits which shall be analysed
     */
    public void analyse(List<String> commitIDs) {
        checkoutMaster();
        List<RevCommit> mergeCommits = getMergeCommits();
        List<RevCommit> mergeCommitsToBeAnalysed =
                mergeCommits.stream()
                        .filter(commit -> commitIDs.contains(commit.getId().name()))
                        .collect(Collectors.toCollection(LinkedList::new));
        this.mergeScenarios = analyseMergeScenarios(mergeCommitsToBeAnalysed);
        checkoutMaster();
    }

    /**
     * Analyses all commits from {@param start} to {@param end}.
     *
     * @param start skip all commits before
     * @param end   skip all commits after
     */
    public void analyseFromTo(String start, String end) {
        checkoutMaster();
        this.mergeScenarios = analyseMergeScenarios(getMergeCommits(start, end));
        checkoutMaster();
    }

    /**
     * Analyses all commits from {@param start} to {@param end}.
     *
     * @param start index to start with
     * @param end   index to end with
     */
    public void analyseFromTo(int start, int end) {
        checkoutMaster();
        List<RevCommit> mergeCommits = getMergeCommits();
        this.mergeScenarios = analyseMergeScenarios(mergeCommits.subList(start, end));
        checkoutMaster();
    }

    /**
     * Returns all commits from {@param start} to {@param end} that are merge commits.
     * <p>
     * We consider a commit as a merge commit if its number of parents is greater than 1.
     *
     * @param start skip all commits before
     * @param end   skip all commits after
     * @return all commits within specified range that are merges
     */
    public List<RevCommit> getMergeCommits(String start, String end) {
        List<RevCommit> merges = new LinkedList<>();
        Iterable<RevCommit> log;
        boolean addCommits = start == null;
        try {
            log = git.log().call();
            for (RevCommit commit : log) {
                if (!addCommits) {
                    if (commit.getName().equals(start)) {
                        addCommits = true;
                    } else {
                        continue;
                    }
                }
                if (commit.getParentCount() > 1) {
                    merges.add(commit);
                }
                if (end != null && commit.getName().equals(end)) {
                    break;
                }
            }
        } catch (GitAPIException e) {
            Logger.log(e.getMessage());
        }

        return merges;
    }

    /**
     * Returns all commits of the project which are merges.
     * <p>
     * We consider a commit as a merge commit if its number of parents is greater than 1.
     *
     * @return all commits which are merges
     */
    public List<RevCommit> getMergeCommits() {
        return getMergeCommits(null, null);
    }

    /**
     * Calculates the number of merges found in this project
     *
     * @return number of mergescenarios.
     */
    public int getNumberOfMerges() {
        checkoutMaster();
        return getMergeCommits().size();
    }

    /**
     * Return the index of a commit in the list of all merge commits.
     *
     * @param commitID ID of commit which index is requested
     * @return index of the commit. Return -1 if there is no such merge commit.
     */
    public int getMergeIndexOf(String commitID) {
        checkoutMaster();
        List<RevCommit> mergeCommits = getMergeCommits();
        for (int i = 0; i < mergeCommits.size(); i++) {
            if (mergeCommits.get(i).getId().getName().equals(commitID)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Analyses a given List of RevCommits which are merges.
     *
     * @param mergeCommits JGit RevCommits to analyse
     * @return list of analysed MergeScenarios
     */
    public List<MergeScenario> analyseMergeScenarios(List<RevCommit> mergeCommits) {
        Logger.log("Analysing " + mergeCommits.size() + " merges");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < mergeCommits.size(); i++) {
            RevCommit commit = mergeCommits.get(i);
            Logger.log("Working on " + (i + 1) + "/" + mergeCommits.size() + " " + commit.getId().getName());
            MergeScenario mergeScenario = analyseMergeScenario(commit);
            mergeScenarios.add(mergeScenario);
            Logger.log("Finished");
        }
        long execTimeSeconds = (System.currentTimeMillis() - startTime) / 1000;
        Logger.log("Total time: " + execTimeSeconds/60 + "m " + ((int)execTimeSeconds%60) + "s");
        return mergeScenarios;
    }

    /**
     * Analyses one given RevCommit which is a merge.
     *
     * @param mergeCommit JGit RevCommit to analyse
     * @return analysed MergeScenario
     */
    public MergeScenario analyseMergeScenario(RevCommit mergeCommit) {
        MergeScenario mergeScenario = new MergeScenario(
                mergeCommit.getName(), mergeCommit.getParents()[0].getName(), mergeCommit.getParents()[1].getName());

        checkoutMaster();

        //TODO support other merge tools
        //Merge
        Logger.log("\tStart Merge");
        mergeScenario.setMerge(merge(mergeCommit));
        Logger.log("\tFinish Merge");

        //Build
        if (buildScript != null) {
            if (mergeScenario.getMerge().getState().equals("CONFLICTING")) {
                mergeScenario.setBuild(new Build("NO BUILD BECAUSE OF CONFLICT", 0));
                Logger.log("NO BUILD BECAUSE OF CONFLICT");
            } else {
                Logger.log("\tStart Build");
                mergeScenario.setBuild(build());
                Logger.log("\tFinish Build");
            }
        }

        //Tests
        if (testScript != null) {
            if (mergeScenario.getBuild().getState().equals("SUCCESSFUL")) {
                Logger.log("\tStart Tests");
                mergeScenario.setTests(test());
                Logger.log("\tFinish Tests");
            } else {
                Logger.log("\tNO TEST BECAUSE BUILD FAILED");
            }
        }

        checkoutMaster();

        //Parent 1
        Logger.log("\tAnalyse Parent " + mergeScenario.getParent1().getCommitID());
        try {
            git.checkout().setName(mergeScenario.getParent1().getCommitID()).call();
        } catch (GitAPIException e) {
            Logger.log(e.getMessage());
        }
        //Build
        if (buildScript != null) {
            Logger.log("\t\tStart Build");
            mergeScenario.getParent1().setBuild(build());
            Logger.log("\t\tFinish Build");
        }
        //Tests
        if (testScript != null) {
            if (mergeScenario.getBuild().getState().equals("SUCCESSFUL")) {
                Logger.log("\t\tStart Tests");
                    mergeScenario.getParent1().setTests(test());
                Logger.log("\t\tFinish Tests");
            } else {
                Logger.log("\tNO TEST BECAUSE BUILD FAILED");
            }
        }

        checkoutMaster();

        //Parent 2
        Logger.log("\t Analyse Parent " + mergeScenario.getParent2().getCommitID());
        try {
            git.checkout().setName(mergeScenario.getParent2().getCommitID()).call();
        } catch (GitAPIException e) {
            Logger.log(e.getMessage());
        }
        //Build
        if (buildScript != null) {
            Logger.log("\t\tStart Build");
            mergeScenario.getParent2().setBuild(build());
            Logger.log("\t\tFinish Build");
        }
        //Tests
        if (testScript != null) {
            if (mergeScenario.getBuild().getState().equals("SUCCESSFUL")) {
                Logger.log("\t\tStart Tests");
                mergeScenario.getParent2().setTests(test());
                Logger.log("\t\tFinish Tests");
            } else {
                Logger.log("\tNO TEST BECAUSE BUILD FAILED");
            }
        }

        checkoutMaster();

        //Pushed
        Logger.log("\t Analyse Pushed " + mergeCommit.getName());
        try {
            git.checkout().setName(mergeCommit.getName()).call();
        } catch (GitAPIException e) {
            Logger.log(e.getMessage());
        }
        //Build
        if (buildScript != null) {
            Logger.log("\t\tStart Build");
            mergeScenario.getPushed().setBuild(build());
            Logger.log("\t\tFinish Build");
        }
        //Tests
        if (testScript != null) {
            if (mergeScenario.getBuild().getState().equals("SUCCESSFUL")) {
                Logger.log("\t\tStart Tests");
                mergeScenario.getPushed().setTests(test());
                Logger.log("\t\tFinish Tests");
            } else {
                Logger.log("\tNO TEST BECAUSE BUILD FAILED");
            }
        }


        //TODO analyse base commit
//        RevWalk walk = new RevWalk(repository);
//        walk.setRevFilter(RevFilter.MERGE_BASE);
//        walk.markStart(commit1);
//        walk.markStart(commit2);
//        RevCommit mergeBase = walk.next();


        return mergeScenario;
    }

    /**
     * Performs a merge between the two parents of the given commit and checks for confilicts.
     * Changes the state of the local repo!
     *
     * @param mergeCommit commit, which merge should be performed
     * @return analysis of the merge: conflicts
     */
    private Merge merge(RevCommit mergeCommit) {
        Merge merge = new Merge();
        try {
            git.checkout().setName(mergeCommit.getParents()[0].getName()).call();
            MergeResult mergeResult = git.merge().include(mergeCommit.getParents()[1]).call();
            merge.setState(mergeResult.getMergeStatus().name());

            if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
                Set<String> keySet = mergeResult.getConflicts().keySet();
                Set<String> conflicts = new HashSet<>(keySet);
                merge.setConflicts(conflicts);
            }
        } catch (GitAPIException e) {
            Logger.log(e.getMessage());
            merge.setState("GitAPI Exception");
        }

        return merge;
    }

    /**
     * Builds the project according to a build-script, which has been set previously.
     *
     * @return analysis of the build: state (success/fail), runtime
     */
    private Build build() {
        Process p2;
        Build build = new Build();
        try {
            String completeCommand = buildScript + " " + localPath;
            p2 = Runtime.getRuntime().exec(completeCommand);
            p2.waitFor();
            String buildMessage = org.apache.commons.io.IOUtils.toString(p2.getInputStream());

            //TODO generalize
            //Handle Build Message
            build.setState("UNKNOWN");
            if (buildMessage.contains("NO BUILD POSSIBLE")) {
                build.setState("NO BUILD POSSIBLE");
            } else {
                if (buildMessage.contains("BUILD SUCCESSFUL")) {
                    build.setState("SUCCESSFUL");
                }
                if (buildMessage.contains("BUILD FAILED")) {
                    build.setState("FAILED");
                }
            }
            //Read Runtime
            if (buildMessage.contains("Total time")) {
                String rawTime = buildMessage.substring(buildMessage.lastIndexOf("Total time: ") + 12);
                build.setRuntime(Double.parseDouble(rawTime.split(" ")[0]));
            }
        } catch (IOException e) {
            build.setState("IO Exception");
            Logger.log(e.getMessage());
        } catch (InterruptedException e) {
            build.setState("Interrupted Exception");
            Logger.log(e.getMessage());
        }
        return build;
    }

    private Tests test() {
        Tests tests = new Tests();
        try {
            String line;
            Process p = Runtime.getRuntime().exec(testScript + " " + localPath);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                Logger.log("\t\t" + line);
            }
            input.close();

            tests.message = org.apache.commons.io.IOUtils.toString(p.getInputStream());
            p.waitFor();
            FileReader fileReader = new FileReader(localPath + "/build/reports/summary.csv");
            for (CSVRecord record : CSVFormat.EXCEL.withHeader().parse(fileReader)) {
                tests.addTestCase(record.get("Test"), record.get("Result"), record.get("Duration"));
            }
            return tests;
        } catch (IOException | InterruptedException e) {
            Logger.log(e.getMessage());
        }
        return tests;
    }

    /**
     * Resets the repo.
     */
    private void checkoutMaster() {
        try {
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef("master").call();
            git.checkout().setForce(true).setName("master").call();
        } catch (GitAPIException e) {
            Logger.log(e.getMessage());
        }
    }
}
