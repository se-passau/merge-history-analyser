import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

/**
 * Created by martin on 30.09.15.
 */
public class myGit {

    String localPath, remotePath;
    Repository localRepo;
    Git git;

    public void init() throws IOException {
        localPath = "/home/martin/hiwi_job/projekte/voldemort";
        remotePath = "https://github.com/voldemort/voldemort.git";
        localRepo = new FileRepository(localPath + "/.git");
        git = new Git(localRepo);
    }

    public void testCreate() throws IOException {
        Repository newRepo = new FileRepository(localPath + ".git");
        newRepo.create();
    }

    public void testClone() throws IOException, GitAPIException {
        Git.cloneRepository().setURI(remotePath)
                .setDirectory(new File(localPath)).call();
    }

    public void testAdd(String myFile) throws IOException, GitAPIException {
        File myfile = new File(localPath + "/" + myFile);
        myfile.createNewFile();
        git.add().addFilepattern(myFile).call();
    }

    public void testCommit(String message) throws IOException, GitAPIException,
            JGitInternalException {
        git.commit().setMessage(message).call();
    }

    public void testPush() throws IOException, JGitInternalException,
            GitAPIException {
        git.push().call();
    }

    public void testCheckout(String tag) throws GitAPIException {
        git.checkout().setName(tag).call();
    }

    public void testTrackMaster() throws IOException, JGitInternalException,
            GitAPIException {
        git.branchCreate().setName("master")
                .setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
                .setStartPoint("origin/master").setForce(true).call();
    }

    public void testPull() throws IOException, GitAPIException {
        git.pull().call();
    }

}
