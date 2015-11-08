import com.sun.org.apache.xpath.internal.SourceTree;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by martin on 30.09.15.
 */
public class mergeHistoryAnalyer {

    static Repository localRepo;
    static Git git;

    static String USAGE = "Usage: mergeHistoryAnalyer [local Repo] [remote Repo]\n";

    public static void main(String[] args) throws IOException, GitAPIException {

        if(args.length != 2) {
            System.err.println(USAGE);
        } else {
            init(args[0], args[1]);

            List<RevCommit> merges = getMerges();


        }






        //Out Status, Parents

        /*
        for (int i = 0; i < 2; i++) {
            System.out.println("checking " + merges.get(i));

            git.checkout().setName(merges.get(i).getName());
            boolean buildSuccessful = buildVoldemort();
            System.out.println(merges.get(i).getName() + " || " + new java.util
                            .Date((long) merges.get(i).getCommitTime() * 1000) + " || Build successful: " + buildSuccessful);


            git.checkout().setName(merges.get(i).getParents()[0].getName());
            buildSuccessful = buildVoldemort();
            System.out.println("First Parent");
            System.out.println(
                    merges.get(i).getName() + " || " + new java.util.Date(
                            (long) merges.get(i).getCommitTime() * 1000) + " " +
                            "|| Build successful: " + buildSuccessful);

            git.checkout().setName(merges.get(i).getParents()[1].getName());
            buildSuccessful = buildVoldemort();
            System.out.println("Second Parent");
            System.out.println(
                    merges.get(i).getName() + " || " + new java.util.Date(
                            (long) merges.get(i).getCommitTime() * 1000) + " " +
                            "|| Build successful: " + buildSuccessful);

        }
        */


    }

    public static List<RevCommit> getMerges() throws GitAPIException {
        Iterable<RevCommit> log = git.log().call();
        Iterator<RevCommit> it = log.iterator();
        List<RevCommit> merges = new LinkedList<RevCommit>();
        while(it.hasNext()) {
            RevCommit comm = it.next();
            if (comm.getParentCount() > 1) {
                merges.add(comm);
            }
        }
        return merges;
    }

    public static List<MergeResult> getMergeResults(List<RevCommit> merges) throws GitAPIException {

        List<MergeResult> mergeResults = new LinkedList<MergeResult>();

        for (int i = 0; i < merges.size(); i++) {
            git.checkout().setName(merges.get(i).getParents()[0].getName()).call();
            MergeResult res = git.merge().include(merges.get(i).getParents()[1]).call();
            mergeResults.add(res);
        }

        /*
        if (res.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)){
            System.out.println(res.getConflicts().toString());
            // inform the user he has to handle the conflicts
        }
        */

        return mergeResults;
    }

    public static List<String> getTags() throws GitAPIException {
        List<Ref> tagList = git.tagList().call();
        List<String> tagNames = new ArrayList<String>(tagList.size());
        for (int i = 0; i < tagList.size(); i++) {
            tagNames.add(tagList.get(i).getName());
        }
        return tagNames;
    }

    public static void init(String localPath, String remotePath) throws IOException {
        //localPath = "/home/martin/hiwi_job/projekte/voldemort";
        //remotePath = "https://github.com/voldemort/voldemort.git";
        localRepo = new FileRepository(localPath + "/.git");
        git = new Git(localRepo);
    }

    public static boolean build () {
        try {
            Process p2 = Runtime.getRuntime().exec("./build.sh");
            p2.waitFor();

            String message = org.apache.commons.io.IOUtils.toString(p2.getInputStream());

            return message.contains("BUILD SUCCESSFUL");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
