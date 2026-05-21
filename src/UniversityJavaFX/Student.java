package UniversityJavaFX;
import java.io.*;
import java.util.ArrayList;
import java.sql.*;

public class Student implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/university_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    int id;
    String name;
    String department;
    char section;
    int year;
    
    Student(int id, String name, String department, char section, int year) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.section = section;
        this.year = year;
    }
 
    public static void addStudent(ArrayList<Student> s) {
        try {
            FileOutputStream f = new FileOutputStream("student.ser");
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(s);
            o.close();
            f.close();
            System.out.println("✓ Students saved to file successfully!");
        } catch (Exception e) {
            System.out.print("Error saving to file: " + e.getMessage());
        }
    }
    
    public static void showStudent() {
        try {
            FileInputStream f = new FileInputStream("student.ser");
            ObjectInputStream o = new ObjectInputStream(f);
            ArrayList<Student> s1 = (ArrayList<Student>) o.readObject();
            System.out.println("\n=== STUDENTS FROM FILE ===");
            System.out.println("Id\tName\t\tDepartment\t\tSection\tYear");
            System.out.println("------------------------------------------------");
            for (Student s : s1) {
                System.out.printf("%d\t%-15s\t%-20s\t%c\t%d%n", 
                                  s.id, s.name, s.department, s.section, s.year);
            }
            o.close();
            f.close();
        } catch (Exception e) {
            System.out.print("Error reading from file: " + e.getMessage());
        }
    }

    public static void saveStudentToDatabase(Student student) {
        String sql = "INSERT INTO students (id, name, department, section, year) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, student.id);
            pstmt.setString(2, student.name);
            pstmt.setString(3, student.department);
            pstmt.setString(4, String.valueOf(student.section));
            pstmt.setInt(5, student.year);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✓ Student " + student.name + " saved to database! (ID: " + student.id + ")");
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            if (e.getErrorCode() == 1062) {
                System.err.println("Hint: A student with ID " + student.id + " already exists!");
            }
        }
    }
    
    public static void readStudentsFromDatabase() {
        String sql = "SELECT * FROM students ORDER BY id";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
               
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String department = rs.getString("department");
                char section = rs.getString("section").charAt(0);
                int year = rs.getInt("year");
                
                System.out.printf("%d\t%-15s\t%-20s\t%c\t%d%n", 
                                  id, name, department, section, year);
            }
            
            if (!hasResults) {
                System.out.println("No students found in database.");
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            System.err.println("Make sure XAMPP is running and the database/table exists!");
        }
    }
    
    public static void updateStudentInDatabase(int id, String newName, int newYear) {
        String sql = "UPDATE students SET name = ?, year = ? WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newName);
            pstmt.setInt(2, newYear);
            pstmt.setInt(3, id);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Student ID " + id + " updated successfully!");
            } else {
                System.out.println("No student found with ID: " + id);
            }
            
        } catch (SQLException e) {
            System.err.println("Update error: " + e.getMessage());
        }
    }
    
    public static void deleteStudentFromDatabase(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Student ID " + id + " deleted from database!");
            } else {
                System.out.println("No student found with ID: " + id);
            }
            
        } catch (SQLException e) {
            System.err.println("Delete error: " + e.getMessage());
        }
    }
}
