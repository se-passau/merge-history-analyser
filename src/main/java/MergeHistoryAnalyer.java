import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NotMergedException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by martin on 30.09.15.
 */
public class MergeHistoryAnalyer {

    String localPath;
    String remotePath;
    Repository localRepo;
    Git git;


    public MergeHistoryAnalyer(String localPath, String remotePath) {

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
        List<RevCommit> mergeScenarios = null;
        try {
            mergeScenarios = getMerges();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < mergeScenarios.size(); i++) {
            System.out.println("Analyse " + mergeScenarios.get(i).getName());
            try {
                //Merge
                MergeResult mergeResult = getMergeResult(mergeScenarios.get(i));
                if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.MERGED)) {
                    System.out.println("Merge successful");
                } else if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
                    System.out.println("Merge conflicted");
                    System.out.println(mergeResult.getConflicts().toString());
                    break;
                } else {
                    System.out.println("Other merge problem");
                    break;
                }

                //Build
                String buildMessage = build();
                if (buildMessage.contains("BUILD SUCCESSFUL")) {
                    System.out.println("Build successful");
                } else {
                    System.out.println("Other build problem");
                }

                //Tests

            } catch (NotMergedException e) {
                System.out.println("Merge, NotMergedException");
            } catch (GitAPIException e) {
                System.out.println("Merge, GitAPIException");
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.out.println("Build exception");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Build exception");
                e.printStackTrace();
            }
            System.out.println();
        }
    }

    public List<RevCommit> getMerges() throws GitAPIException {
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

    public String build() throws IOException, InterruptedException {
        Process p2 = Runtime.getRuntime().exec("./build.sh");
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
            MergeHistoryAnalyer analyer = new MergeHistoryAnalyer(args[0], args[1]);
            analyer.analyse();
            try {
                analyer.getMerges();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }


        }


    }
}
