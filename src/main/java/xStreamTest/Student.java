package xStreamTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 15.11.15.
 */
class Student {
    private String studentName;
    private List<Note> notes = new ArrayList<Note>();

    public Student(String name) {
        this.studentName = name;
    }

    public void addNote(Note note) {
        notes.add(note);
    }

    public String getName(){
        return studentName;
    }

    public List<Note> getNotes(){
        return notes;
    }
}
