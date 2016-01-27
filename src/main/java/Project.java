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
        try {
            checkoutMaster();
        } catch (MyCheckoutMasterException e) {
            e.printStackTrace();
        }

        List<RevCommit> mergeCommits = null;
        try {
            mergeCommits = getMergeCommits();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        if (mergeCommits != null) {
            this.mergeScenarios = analyseMergeScenarios(mergeCommits);
        }

        try {
            checkoutMaster();
        } catch (MyCheckoutMasterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Analyses commites with given commit IDs.
     *
     * @param commitIDs commit IDs of the commits which shall be analysed
     */
    public void analyse(List<String> commitIDs) {
        try {
            checkoutMaster();
        } catch (MyCheckoutMasterException e) {
            e.printStackTrace();
        }

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

        try {
            checkoutMaster();
        } catch (MyCheckoutMasterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Analyses the first {@param numberOfAnalysis} commits
     *
     * @param start index to start with
     * @param end   index to end with
     */
    public void analyseFromTo(int start, int end) {
        try {
            checkoutMaster();
        } catch (MyCheckoutMasterException e) {
            e.printStackTrace();
        }

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

        try {
            checkoutMaster();
        } catch (MyCheckoutMasterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the index of a commit in the list of all merge commits.
     *
     * @param commitID ID of commit which index is requested
     * @return index of the commit. Return -1 if there is no such merge commit.
     */
    public int mergeIndexOf(String commitID) {
        try {
            checkoutMaster();
        } catch (MyCheckoutMasterException e) {
            e.printStackTrace();
            return -1;
        }

        List<RevCommit> mergeCommits = null;
        try {
            mergeCommits = getMergeCommits();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        if (mergeCommits != null) {
            for (int i = 0; i < mergeCommits.size(); i++) {
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
        MergeScenario mergeScenario = new MergeScenario(
                mergeCommit.getName(), mergeCommit.getParents()[0].getName(), mergeCommit.getParents()[1].getName());
        try {
            checkoutMaster();

            //Merge
            MergeResult mergeResult = getMergeResult(mergeCommit);

            mergeScenario.getMerge().setState(mergeResult.getMergeStatus().name());

            if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
                throw new MyMergeConflictException(mergeResult);
            }

            //Build
            mergeScenario.setBuild(build());

            //Tests

            //checkoutMaster();
        }
        //Checkout Master
        catch (MyCheckoutMasterException e) {
            mergeScenario.getMerge().setState("CHECKOUT MASTER ERROR");
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
        }

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

    public class MyNotMergedException extends Exception {
    }

    public class MyNotBuildException extends Exception {
    }

    public class MyCheckoutMasterException extends Exception {
        public MyCheckoutMasterException (String message) {
            super(message);
        }
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

    public Build build() throws IOException, InterruptedException {
        Process p2 = Runtime.getRuntime().exec(buildCommand);
        p2.waitFor();

        String buildMessage = org.apache.commons.io.IOUtils.toString(p2.getInputStream());
        Build build = new Build();

        if (buildMessage.contains("NO BUILD POSSIBLE")) {
            build.setState("NO BUILD POSSIBLE");
        } else {
            if (buildMessage.contains("BUILD SUCCESSFUL")) {
                build.setState("SUCCESSFUL");
            } else {
                build.setState("FAILED");
            }
            double runtime = getRuntimeOutOfBuild(buildMessage);
            build.setRuntime(runtime);
        }
        return build;
    }

    public Double getRuntimeOutOfBuild(String buildMessage) {
        String rawTime = buildMessage.substring(buildMessage.lastIndexOf("Total time: ") + 12);
        return Double.parseDouble(rawTime.split(" ")[0]);
    }

    public void checkoutMaster() throws MyCheckoutMasterException {
        try {
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef("origin/master").call();
            git.checkout().setForce(true).setName("master").call();
        } catch (GitAPIException e) {
            throw new MyCheckoutMasterException(e.getMessage());
        }
    }
}