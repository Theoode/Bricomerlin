package client;

import rmi.Services;

import java.rmi.Naming;
import java.util.List;

public class ClientApp {
    public static void main(String[] args) {
        try {
            // Recherche du service sur le registre RMI
            Services service = (Services) Naming.lookup("rmi://localhost/ServiceStock");

            System.out.println(" Connexion au serveur RMI réussie !");

            // Appel des méthodes distantes
            System.out.println("🔹 Consultation du stock (A123) : " + service.consulterStock("A123"));

            List<String> articles = service.rechercherArticlesParFamille("Electro");
            System.out.println("🔹 Articles de la famille 'Electro' : " + articles);

            // Acheter un article
            boolean achat = service.acheterArticle("A123", 2);
            System.out.println("🔹 Achat réussi ? " + achat);

            // Vérification du stock après achat
            System.out.println("🔹 Stock après achat : " + service.consulterStock("A123"));

            // Ajout de stock
            service.ajouterStockProduit("A123", 5);
            System.out.println("🔹 Stock après ajout : " + service.consulterStock("A123"));

            // Consultation du chiffre d'affaires
            double chiffreAffaire = service.calculerChiffreAffaire("2025-03-27");
            System.out.println("🔹 Chiffre d'affaires du jour : " + chiffreAffaire + "€");

        } catch (Exception e) {
            System.err.println("Erreur dans le client : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
