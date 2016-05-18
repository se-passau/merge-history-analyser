import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoMergeBaseException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.Merger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by martin on 18.05.16.
 */
public class Test {
    public static void main(String[] args) throws IOException, GitAPIException {
        Repository localRepo = new FileRepository("/home/martin/hiwi_job/projekte/voldemort/.git");
        Git git = new Git(localRepo);



        List<RevCommit> merges = getMergeCommits(null, null, git);
        System.out.println(merges.get(0).getName());
        RevCommit parent1 = merges.get(0).getParent(0);
        System.out.println(parent1.getName());
        RevCommit parent2 = merges.get(0).getParent(1);
        System.out.println(parent2.getName());




        Repository db = localRepo;
        ObjectInserter inserter = db.newObjectInserter();
        ObjectReader reader = inserter.newReader();
        RevWalk walk = new RevWalk(reader);

        walk.reset();
        walk.setRevFilter(RevFilter.MERGE_BASE);
        walk.markStart(parent1);
        walk.markStart(parent1);
        final RevCommit base = walk.next();
        if (base == null) {
//            System.out.println("null");
        }
        final RevCommit base2 = walk.next();
        if (base2 != null) {
            throw new NoMergeBaseException(
                    NoMergeBaseException.MergeBaseFailureReason.MULTIPLE_MERGE_BASES_NOT_SUPPORTED,
                    MessageFormat.format(
                            JGitText.get().multipleMergeBasesFor, parent1.name(), parent2.name(),
                            base.name(), base2.name()));
        }
        System.out.println(base);
    }


    public static List<RevCommit> getMergeCommits(String start, String end, Git git) {
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
            e.printStackTrace();
        }

        return merges;
    }

}
