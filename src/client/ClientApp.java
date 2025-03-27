package client;

import rmi.Services;

import java.rmi.Naming;
import java.util.List;

public class ClientApp {
    public static void main(String[] args) {
        try {

            Services service = (Services) Naming.lookup("rmi://localhost/ServiceStock");
            System.out.println(" Connexion au serveur RMI réussie !");

        } catch (Exception e) {
            System.err.println("Erreur dans le client : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
