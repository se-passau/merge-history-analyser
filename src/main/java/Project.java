import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NotMergedException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import util.Pair;
import util.UTIL;

import java.io.IOException;
import java.util.*;

/**
 * Created by martin on 14.11.15.
 */
public class Project {
    String localPath;
    String remotePath;
    Repository localRepo;
    Git git;
    List<MergeScenario> mergeScenarios;
    String buildCommand;

    public Project(String localPath, String remotePath, String buildCommand) {
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.buildCommand = buildCommand;
        mergeScenarios = new LinkedList<MergeScenario>();
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

        //Checkout master
        try {
            git.checkout().setName("master").call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }


        List<RevCommit> gitMerges = null;
        try {
            gitMerges = getMergeScenarios();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        Iterator<RevCommit> it = gitMerges.iterator();

        while (it.hasNext()) {
            RevCommit commit = it.next();
            MergeScenario mergeScenario = new MergeScenario(commit);
            mergeScenarios.add(mergeScenario);
            try {
                //Merge
                MergeResult mergeResult = getMergeResult(commit);
                mergeScenario.getMerge().setMergeResult(mergeResult);

                if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
                    break;
                } else {
                }

                //Build
                Pair<String, Double> pair = build();
                mergeScenario.getBuild().setBuildMessage(pair.getFst());
                mergeScenario.getBuild().setRuntime(pair.getScd());

                //Tests

            } catch (NotMergedException e) {
                mergeScenario.getMerge().addException(e);
            } catch (GitAPIException e) {
                mergeScenario.getMerge().addException(e);
            } catch (InterruptedException e) {
                mergeScenario.getBuild().addException(e);
            } catch (IOException e) {
                mergeScenario.getBuild().addException(e);
            }
            UTIL.writeFile(toString());
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

    public Pair<String, Double> build() throws IOException, InterruptedException {
        double startTime = System.currentTimeMillis();
        Process p2 = Runtime.getRuntime().exec(buildCommand);
        p2.waitFor();
        double runtime = System.currentTimeMillis() - startTime;

        String message = org.apache.commons.io.IOUtils.toString(p2.getInputStream());

        return new Pair(message, runtime);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        Iterator iterator = mergeScenarios.iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next());
            builder.append("\n");
        }
        return builder.toString();
    }

}
