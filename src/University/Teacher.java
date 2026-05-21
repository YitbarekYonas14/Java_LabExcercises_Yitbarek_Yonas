package University;

import java.io.Serializable;

public class Teacher implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int id;
    public String name;
    public String department;
    public String subject;
    public int experience;
    
    public Teacher(int id, String name, String department, String subject, int experience) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.subject = subject;
        this.experience = experience;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getSubject() { return subject; }
    public int getExperience() { return experience; }
}
