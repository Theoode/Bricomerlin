package server_magasin;

import rmi.ServicesServeur;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ServeurRMI {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            ServicesServeur service = new ServerImpl();
            Naming.rebind("rmi://localhost/ServiceStock", service);
            System.out.println("Serveur Magasin prêt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
