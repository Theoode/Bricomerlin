package server;

import rmi.Services;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ServeurRMI {
    public static void main(String[] args) {
        try {

            LocateRegistry.createRegistry(1099);
            System.out.println("Registre RMI démarré sur le port 1099...");

            Services service = new ServerImpl();
            Naming.rebind("rmi://localhost/ServiceStock", service);
            System.out.println("Serveur prêt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
