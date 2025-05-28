package siege;

import rmi.ServiceSiege;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class SiegeRMI {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            System.out.println("Registre RMI démarré sur le port 1099...");

            System.out.println("Avant instanciation du service...");
            ServiceSiege service = new SiegeImpl();
            System.out.println("Service instancié");

            Naming.rebind("rmi://localhost/ServiceSiege", service);
            System.out.println("Serveur du siège prêt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
