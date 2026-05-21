package UniversityJavaFX;
import java.io.*;
import java.util.ArrayList;
import java.sql.*;

public class Teacher implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/university_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    int id;
    String name;
    String department;
    String subject;
    int experience;
    
    Teacher(int id, String name, String department, String subject, int experience) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.subject = subject;
        this.experience = experience;
    }
    
    
    public static void addTeacher(ArrayList<Teacher> t) {
        try {
            FileOutputStream f = new FileOutputStream("teacher.ser");
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(t);
            o.close();
            f.close();
            System.out.println("✓ Teachers saved to file successfully!");
        } catch (Exception e) {
            System.out.print("Error saving to file: " + e.getMessage());
        }
    }
    
    public static void showTeacher() {
        try {
            FileInputStream f = new FileInputStream("teacher.ser");
            ObjectInputStream o = new ObjectInputStream(f);
            ArrayList<Teacher> t1 = (ArrayList<Teacher>) o.readObject();
            System.out.println("\n=== TEACHERS FROM FILE ===");
            System.out.println("Id\tName\t\tDepartment\t\tSubject\t\tExperience");
            System.out.println("------------------------------------------------");
            for (Teacher t : t1) {
                System.out.printf("%d\t%-15s\t%-20s\t%-15s\t%d years%n", 
                                  t.id, t.name, t.department, t.subject, t.experience);
            }
            o.close();
            f.close();
        } catch (Exception e) {
            System.out.print("Error reading from file: " + e.getMessage());
        }
    }
    
    public static void saveTeacherToDatabase(Teacher teacher) {
        String sql = "INSERT INTO teachers (id, name, department, subject, experience) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, teacher.id);
            pstmt.setString(2, teacher.name);
            pstmt.setString(3, teacher.department);
            pstmt.setString(4, teacher.subject);
            pstmt.setInt(5, teacher.experience);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✓ Teacher " + teacher.name + " saved to database! (ID: " + teacher.id + ")");
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            if (e.getErrorCode() == 1062) {
                System.err.println("Hint: A teacher with ID " + teacher.id + " already exists!");
            }
        }
    }
    
    public static void readTeachersFromDatabase() {
        String sql = "SELECT * FROM teachers ORDER BY id";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String department = rs.getString("department");
                String subject = rs.getString("subject");
                int experience = rs.getInt("experience");
                
                System.out.printf("%d\t%-15s\t%-20s\t%-15s\t%d years%n", 
                                  id, name, department, subject, experience);
            }
            
            if (!hasResults) {
                System.out.println("No teachers found in database.");
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            System.err.println("Make sure XAMPP is running and the database/table exists!");
        }
    }
    

    public static void updateTeacherInDatabase(int id, String newName, int newExperience) {
        String sql = "UPDATE teachers SET name = ?, experience = ? WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newName);
            pstmt.setInt(2, newExperience);
            pstmt.setInt(3, id);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Teacher ID " + id + " updated successfully!");
            } else {
                System.out.println("No teacher found with ID: " + id);
            }
            
        } catch (SQLException e) {
            System.err.println("Update error: " + e.getMessage());
        }
    }
    
    public static void deleteTeacherFromDatabase(int id) {
        String sql = "DELETE FROM teachers WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Teacher ID " + id + " deleted from database!");
            } else {
                System.out.println("No teacher found with ID: " + id);
            }
            
        } catch (SQLException e) {
            System.err.println("Delete error: " + e.getMessage());
        }
    }
}
