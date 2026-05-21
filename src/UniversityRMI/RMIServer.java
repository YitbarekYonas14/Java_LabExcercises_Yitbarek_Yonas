package UniversityRMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    public static void main(String[] args) {
        try {
            
            ServerImpl server = new ServerImpl();
            
           
            Registry registry = LocateRegistry.createRegistry(1099);
            
            
            registry.rebind("UniversityManagementSystem", server);
            
            System.out.println("==========================================");
            System.out.println("✓ RMI Server is running...");
            System.out.println("✓ Registry bound to port 1099");
            System.out.println("✓ Remote object registered as: UniversityManagementSystem");
            System.out.println("✓ Waiting for client connections...");
            System.out.println("==========================================");
            
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
