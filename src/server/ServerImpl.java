package server;

import rmi.Services;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerImpl extends UnicastRemoteObject implements Services {
    private Map<String, Integer> stock;
    private Map<Integer, String> factures;
    private double chiffreAffaire;

    // Constructeur
    protected ServerImpl() throws RemoteException {
        super();
        stock = new HashMap<>();
        factures = new HashMap<>();
        chiffreAffaire = 0.0;


        stock.put("A123", 10);
        stock.put("B456", 5);
    }

    @Override
    public String consulterStock(String reference) throws RemoteException {
        return stock.containsKey(reference) ? "Stock disponible: " + stock.get(reference) : "Article non trouvé";
    }

    @Override
    public List<String> rechercherArticlesParFamille(String famille) throws RemoteException {
        // Juste un exemple, normalement, les articles sont classés par famille
        return Arrays.asList("Article1-" + famille, "Article2-" + famille);
    }

    @Override
    public boolean acheterArticle(String reference, int quantite) throws RemoteException {
        if (stock.containsKey(reference) && stock.get(reference) >= quantite) {
            stock.put(reference, stock.get(reference) - quantite);
            chiffreAffaire += quantite * 100;  // Supposons que chaque article coûte 100€
            return true;
        }
        return false;
    }

    @Override
    public String consulterFacture(int idFacture) throws RemoteException {
        return factures.getOrDefault(idFacture, "Facture introuvable");
    }

    @Override
    public double calculerChiffreAffaire(String date) throws RemoteException {
        return chiffreAffaire;
    }

    @Override
    public boolean ajouterStockProduit(String reference, int quantite) throws RemoteException {
        stock.put(reference, stock.getOrDefault(reference, 0) + quantite);
        return true;
    }
}
