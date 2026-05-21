package UniversityRMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.ArrayList;
import java.sql.*;

public class ServerImpl extends UnicastRemoteObject implements RemoteInterface {
    
    private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/university_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    private static final String STUDENT_FILE = "student.ser";
    private static final String TEACHER_FILE = "teacher.ser";
    
    public ServerImpl() throws RemoteException {
        super();
    }
    
    // ==================== STUDENT METHODS ====================
    
    @Override
    public String addStudentToFile(Student student) throws RemoteException {
        StringBuilder result = new StringBuilder();
        try {
            ArrayList<Student> existingStudents = new ArrayList<>();
            
            
            File file = new File(STUDENT_FILE);
            if (file.exists()) {
                try (FileInputStream f = new FileInputStream(STUDENT_FILE);
                     ObjectInputStream o = new ObjectInputStream(f)) {
                    existingStudents = (ArrayList<Student>) o.readObject();
                } catch (Exception e) {
    
                    result.append("Warning: Existing file corrupted, creating new file.\n");
                }
            }
            
            
            for (Student s : existingStudents) {
                if (s.id == student.id) {
                    return "❌ Error: Student with ID " + student.id + " already exists in file!\n";
                }
            }
            
            existingStudents.add(student);
            
            
            try (FileOutputStream f = new FileOutputStream(STUDENT_FILE);
                 ObjectOutputStream o = new ObjectOutputStream(f)) {
                o.writeObject(existingStudents);
            }
            
            result.append("✅ Student '").append(student.name).append("' (ID: ")
                  .append(student.id).append(") saved to file successfully!\n");
            
        } catch (Exception e) {
            result.append("❌ Error adding student to file: ").append(e.getMessage()).append("\n");
        }
        return result.toString();
    }
    
    @Override
    public String showStudentsFromFile() throws RemoteException {
        StringBuilder result = new StringBuilder();
        result.append("=== 📚 STUDENTS FROM FILE ===\n");
        result.append("ID\tName\t\tDepartment\t\tSection\tYear\n");
        result.append("------------------------------------------------\n");
        
        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            result.append("No student file found. Please add students first.\n");
            return result.toString();
        }
        
        try (FileInputStream f = new FileInputStream(STUDENT_FILE);
             ObjectInputStream o = new ObjectInputStream(f)) {
            
            ArrayList<Student> students = (ArrayList<Student>) o.readObject();
            if (students.isEmpty()) {
                result.append("No students found in file.\n");
            } else {
                for (Student s : students) {
                    result.append(String.format("%d\t%-15s\t%-20s\t%c\t%d\n", 
                        s.id, s.name, s.department, s.section, s.year));
                }
                result.append("\n✅ Total students: ").append(students.size()).append("\n");
            }
            
        } catch (Exception e) {
            result.append("Error reading from file: ").append(e.getMessage()).append("\n");
        }
        return result.toString();
    }
    
    @Override
    public String saveStudentToDatabase(Student student) throws RemoteException {
        StringBuilder result = new StringBuilder();
        String sql = "INSERT INTO students (id, name, department, section, year) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, student.id);
            pstmt.setString(2, student.name);
            pstmt.setString(3, student.department);
            pstmt.setString(4, String.valueOf(student.section));
            pstmt.setInt(5, student.year);
            
            int rowsAffected = pstmt.executeUpdate();
            result.append("✓ Student ").append(student.name).append(" saved to database! (ID: ").append(student.id).append(")\n");
            
        } catch (SQLException e) {
            result.append("Database error: ").append(e.getMessage()).append("\n");
            if (e.getErrorCode() == 1062) {
                result.append("Hint: A student with ID ").append(student.id).append(" already exists!\n");
            }
        }
        return result.toString();
    }
    
    @Override
    public String readStudentsFromDatabase() throws RemoteException {
        StringBuilder result = new StringBuilder();
        String sql = "SELECT * FROM students ORDER BY id";
        
        result.append("=== 🗄️ STUDENTS FROM DATABASE ===\n");
        result.append("ID\tName\t\tDepartment\t\tSection\tYear\n");
        result.append("------------------------------------------------\n");
        
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
                
                result.append(String.format("%d\t%-15s\t%-20s\t%c\t%d\n", 
                                  id, name, department, section, year));
            }
            
            if (!hasResults) {
                result.append("No students found in database.\n");
            }
            
        } catch (SQLException e) {
            result.append("Database error: ").append(e.getMessage()).append("\n");
            result.append("Make sure XAMPP is running and the database/table exists!\n");
        }
        return result.toString();
    }
    
    @Override
    public String updateStudentInDatabase(int id, String newName, int newYear) throws RemoteException {
        StringBuilder result = new StringBuilder();
        
        
        if (newName == null || newName.isEmpty()) {
            String sql = "UPDATE students SET year = ? WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, newYear);
                pstmt.setInt(2, id);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    result.append("✓ Student ID ").append(id).append(" year updated to ").append(newYear).append("!\n");
                } else {
                    result.append("No student found with ID: ").append(id).append("\n");
                }
            } catch (SQLException e) {
                result.append("Update error: ").append(e.getMessage()).append("\n");
            }
        } else if (newYear == 0) {
            String sql = "UPDATE students SET name = ? WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, newName);
                pstmt.setInt(2, id);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    result.append("✓ Student ID ").append(id).append(" name updated to ").append(newName).append("!\n");
                } else {
                    result.append("No student found with ID: ").append(id).append("\n");
                }
            } catch (SQLException e) {
                result.append("Update error: ").append(e.getMessage()).append("\n");
            }
        } else {
            String sql = "UPDATE students SET name = ?, year = ? WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, newName);
                pstmt.setInt(2, newYear);
                pstmt.setInt(3, id);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    result.append("✓ Student ID ").append(id).append(" updated successfully!\n");
                } else {
                    result.append("No student found with ID: ").append(id).append("\n");
                }
            } catch (SQLException e) {
                result.append("Update error: ").append(e.getMessage()).append("\n");
            }
        }
        return result.toString();
    }
    
    @Override
    public String deleteStudentFromDatabase(int id) throws RemoteException {
        StringBuilder result = new StringBuilder();
        String sql = "DELETE FROM students WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                result.append("✓ Student ID ").append(id).append(" deleted from database!\n");
            } else {
                result.append("No student found with ID: ").append(id).append("\n");
            }
            
        } catch (SQLException e) {
            result.append("Delete error: ").append(e.getMessage()).append("\n");
        }
        return result.toString();
    }
    
    // ==================== TEACHER METHODS ====================
    
    @Override
    public String addTeacherToFile(Teacher teacher) throws RemoteException {
        StringBuilder result = new StringBuilder();
        try {
            ArrayList<Teacher> existingTeachers = new ArrayList<>();
            
            
            File file = new File(TEACHER_FILE);
            if (file.exists()) {
                try (FileInputStream f = new FileInputStream(TEACHER_FILE);
                     ObjectInputStream o = new ObjectInputStream(f)) {
                    existingTeachers = (ArrayList<Teacher>) o.readObject();
                } catch (Exception e) {
                    result.append("Warning: Existing file corrupted, creating new file.\n");
                }
            }
            
            
            for (Teacher t : existingTeachers) {
                if (t.id == teacher.id) {
                    return "❌ Error: Teacher with ID " + teacher.id + " already exists in file!\n";
                }
            }
            
            existingTeachers.add(teacher);
            
            
            try (FileOutputStream f = new FileOutputStream(TEACHER_FILE);
                 ObjectOutputStream o = new ObjectOutputStream(f)) {
                o.writeObject(existingTeachers);
            }
            
            result.append("✅ Teacher '").append(teacher.name).append("' (ID: ")
                  .append(teacher.id).append(") saved to file successfully!\n");
            
        } catch (Exception e) {
            result.append("❌ Error adding teacher to file: ").append(e.getMessage()).append("\n");
        }
        return result.toString();
    }
    
    @Override
    public String showTeachersFromFile() throws RemoteException {
        StringBuilder result = new StringBuilder();
        result.append("=== 📚 TEACHERS FROM FILE ===\n");
        result.append("ID\tName\t\tDepartment\t\tSubject\t\tExperience\n");
        result.append("------------------------------------------------\n");
        
        File file = new File(TEACHER_FILE);
        if (!file.exists()) {
            result.append("No teacher file found. Please add teachers first.\n");
            return result.toString();
        }
        
        try (FileInputStream f = new FileInputStream(TEACHER_FILE);
             ObjectInputStream o = new ObjectInputStream(f)) {
            
            ArrayList<Teacher> teachers = (ArrayList<Teacher>) o.readObject();
            if (teachers.isEmpty()) {
                result.append("No teachers found in file.\n");
            } else {
                for (Teacher t : teachers) {
                    result.append(String.format("%d\t%-15s\t%-20s\t%-15s\t%d years\n", 
                        t.id, t.name, t.department, t.subject, t.experience));
                }
                result.append("\n✅ Total teachers: ").append(teachers.size()).append("\n");
            }
            
        } catch (Exception e) {
            result.append("Error reading from file: ").append(e.getMessage()).append("\n");
        }
        return result.toString();
    }
    
    @Override
    public String saveTeacherToDatabase(Teacher teacher) throws RemoteException {
        StringBuilder result = new StringBuilder();
        String sql = "INSERT INTO teachers (id, name, department, subject, experience) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, teacher.id);
            pstmt.setString(2, teacher.name);
            pstmt.setString(3, teacher.department);
            pstmt.setString(4, teacher.subject);
            pstmt.setInt(5, teacher.experience);
            
            int rowsAffected = pstmt.executeUpdate();
            result.append("✓ Teacher ").append(teacher.name).append(" saved to database! (ID: ").append(teacher.id).append(")\n");
            
        } catch (SQLException e) {
            result.append("Database error: ").append(e.getMessage()).append("\n");
            if (e.getErrorCode() == 1062) {
                result.append("Hint: A teacher with ID ").append(teacher.id).append(" already exists!\n");
            }
        }
        return result.toString();
    }
    
    @Override
    public String readTeachersFromDatabase() throws RemoteException {
        StringBuilder result = new StringBuilder();
        String sql = "SELECT * FROM teachers ORDER BY id";
        
        result.append("=== 🗄️ TEACHERS FROM DATABASE ===\n");
        result.append("ID\tName\t\tDepartment\t\tSubject\t\tExperience\n");
        result.append("------------------------------------------------\n");
        
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
                
                result.append(String.format("%d\t%-15s\t%-20s\t%-15s\t%d years\n", 
                                  id, name, department, subject, experience));
            }
            
            if (!hasResults) {
                result.append("No teachers found in database.\n");
            }
            
        } catch (SQLException e) {
            result.append("Database error: ").append(e.getMessage()).append("\n");
            result.append("Make sure XAMPP is running and the database/table exists!\n");
        }
        return result.toString();
    }
    
    @Override
    public String updateTeacherInDatabase(int id, String newName, int newExperience) throws RemoteException {
        StringBuilder result = new StringBuilder();
        
        
        if (newName == null || newName.isEmpty()) {
            String sql = "UPDATE teachers SET experience = ? WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, newExperience);
                pstmt.setInt(2, id);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    result.append("✓ Teacher ID ").append(id).append(" experience updated to ").append(newExperience).append(" years!\n");
                } else {
                    result.append("No teacher found with ID: ").append(id).append("\n");
                }
            } catch (SQLException e) {
                result.append("Update error: ").append(e.getMessage()).append("\n");
            }
        } else if (newExperience == -1) {
            String sql = "UPDATE teachers SET name = ? WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, newName);
                pstmt.setInt(2, id);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    result.append("✓ Teacher ID ").append(id).append(" name updated to ").append(newName).append("!\n");
                } else {
                    result.append("No teacher found with ID: ").append(id).append("\n");
                }
            } catch (SQLException e) {
                result.append("Update error: ").append(e.getMessage()).append("\n");
            }
        } else {
            String sql = "UPDATE teachers SET name = ?, experience = ? WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, newName);
                pstmt.setInt(2, newExperience);
                pstmt.setInt(3, id);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    result.append("✓ Teacher ID ").append(id).append(" updated successfully!\n");
                } else {
                    result.append("No teacher found with ID: ").append(id).append("\n");
                }
            } catch (SQLException e) {
                result.append("Update error: ").append(e.getMessage()).append("\n");
            }
        }
        return result.toString();
    }
    
    @Override
    public String deleteTeacherFromDatabase(int id) throws RemoteException {
        StringBuilder result = new StringBuilder();
        String sql = "DELETE FROM teachers WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                result.append("✓ Teacher ID ").append(id).append(" deleted from database!\n");
            } else {
                result.append("No teacher found with ID: ").append(id).append("\n");
            }
            
        } catch (SQLException e) {
            result.append("Delete error: ").append(e.getMessage()).append("\n");
        }
        return result.toString();
    }
}
