package server_magasin;

import rmi.ServiceMagasin;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MagasinRMI {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            ServiceMagasin service = new MagasinImpl();
            Naming.rebind("rmi://localhost/ServiceStock", service);
            System.out.println("Serveur Magasin prêt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
