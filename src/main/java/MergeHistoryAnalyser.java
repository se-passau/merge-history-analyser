import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NotMergedException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.*;
import java.util.*;

/**
 * Created by martin on 30.09.15.
 */
public class MergeHistoryAnalyser {

    String localPath;
    String remotePath;
    Repository localRepo;
    Git git;

    public MergeHistoryAnalyser(String localPath, String remotePath) {

        this.localPath = localPath;
        this.remotePath = remotePath;
        //init
        try {
            localRepo = new FileRepository(localPath + "/.git");
            git = new Git(localRepo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void analyse() {

        try {
            git.checkout().setName("master").call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        List<RevCommit> mergeScenarios = null;
        try {
            mergeScenarios = getMergeScenarios();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        analyse(mergeScenarios.size());
    }

    public void analyse(int numberOfAnalysis) {
        StringBuffer logMessage = new StringBuffer();

        try {
            git.checkout().setName("master").call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        List<RevCommit> mergeScenarios = null;
        try {
            mergeScenarios = getMergeScenarios();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < numberOfAnalysis; i++) {
            logMessage.append("Analyse " + mergeScenarios.get(i) + " " + new Date((long) mergeScenarios.get(i).getCommitTime() * 1000) + "\n");
            try {
                //Merge
                MergeResult mergeResult = getMergeResult(mergeScenarios.get(i));

                if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
                    logMessage.append("Merge conflicted\n");
                    logMessage.append(mergeResult.getConflicts().toString());
                    break;
                } else {
                    logMessage.append("Merge successful\n");
                }

                //Build
                String buildMessage = build("./buildVoldemort.sh");
                if (buildMessage.contains("BUILD SUCCESSFUL")) {
                    logMessage.append("Build successful\n");
                } else {
                    logMessage.append("Other build problem\n");
                }

                //Tests

            } catch (NotMergedException e) {
                logMessage.append("Merge, NotMergedException\n");
            } catch (GitAPIException e) {
                logMessage.append("Merge, GitAPIException\n");
                e.printStackTrace();
            } catch (InterruptedException e) {
                logMessage.append("Build exception\n");
                e.printStackTrace();
            } catch (IOException e) {
                logMessage.append("Build exception\n");
                e.printStackTrace();
            }
            logMessage.append("\n");
            writeFile("log", logMessage.toString());
        }
    }

    public List<RevCommit> getMergeScenarios() throws GitAPIException {
        Iterable<RevCommit> log = git.log().call();
        Iterator<RevCommit> it = log.iterator();
        List<RevCommit> merges = new LinkedList<RevCommit>();
        while (it.hasNext()) {
            RevCommit comm = it.next();
            if (comm.getParentCount() > 1) {
                merges.add(comm);
            }
        }
        return merges;
    }

    public MergeResult getMergeResult(RevCommit merge) throws GitAPIException {

        git.checkout().setName(merge.getParents()[0].getName()).call();
        MergeResult mergeResult = git.merge().include(merge.getParents()[1]).call();

        return mergeResult;
    }

    public List<String> getTags() throws GitAPIException {
        List<Ref> tagList = git.tagList().call();
        List<String> tagNames = new ArrayList<String>(tagList.size());
        for (int i = 0; i < tagList.size(); i++) {
            tagNames.add(tagList.get(i).getName());
        }
        return tagNames;
    }

    public String build(String buildCommand) throws IOException, InterruptedException {
        Process p2 = Runtime.getRuntime().exec(buildCommand);
        p2.waitFor();

        String message = org.apache.commons.io.IOUtils.toString(p2.getInputStream());

        return message;
    }

    public void writeFile(String filename, String text) {
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

    public static void main(String[] args) {
        String USAGE = "Usage: MergeHistoryAnalyer [local Repo] [remote Repo]\n";

        if (args.length != 2) {
            System.err.println(USAGE);
        } else {
            MergeHistoryAnalyser analyer = new MergeHistoryAnalyser(args[0], args[1]);

            analyer.analyse(2);
        }


    }
}
