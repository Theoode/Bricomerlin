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
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;



public class ServerImpl extends UnicastRemoteObject implements Services {
    private Map<String, Integer> stock;
    private double chiffreAffaire;

    // Constructeur
    protected ServerImpl() throws RemoteException {
        super();
        System.out.println("Constructeur ServerImpl lancé");
        stock = new HashMap<>();
        System.out.println("Constructeur ServerImpl terminé");
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

    @Override
    public boolean creerCommande(Map<String, Integer> articles) throws RemoteException {
        String insertCommandeSQL = "INSERT INTO commandes(total_prix, statut_paiement) VALUES ( ?, ?)";
        String insertCommandeProduitSQL = "INSERT INTO article_commande(idReference, id_commande, quantite) VALUES (?, ?, ?)";
        String updateStockSQL = "UPDATE article SET enStock = enStock - ? WHERE idReference = ?";
        String selectArticleSQL = "SELECT nom, prixUnitaire, enStock FROM article WHERE idReference = ?";

        try (Connection conn = DBManager.getConnection()) {
            conn.setAutoCommit(false);

            double totalPrix = 0.0;
            Map<String, Double> prixArticles = new HashMap<>();
            Map<String, String> nomsArticles = new HashMap<>();

            for (Map.Entry<String, Integer> entry : articles.entrySet()) {
                String ref = entry.getKey();
                int quantite = entry.getValue();

                try (PreparedStatement ps = conn.prepareStatement(selectArticleSQL)) {
                    ps.setString(1, ref);
                    ResultSet rs = ps.executeQuery();

                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }

                    String nom = rs.getString("nom");
                    double prix = rs.getDouble("prixUnitaire");
                    int enStock = rs.getInt("enStock");

                    if (enStock < quantite) {
                        conn.rollback();
                        return false;
                    }

                    nomsArticles.put(ref, nom);
                    prixArticles.put(ref, prix);
                    totalPrix += prix * quantite;
                }
            }

            int idCommande;
            try (PreparedStatement ps = conn.prepareStatement(insertCommandeSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setDouble(1, totalPrix);
                ps.setString(2, "En Attente");
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    idCommande = rs.getInt(1);
                } else {
                    conn.rollback();
                    return false;
                }
            }

            // Insertion des articles + mise à jour du stock
            for (Map.Entry<String, Integer> entry : articles.entrySet()) {
                String ref = entry.getKey();
                int quantite = entry.getValue();

                try (PreparedStatement ps = conn.prepareStatement(insertCommandeProduitSQL)) {
                    ps.setString(1, ref);
                    ps.setInt(2, idCommande);
                    ps.setInt(3, quantite);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(updateStockSQL)) {
                    ps.setInt(1, quantite);
                    ps.setString(2, ref);
                    ps.executeUpdate();
                }
            }

            conn.commit();

            // Génération du ticket de caisse
            try {
                File dossierFactures = new File("factures");
                if (!dossierFactures.exists()) dossierFactures.mkdirs();

                String fileName = "factures/ticket_" + idCommande + ".txt";
                try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                    writer.println("🧾 Ticket de caisse - BricoMerlin");
                    writer.println("Commande n° : " + idCommande);
                    writer.println("Date : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    writer.println();
                    writer.printf("%-15s %-25s %-10s %-15s %-10s%n", "Référence", "Nom", "Quantité", "Prix Unitaire", "Total");
                    writer.println("----------------------------------------------------------------------------------");

                    for (Map.Entry<String, Integer> entry : articles.entrySet()) {
                        String ref = entry.getKey();
                        int qte = entry.getValue();
                        double prix = prixArticles.get(ref);
                        String nomArticle = nomsArticles.get(ref);
                        double total = prix * qte;

                        writer.printf("%-15s %-25s %-10d %-15.2f %-10.2f%n", ref, nomArticle, qte, prix, total);
                    }

                    writer.println("----------------------------------------------------------------------------------");
                    writer.printf("Total à payer : %.2f €%n", totalPrix);
                }

                System.out.println("Ticket TXT généré dans factures/: ticket_" + idCommande + ".txt");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> getArticlesDisponibles() throws RemoteException {
        List<String> articlesDisponibles = new ArrayList<>();
        String query = "SELECT idReference, nom FROM article WHERE enStock > 0";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String reference = rs.getString("idReference");
                String nom = rs.getString("nom");
                articlesDisponibles.add(String.format("%s - %s", reference, nom));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Erreur lors de la récupération des articles disponibles.", e);
        }

        return articlesDisponibles;
    }

    @Override
    public String getStatutPaiement(int idCommande) throws RemoteException {
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT statut_paiement FROM commandes WHERE id_commande = ?")) {
            ps.setInt(1, idCommande);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("statut_paiement");
            }
            return "Inconnue";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur";
        }
    }

    @Override
    public boolean reglerCommande(int idCommande) throws RemoteException {
        try (Connection conn = DBManager.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT statut_paiement FROM commandes WHERE id_commande = ?");
            check.setInt(1, idCommande);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                String statut = rs.getString("statut_paiement");
                if ("Payée".equalsIgnoreCase(statut)) return false; // déjà payée
            } else {
                return false; // commande inconnue
            }

            PreparedStatement update = conn.prepareStatement("UPDATE commandes SET statut_paiement = 'Payée' WHERE id_commande = ?");
            update.setInt(1, idCommande);
            return update.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
