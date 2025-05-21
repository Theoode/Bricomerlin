package server;
import rmi.Services;
import utils.DBManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        try (Connection conn = DBManager.getConnection()) {
            String query = "SELECT enStock FROM article WHERE idReference = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, reference);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return "Stock disponible: " + rs.getInt("enStock");
                } else {
                    return "Article non trouvé";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur lors de la consultation du stock.";
        }
    }

    @Override
    public List<String> rechercherArticlesParFamille(String famille) throws RemoteException {
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
