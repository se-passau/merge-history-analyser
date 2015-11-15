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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by martin on 14.11.15.
 */
public class Project {

    @XStreamAsAttribute
    String name;

    @XStreamOmitField
    String localPath;

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

        while (it.hasNext() && numberOfAnalysis > 0) {
            numberOfAnalysis--;

            RevCommit commit = it.next();
            MergeScenario mergeScenario = new MergeScenario(commit.getName(), commit.getParents()[0].getName(), commit.getParents()[1].getName());
            mergeScenarios.add(mergeScenario);
            try {
                //Merge
                MergeResult mergeResult = getMergeResult(commit);

                mergeScenario.getMerge().setStatus(mergeResult.getMergeStatus().name());

                if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
                    break;
                } else {
                }

                //Build
                String buildMessage = build();
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
            }
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

    public String build() throws IOException, InterruptedException {
        double startTime = System.currentTimeMillis();
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
        double time = Double.parseDouble(rawTime.split(" ")[0]);
        return time;
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
