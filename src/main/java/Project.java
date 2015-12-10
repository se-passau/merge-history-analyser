import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.*;

/**
 * Created by martin on 14.11.15.
 */
public class Project {

    @XStreamAsAttribute
    String name;

    @XStreamOmitField
    String localPath;

    @XStreamAlias("url")
    @XStreamAsAttribute
    String remotePath;

    @XStreamOmitField
    Repository localRepo;

    @XStreamOmitField
    Git git;

    @XStreamImplicit
    List<MergeScenario> mergeScenarios;

    @XStreamAsAttribute
    String buildCommand;

    public Project(String localPath, String remotePath, String buildCommand) {
        name = localPath.substring(localPath.lastIndexOf("/") + 1);
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.buildCommand = buildCommand;
        mergeScenarios = new LinkedList<>();
        //init
        try {
            localRepo = new FileRepository(localPath + "/.git");
            git = new Git(localRepo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Analyses all merge commits.
     */
    public void analyse() {
        checkoutMaster();

        List<RevCommit> mergeCommits = null;
        try {
            mergeCommits = getMergeCommits();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        if (mergeCommits != null) {
            this.mergeScenarios = analyseMergeScenarios(mergeCommits);
        }
        checkoutMaster();
    }

    /**
     * Analyses commites with given commit IDs.
     * @param commitIDs commit IDs of the commits which shall be analysed
     */
    public void analyse(List<String> commitIDs) {
        checkoutMaster();

        List<RevCommit> mergeCommits = null;
        try {
            mergeCommits = getMergeCommits();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        if (mergeCommits != null) {
            List<RevCommit> mergeCommitsToBeAnalysed = new LinkedList<>();
            for (RevCommit commit : mergeCommits) {
                if (commitIDs.contains(commit.getId().name())) {
                    mergeCommitsToBeAnalysed.add(commit);
                }
            }
            this.mergeScenarios = analyseMergeScenarios(mergeCommitsToBeAnalysed);
        }
        checkoutMaster();
    }

    /**
     * Analyses the first {@param numberOfAnalysis} commits
     *
     * @param start index to start with
     * @param end index to end with
     */
    public void analyseFromTo(int start, int end) {
        checkoutMaster();

        List<RevCommit> mergeCommits = null;
        try {
            mergeCommits = getMergeCommits();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        if (mergeCommits != null) {
            System.out.println(mergeCommits.size() + " merges found");
            //Collections.reverse(mergeCommits);
            this.mergeScenarios = analyseMergeScenarios(mergeCommits.subList(start, end));
        } else {
            System.out.println("No merges found");
        }
        checkoutMaster();
    }

    /**
     * Return the index of a commit in the list of all merge commits.
     * @param commitID ID of commit which index is requested
     * @return index of the commit. Return -1 if there is no such merge commit.
     */
    public int mergeIndexOf(String commitID) {
        checkoutMaster();

        List<RevCommit> mergeCommits = null;
        try {
            mergeCommits = getMergeCommits();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        if (mergeCommits != null) {
            for (int i=0; i < mergeCommits.size(); i++) {
                if (mergeCommits.get(i).getId().getName().equals(commitID)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public List<MergeScenario> analyseMergeScenarios(List<RevCommit> mergeCommits) {
        System.out.println("Analysing " + mergeCommits.size() + " merges");
        List<MergeScenario> mergeScenarios = new ArrayList<>(mergeCommits.size());
        for (int i = 0; i < mergeCommits.size(); i++) {
            RevCommit commit = mergeCommits.get(i);
            MergeScenario mergeScenario = analyseMergeScenario(commit);
            mergeScenarios.add(mergeScenario);

            System.out.println("Finished " + (i + 1) + "/" + mergeCommits.size() + "   " + commit.getId().getName());
        }
        return mergeScenarios;
    }

    public MergeScenario analyseMergeScenario(RevCommit mergeCommit) {
        checkoutMaster();

        MergeScenario mergeScenario = new MergeScenario(
                mergeCommit.getName(), mergeCommit.getParents()[0].getName(), mergeCommit.getParents()[1].getName());
        try {
            //Merge
            MergeResult mergeResult = getMergeResult(mergeCommit);

            mergeScenario.getMerge().setState(mergeResult.getMergeStatus().name());

            if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
                throw new MyMergeConflictException(mergeResult);
            }

            //Build
            String buildMessage = build();
            if (buildMessage.isEmpty()) {
                throw new MyNotBuildException();
            }
            String state = getStateOutOfBuild(buildMessage);
            double runtime = getRuntimeOutOfBuild(buildMessage);
            mergeScenario.getBuild().setState(state);
            mergeScenario.getBuild().setRuntime(runtime);

            //Tests

        }
        //Merge Exceptions
        catch (GitAPIException e) {
            mergeScenario.getMerge().setState("GitAPIException");
        } catch (MyMergeConflictException e) {
            Set<String> keySet = e.getMergeResult().getConflicts().keySet();
            Set<String> conflicts = new HashSet<>(keySet);
            mergeScenario.getMerge().setConflicts(conflicts);
        }
        //Build Exceptions
        catch (InterruptedException e) {
            mergeScenario.getBuild().setState("Interrupted");
        } catch (IOException e) {
            mergeScenario.getBuild().setState("IOException");
        } catch (MyNotBuildException e) {
            mergeScenario.getBuild().setState("not build");
        }

        checkoutMaster();

        return mergeScenario;
    }

    public class MyMergeConflictException extends Exception {
        MergeResult mergeResult;

        public MyMergeConflictException(MergeResult mergeResult) {
            this.mergeResult = mergeResult;
        }

        public MergeResult getMergeResult() {
            return mergeResult;
        }
    }

    public class MyNotBuildException extends Exception {
    }

    public List<RevCommit> getMergeCommits() throws GitAPIException {
        Iterable<RevCommit> log = git.log().call();
        Iterator<RevCommit> it = log.iterator();
        List<RevCommit> merges = new LinkedList<>();
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
        return git.merge().include(merge.getParents()[1]).call();
    }

    public String build() throws IOException, InterruptedException {
        Process p2 = Runtime.getRuntime().exec(buildCommand);
        p2.waitFor();

        return org.apache.commons.io.IOUtils.toString(p2.getInputStream());
    }

    public String getStateOutOfBuild(String buildMessage) {
        if (buildMessage.contains("BUILD SUCCESSFUL")) {
            return "PASSED";
        } else {
            return "FAILED";
        }
    }

    public Double getRuntimeOutOfBuild(String buildMessage) {
        String rawTime = buildMessage.substring(buildMessage.lastIndexOf("Total time: ") + 12);
        return Double.parseDouble(rawTime.split(" ")[0]);
    }

    public void checkoutMaster() {
        try {
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            git.checkout().setForce(true).setName("master").call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Object mergeScenario : mergeScenarios) {
            builder.append(mergeScenario);
            builder.append("\n");
        }
        return builder.toString();
    }
}
