package server;

import rmi.Services;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ServeurRMI {
    public static void main(String[] args) {
        try {
            // Lancer le registre RMI sur le port 1099
            LocateRegistry.createRegistry(1099);
            System.out.println("Registre RMI démarré sur le port 1099...");

            // Créer une instance du service
            Services service = new ServerImpl();

            // Enregistrer l'objet distant dans le registre
            Naming.rebind("rmi://localhost/ServiceStock", service);
            System.out.println("Serveur prêt et service 'ServiceStock' enregistré...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
