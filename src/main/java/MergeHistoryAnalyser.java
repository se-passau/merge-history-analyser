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



    public MergeHistoryAnalyser(String localPath, String remotePath) {


    }





    public static void main(String[] args) {
        String USAGE = "Usage: MergeHistoryAnalyer [local Repo] [remote Repo]\n";

        if (args.length != 2) {
            System.err.println(USAGE);
        } else {
            MergeHistoryAnalyser analyer = new MergeHistoryAnalyser(args[0], args[1]);

        }


    }
}
