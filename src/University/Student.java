package University;

import java.io.Serializable;

public class Student implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int id;
    public String name;
    public String department;
    public char section;
    public int year;
    
    public Student(int id, String name, String department, char section, int year) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.section = section;
        this.year = year;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public char getSection() { return section; }
    public int getYear() { return year; }
}
