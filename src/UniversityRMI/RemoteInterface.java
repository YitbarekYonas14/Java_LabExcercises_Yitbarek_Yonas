package UniversityRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RemoteInterface extends Remote {
    
    
    String addStudentToFile(Student student) throws RemoteException;
    String showStudentsFromFile() throws RemoteException;
    String saveStudentToDatabase(Student student) throws RemoteException;
    String readStudentsFromDatabase() throws RemoteException;
    String updateStudentInDatabase(int id, String newName, int newYear) throws RemoteException;
    String deleteStudentFromDatabase(int id) throws RemoteException;
    
    
    String addTeacherToFile(Teacher teacher) throws RemoteException;
    String showTeachersFromFile() throws RemoteException;
    String saveTeacherToDatabase(Teacher teacher) throws RemoteException;
    String readTeachersFromDatabase() throws RemoteException;
    String updateTeacherInDatabase(int id, String newName, int newExperience) throws RemoteException;
    String deleteTeacherFromDatabase(int id) throws RemoteException;
}
