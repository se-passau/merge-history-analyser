import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NotMergedException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.xml.sax.InputSource;
import util.UTIL;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

/**
 * Created by martin on 30.09.15.
 */
public class MergeHistoryAnalyser {



    public MergeHistoryAnalyser(String localPath, String remotePath) {


    }


    public static void main(String[] args) {

        Project project = new Project("/home/martin/hiwi_job/projekte/voldemort", "https://github.com/voldemort/voldemort.git", "./buildVoldemort.sh");

        project.analyse(3);

        XStream xstream = new XStream(new StaxDriver());
        xstream.processAnnotations(Project.class);

        //Object to XML Conversion
        String xml = xstream.toXML(project);
        System.out.println(formatXml(xml));
        UTIL.writeFile("log", xml);

    }

    /*
    public static void main(String[] args) {
        String USAGE = "Usage: MergeHistoryAnalyer [local Repo] [remote Repo]\n";

        if (args.length != 2) {
            System.err.println(USAGE);
        } else {
            MergeHistoryAnalyser analyer = new MergeHistoryAnalyser(args[0], args[1]);

        }
    }
    */

    public static String formatXml(String xml){

        try{
            Transformer serializer = SAXTransformerFactory.newInstance().newTransformer();

            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            Source xmlSource = new SAXSource(new InputSource(new ByteArrayInputStream(xml.getBytes())));
            StreamResult res =  new StreamResult(new ByteArrayOutputStream());

            serializer.transform(xmlSource, res);

            return new String(((ByteArrayOutputStream)res.getOutputStream()).toByteArray());

        }catch(Exception e){
            return xml;
        }
    }
}
