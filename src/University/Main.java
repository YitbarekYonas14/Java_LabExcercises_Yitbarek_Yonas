package University;

import java.util.Scanner;

public class Main {
    
    private static Scanner scanner = new Scanner(System.in);
    private static DatabaseFileOperations dbOps = new DatabaseFileOperations();
    
    public static void main(String[] args) {
        
       
        System.out.println("\n" + "=".repeat(60));
        System.out.println("     WELCOME TO UNIVERSITY MANAGEMENT SYSTEM");
        System.out.println("=".repeat(60));
        
       
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    studentMenu();
                    break;
                case 2:
                    teacherMenu();
                    break;
                case 3:
                    running = false;
                    System.out.println("\n" + "=".repeat(50));
                    System.out.println("👋 THANK YOU FOR USING UNIVERSITY MANAGEMENT SYSTEM!");
                    System.out.println("=".repeat(50) + "\n");
                    break;
                default:
                    System.out.println("\n❌ Invalid choice! Please enter 1-3.\n");
            }
        }
        
        scanner.close();
    }
    
    private static void displayMainMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("              📚 MAIN MENU");
        System.out.println("=".repeat(50));
        System.out.println("   1. 🎓 STUDENT Operations");
        System.out.println("   2. 👨‍🏫 TEACHER Operations");
        System.out.println("   3. 🚪 EXIT");
        System.out.println("=".repeat(50));
        System.out.print("👉 Enter your choice: ");
    }
    
    private static void studentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + "-".repeat(50));
            System.out.println("           🎓 STUDENT OPERATIONS");
            System.out.println("-".repeat(50));
            System.out.println("   1. 📝 Add Student to FILE");
            System.out.println("   2. 📖 Show Students from FILE");
            System.out.println("   3. 💾 Save Student to DATABASE");
            System.out.println("   4. 🔍 Read Students from DATABASE");
            System.out.println("   5. ✏️ Update Student in DATABASE");
            System.out.println("   6. 🗑️ Delete Student from DATABASE");
            System.out.println("   7. ➕ Add NEW Student (to both File & DB)");
            System.out.println("   8. 🔙 Back to Main Menu");
            System.out.println("-".repeat(50));
            System.out.print("👉 Enter your choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    System.out.println("\n" + dbOps.addStudentToFile(getStudentFromUser()));
                    break;
                case 2:
                    System.out.println("\n" + dbOps.showStudentsFromFile());
                    break;
                case 3:
                    System.out.println("\n" + dbOps.saveStudentToDatabase(getStudentFromUser()));
                    break;
                case 4:
                    System.out.println("\n" + dbOps.readStudentsFromDatabase());
                    break;
                case 5:
                    updateStudentInDatabase();
                    break;
                case 6:
                    deleteStudentFromDatabase();
                    break;
                case 7:
                    Student newStudent = getStudentFromUser();
                    System.out.println("\n" + dbOps.addStudentToFile(newStudent));
                    System.out.println(dbOps.saveStudentToDatabase(newStudent));
                    break;
                case 8:
                    back = true;
                    break;
                default:
                    System.out.println("\n❌ Invalid choice! Please enter 1-8.\n");
            }
        }
    }
    
    private static void teacherMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + "-".repeat(50));
            System.out.println("           👨‍🏫 TEACHER OPERATIONS");
            System.out.println("-".repeat(50));
            System.out.println("   1. 📝 Add Teacher to FILE");
            System.out.println("   2. 📖 Show Teachers from FILE");
            System.out.println("   3. 💾 Save Teacher to DATABASE");
            System.out.println("   4. 🔍 Read Teachers from DATABASE");
            System.out.println("   5. ✏️ Update Teacher in DATABASE");
            System.out.println("   6. 🗑️ Delete Teacher from DATABASE");
            System.out.println("   7. ➕ Add NEW Teacher (to both File & DB)");
            System.out.println("   8. 🔙 Back to Main Menu");
            System.out.println("-".repeat(50));
            System.out.print("👉 Enter your choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    System.out.println("\n" + dbOps.addTeacherToFile(getTeacherFromUser()));
                    break;
                case 2:
                    System.out.println("\n" + dbOps.showTeachersFromFile());
                    break;
                case 3:
                    System.out.println("\n" + dbOps.saveTeacherToDatabase(getTeacherFromUser()));
                    break;
                case 4:
                    System.out.println("\n" + dbOps.readTeachersFromDatabase());
                    break;
                case 5:
                    updateTeacherInDatabase();
                    break;
                case 6:
                    deleteTeacherFromDatabase();
                    break;
                case 7:
                    Teacher newTeacher = getTeacherFromUser();
                    System.out.println("\n" + dbOps.addTeacherToFile(newTeacher));
                    System.out.println(dbOps.saveTeacherToDatabase(newTeacher));
                    break;
                case 8:
                    back = true;
                    break;
                default:
                    System.out.println("\n❌ Invalid choice! Please enter 1-8.\n");
            }
        }
    }
    
    private static Student getStudentFromUser() {
        System.out.println("\n📝 Enter Student Details:");
        System.out.print("   ID: ");
        int id = getIntInput();
        System.out.print("   Name: ");
        String name = scanner.next();
        System.out.print("   Department: ");
        String department = scanner.next();
        System.out.print("   Section (A/B/C/D): ");
        char section = scanner.next().charAt(0);
        System.out.print("   Year: ");
        int year = getIntInput();
        
        return new Student(id, name, department, section, year);
    }
    
    private static Teacher getTeacherFromUser() {
        System.out.println("\n📝 Enter Teacher Details:");
        System.out.print("   ID: ");
        int id = getIntInput();
        System.out.print("   Name: ");
        String name = scanner.next();
        System.out.print("   Department: ");
        String department = scanner.next();
        System.out.print("   Subject: ");
        String subject = scanner.next();
        System.out.print("   Experience (years): ");
        int experience = getIntInput();
        
        return new Teacher(id, name, department, subject, experience);
    }
    
    private static void updateStudentInDatabase() {
        System.out.println("\n✏️ UPDATE STUDENT IN DATABASE");
        System.out.print("   Enter Student ID to update: ");
        int id = getIntInput();
        
        System.out.println("\n   What do you want to update?");
        System.out.println("      1. Update Name only");
        System.out.println("      2. Update Year only");
        System.out.println("      3. Update Both Name and Year");
        System.out.print("      Choice: ");
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                System.out.print("   Enter new Name: ");
                String newName = scanner.next();
                System.out.println("\n" + dbOps.updateStudentInDatabase(id, newName, 0));
                break;
            case 2:
                System.out.print("   Enter new Year: ");
                int newYear = getIntInput();
                System.out.println("\n" + dbOps.updateStudentInDatabase(id, "", newYear));
                break;
            case 3:
                System.out.print("   Enter new Name: ");
                newName = scanner.next();
                System.out.print("   Enter new Year: ");
                newYear = getIntInput();
                System.out.println("\n" + dbOps.updateStudentInDatabase(id, newName, newYear));
                break;
            default:
                System.out.println("\n❌ Invalid choice!\n");
        }
    }
    
    private static void deleteStudentFromDatabase() {
        System.out.println("\n🗑️ DELETE STUDENT FROM DATABASE");
        System.out.print("   Enter Student ID to delete: ");
        int id = getIntInput();
        System.out.println("\n" + dbOps.deleteStudentFromDatabase(id));
    }
    
    private static void updateTeacherInDatabase() {
        System.out.println("\n✏️ UPDATE TEACHER IN DATABASE");
        System.out.print("   Enter Teacher ID to update: ");
        int id = getIntInput();
        
        System.out.println("\n   What do you want to update?");
        System.out.println("      1. Update Name only");
        System.out.println("      2. Update Experience only");
        System.out.println("      3. Update Both Name and Experience");
        System.out.print("      Choice: ");
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                System.out.print("   Enter new Name: ");
                String newName = scanner.next();
                System.out.println("\n" + dbOps.updateTeacherInDatabase(id, newName, -1));
                break;
            case 2:
                System.out.print("   Enter new Experience (years): ");
                int newExp = getIntInput();
                System.out.println("\n" + dbOps.updateTeacherInDatabase(id, "", newExp));
                break;
            case 3:
                System.out.print("   Enter new Name: ");
                newName = scanner.next();
                System.out.print("   Enter new Experience (years): ");
                newExp = getIntInput();
                System.out.println("\n" + dbOps.updateTeacherInDatabase(id, newName, newExp));
                break;
            default:
                System.out.println("\n❌ Invalid choice!\n");
        }
    }
    
    private static void deleteTeacherFromDatabase() {
        System.out.println("\n🗑️ DELETE TEACHER FROM DATABASE");
        System.out.print("   Enter Teacher ID to delete: ");
        int id = getIntInput();
        System.out.println("\n" + dbOps.deleteTeacherFromDatabase(id));
    }
    
    private static int getIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.print("   Please enter a valid number: ");
            scanner.next();
        }
        int input = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return input;
    }
}