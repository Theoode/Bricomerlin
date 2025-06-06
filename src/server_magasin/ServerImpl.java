package server_magasin;


import rmi.ServicesServeur;
import utils.DBManagerMagasin;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



public class ServerImpl extends UnicastRemoteObject implements ServicesServeur {
    private Map<String, Integer> stock;

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

        try (Connection conn = DBManagerMagasin.getConnection();
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

        try (Connection conn = DBManagerMagasin.getConnection();
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
    public List<String> getFamillesDisponibles() throws RemoteException {
        List<String> familles = new ArrayList<>();
        String query = "SELECT nomFamille FROM Famille ORDER BY nomFamille";

        try (Connection conn = DBManagerMagasin.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                familles.add(rs.getString("nomFamille"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Erreur lors de la récupération des familles", e);
        }

        return familles;
    }

    @Override
    public boolean ajouterStockProduit(String reference, int quantite) throws RemoteException {
        String queryCheck = "SELECT enStock FROM Article WHERE idReference = ?";
        String queryUpdate = "UPDATE Article SET enStock = enStock + ? WHERE idReference = ?";

        try (Connection conn = DBManagerMagasin.getConnection()) {
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
        String insertCommandeSQL = "INSERT INTO commandes(total_prix, statut_paiement,date_commande) VALUES ( ?, ?,CURRENT_TIMESTAMP)";
        String insertCommandeProduitSQL = "INSERT INTO article_commande(idReference, id_commande, quantite) VALUES (?, ?, ?)";
        String updateStockSQL = "UPDATE article SET enStock = enStock - ? WHERE idReference = ?";
        String selectArticleSQL = "SELECT nom, prixUnitaire, enStock FROM article WHERE idReference = ?";

        try (Connection conn = DBManagerMagasin.getConnection()) {
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
            try {
                File dossierFactures = new File("factures");
                if (!dossierFactures.exists()) dossierFactures.mkdirs();

                String fileName = "factures/ticket_" + idCommande + ".json";

                // Construire la structure de données à sérialiser en JSON
                Map<String, Object> ticket = new LinkedHashMap<>();
                ticket.put("titre", "🧾 Ticket de caisse - BricoMerlin");
                ticket.put("commande_id", idCommande);
                ticket.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                List<Map<String, Object>> detailsArticles = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : articles.entrySet()) {
                    String ref = entry.getKey();
                    int qte = entry.getValue();
                    double prix = prixArticles.get(ref);
                    String nomArticle = nomsArticles.get(ref);
                    double total = prix * qte;

                    Map<String, Object> articleJson = new LinkedHashMap<>();
                    articleJson.put("reference", ref);
                    articleJson.put("nom", nomArticle);
                    articleJson.put("quantite", qte);
                    articleJson.put("prix_unitaire", prix);
                    articleJson.put("total", total);

                    detailsArticles.add(articleJson);
                }
                ticket.put("articles", detailsArticles);
                ticket.put("total_a_payer", totalPrix);

                // Sérialisation JSON avec Gson (bibliothèque Google)
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                    writer.println(gson.toJson(ticket));
                }

                System.out.println("Ticket JSON généré dans factures/: ticket_" + idCommande + ".json");
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

        try (Connection conn = DBManagerMagasin.getConnection();
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
        try (Connection conn = DBManagerMagasin.getConnection();
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
        try (Connection conn = DBManagerMagasin.getConnection()) {
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

    @Override
    public double calculerChiffreAffaires(String date) throws RemoteException {
        String sql = "SELECT SUM(total_prix) FROM commandes WHERE DATE(date_commande) = ?";
        try (Connection conn = DBManagerMagasin.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date); // format "2024-05-28"
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }


    @Override
    public void exporterFactures() {
        File sourceDir = new File("factures");
        File destinationDir = new File("factures_siege");

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            System.out.println("Le dossier 'factures' n'existe pas.");
            return;
        }

        if (!destinationDir.exists()) {
            if (destinationDir.mkdirs()) {
                System.out.println("Le dossier 'factures_siege' a été créé.");
            } else {
                System.out.println("Échec de la création du dossier 'factures_siege'.");
                return;
            }
        }

        File[] fichiers = sourceDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (fichiers == null || fichiers.length == 0) {
            System.out.println("Aucune facture à exporter.");
            return;
        }

        for (File fichier : fichiers) {
            File destFile = new File(destinationDir, fichier.getName());
            try (Scanner scanner = new Scanner(fichier);
                 PrintWriter writer = new PrintWriter(new FileWriter(destFile))) {
                while (scanner.hasNextLine()) {
                    writer.println(scanner.nextLine());
                }
                System.out.println("Facture exportée : " + fichier.getName());
            } catch (Exception e) {
                System.err.println("Erreur lors de l'export de la facture : " + fichier.getName());
                e.printStackTrace();
            }
        }
    }







}
