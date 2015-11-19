import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NotMergedException;
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
        mergeScenarios = new LinkedList<MergeScenario>();
        //init
        try {
            localRepo = new FileRepository(localPath + "/.git");
            git = new Git(localRepo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void analyse(List<String> commitIDs) {
        checkoutMaster();

        List<RevCommit> mergeCommits = null;
        try {
            mergeCommits = getMergeScenarios();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        if (mergeCommits != null) {
            List<RevCommit> mergeCommitsToBeAnalysed = new LinkedList<RevCommit>();
            for (RevCommit commit : mergeCommits) {
                if (commitIDs.contains(commit.getId().name())) {
                    mergeCommitsToBeAnalysed.add(commit);
                }
            }

            this.mergeScenarios = analyseMergeScenarios(mergeCommitsToBeAnalysed);
        }
        checkoutMaster();
    }

    public void analyse() {
        checkoutMaster();

        List<RevCommit> mergeCommits = null;
        try {
            mergeCommits = getMergeScenarios();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        if (mergeCommits != null) {
            this.mergeScenarios = analyseMergeScenarios(mergeCommits);
        }
        checkoutMaster();
    }

    public void analyse(int numberOfAnalysis) {
        checkoutMaster();

        List<RevCommit> mergeCommits = null;
        try {
            mergeCommits = getMergeScenarios();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        if (mergeCommits != null) {
            System.out.println(mergeCommits.size() + " merges found");


            //Collections.reverse(mergeCommits);

            this.mergeScenarios = analyseMergeScenarios(mergeCommits.subList(0, numberOfAnalysis));

        } else {
            System.out.println("No merges found");
        }
        checkoutMaster();
    }

    public List<MergeScenario> analyseMergeScenarios(List<RevCommit> commits) {
        System.out.println("Analysing " + commits.size() + " merges");
        List<MergeScenario> mergeScenarios = new ArrayList<MergeScenario>(commits.size());
        for (int i = 0; i < commits.size(); i++) {
            RevCommit commit = commits.get(i);
            MergeScenario mergeScenario = analyseMergeScenario(commit);
            mergeScenarios.add(mergeScenario);

            System.out.println("Finished " + (i + 1) + "/" + commits.size());
        }
        return mergeScenarios;
    }

    public MergeScenario analyseMergeScenario(RevCommit commit) {
        MergeScenario mergeScenario = new MergeScenario(
                commit.getName(), commit.getParents()[0].getName(), commit.getParents()[1].getName());
        try {
            //Merge
            MergeResult mergeResult = getMergeResult(commit);

            mergeScenario.getMerge().setStatus(mergeResult.getMergeStatus().name());

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


        } catch (NotMergedException e) {
            mergeScenario.getMerge().addException(e);
        } catch (GitAPIException e) {
            mergeScenario.getMerge().addException(e);
        } catch (InterruptedException e) {
            mergeScenario.getBuild().addException(e);
        } catch (IOException e) {
            mergeScenario.getBuild().addException(e);
        } catch (MyMergeConflictException e) {
            Map<String, int[][]> conflicts = e.getMergeResult().getConflicts();
            if (conflicts == null) {
                System.out.println("conflicts null");
            } else if (conflicts.isEmpty()) {
                System.out.println("conflicts empty");
            } else {
                for (Object string : conflicts.keySet()) {
                    System.out.println(string);
                }
            }
        } catch (MyNotBuildException e) {
            mergeScenario.getBuild().setState("not build");
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

    public class MyNotBuildException extends Exception {
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

    public String build() throws IOException, InterruptedException {
        Process p2 = Runtime.getRuntime().exec(buildCommand);
        p2.waitFor();

        String message = org.apache.commons.io.IOUtils.toString(p2.getInputStream());

        return message;
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
            //git.reset().call();
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
