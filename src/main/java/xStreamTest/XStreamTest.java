package xStreamTest;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.xml.sax.InputSource;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by martin on 15.11.15.
 */
public class XStreamTest {
    public static void main(String args[]){

        XStreamTest tester = new XStreamTest();
        XStream xstream = new XStream(new StaxDriver());

        xstream.alias("student", Student.class);
        xstream.alias("note", Note.class);
        xstream.useAttributeFor(Student.class, "studentName");
        xstream.aliasField("name", Student.class, "studentName");
        xstream.addImplicitCollection(Student.class, "notes");

        Student student = tester.getStudentDetails();

        //Object to XML Conversion
        String xml = xstream.toXML(student);
        System.out.println(formatXml(xml));
    }

    private Student getStudentDetails(){
        Student student = new Student("Mahesh");

        student.addNote(new Note("first","My first assignment."));
        student.addNote(new Note("second","My Second assignment."));

        return student;
    }

    public static String formatXml(String xml){

        try{
            Transformer serializer = SAXTransformerFactory.newInstance().newTransformer();

            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            Source xmlSource = new SAXSource(new InputSource(new ByteArrayInputStream(xml.getBytes())));
            StreamResult res =  new StreamResult(new ByteArrayOutputStream());

            serializer.transform(xmlSource, res);

            return new String(((ByteArrayOutputStream)res.getOutputStream()).toByteArray());

        } catch(Exception e){
            return xml;
        }
    }
}
