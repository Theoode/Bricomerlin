package server;
import rmi.Services;
import utils.DBManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ServerImpl extends UnicastRemoteObject implements Services {
    private Map<String, Integer> stock;
    private double chiffreAffaire;

    // Constructeur
    protected ServerImpl() throws RemoteException {
        super();
        stock = new HashMap<>();
    }

    @Override
    public String consulterStock(String reference) throws RemoteException {
        String query = """
        SELECT a.enStock, a.prixUnitaire, f.nomFamille,a.nom
        FROM Article a 
        JOIN Famille f ON a.idFamille = f.idFamille 
        WHERE a.idReference = ?
        """;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, reference);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int stock = rs.getInt("enStock");
                double prix = rs.getDouble("prixUnitaire");
                String nom= rs.getString("nom");
                String famille = rs.getString("nomFamille");

                return String.format("Article %s | Nom: %s | Famille: %s | Prix: %.2f€ | Stock: %d",
                        reference,nom, famille, prix, stock);
            } else {
                return "Article non trouvé";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur lors de la consultation du stock.";
        }
    }


    @Override
    public List<String> rechercherArticlesParFamille(String nomFamille) throws RemoteException {
        List<String> articles = new ArrayList<>();
        String query = "SELECT idReference,nom FROM Article A,Famille F WHERE A.idFamille = F.idFamille AND A.EnStock > 0 AND F.nomFamille = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, nomFamille);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                articles.add(rs.getString("idReference") + " - " + rs.getString("nom"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Erreur lors de la recherche des articles pour la famille: " + nomFamille, e);
        }

        return articles;
    }

    @Override
    public boolean ajouterStockProduit(String reference, int quantite) throws RemoteException {
        String queryCheck = "SELECT enStock FROM Article WHERE idReference = ?";
        String queryUpdate = "UPDATE Article SET enStock = enStock + ? WHERE idReference = ?";

        try (Connection conn = DBManager.getConnection()) {
            // Vérifier que l'article existe
            try (PreparedStatement psCheck = conn.prepareStatement(queryCheck)) {
                psCheck.setString(1, reference);
                ResultSet rs = psCheck.executeQuery();

                if (!rs.next()) {
                    return false; // Article non trouvé
                }
            }

            // Mise à jour du stock
            try (PreparedStatement psUpdate = conn.prepareStatement(queryUpdate)) {
                psUpdate.setInt(1, quantite);
                psUpdate.setString(2, reference);
                psUpdate.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Erreur lors de l'ajout de stock pour l'article: " + reference, e);
        }
    }


}
