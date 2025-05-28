package serveur_siege;

import rmi.ServiceSiege;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class SiegeRMI {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            ServiceSiege siegeService = new SiegeImpl();
            Naming.rebind("rmi://localhost/ServiceSiege", siegeService);
            System.out.println("Service publié sur rmi://localhost/ServiceSiege");
            String resultat = siegeService.synchroniserTousLesPrix();
            System.out.println("🔄 Synchronisation des prix terminée :\n" + resultat);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du lancement du serveur RMI :");
            e.printStackTrace();
        }
    }
}
