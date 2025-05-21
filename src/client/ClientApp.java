package client;

import rmi.Services;

import javax.swing.*;
import java.awt.*;
import java.rmi.Naming;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientApp::createUI);
    }

    private static void createUI() {
        JFrame frame = new JFrame("BricoMerlin - Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 550);

        try {
            Services service = (Services) Naming.lookup("rmi://localhost/ServiceStock");

            // Onglet 1 : Gestion des articles
            JPanel articlePanel = new JPanel(new BorderLayout());
            JTextArea articleResultArea = new JTextArea(10, 50);
            articleResultArea.setEditable(false);

            JTextField refField = new JTextField();
            JTextField qteField = new JTextField();
            JTextField familleField = new JTextField();
            JButton btnConsulter = new JButton("Consulter l'article");
            JButton btnAcheter = new JButton("Acheter l'article");
            JButton btnRechercherFamille = new JButton("Rechercher par famille");

            JPanel topPanel = new JPanel(new GridLayout(4, 2, 10, 10));
            topPanel.add(new JLabel("Référence article :")); topPanel.add(refField);
            topPanel.add(new JLabel("Quantité :")); topPanel.add(qteField);
            topPanel.add(new JLabel("Nom de la famille :")); topPanel.add(familleField);
            topPanel.add(btnConsulter); topPanel.add(btnAcheter);

            JPanel famillePanel = new JPanel();
            famillePanel.add(btnRechercherFamille);

            articlePanel.add(topPanel, BorderLayout.NORTH);
            articlePanel.add(famillePanel, BorderLayout.CENTER);
            articlePanel.add(new JScrollPane(articleResultArea), BorderLayout.SOUTH);

            btnConsulter.addActionListener(e -> {
                try {
                    String ref = refField.getText();
                    String res = service.consulterStock(ref);
                    articleResultArea.append("🟡 Infos : " + res + "\n");
                } catch (Exception ex) {
                    articleResultArea.append("❌ Erreur : " + ex.getMessage() + "\n");
                }
            });

            /*btnAcheter.addActionListener(e -> {
                try {
                    String ref = refField.getText();
                    int qte = Integer.parseInt(qteField.getText());
                    boolean success = service.acheterArticle(ref, qte);
                    articleResultArea.append(success ? "✅ Achat effectué\n" : "❌ Achat échoué\n");
                } catch (Exception ex) {
                    articleResultArea.append("❌ Erreur : " + ex.getMessage() + "\n");
                }
            });*/

            btnRechercherFamille.addActionListener(e -> {
                try {
                    String nomFamille = familleField.getText();
                    List<String> articles = service.rechercherArticlesParFamille(nomFamille);
                    articleResultArea.append("🔍 Articles de la famille " + nomFamille + " :\n");
                    for (String a : articles) articleResultArea.append("- " + a + "\n");
                } catch (Exception ex) {
                    articleResultArea.append("❌ Erreur famille : " + ex.getMessage() + "\n");
                }
            });

            // Onglet 2 : Créer une commande
            JPanel commandePanel = new JPanel(new BorderLayout());
            JTextField nomAcheteurField = new JTextField(20);
            JTextField qteCommandeField = new JTextField(5);
            JComboBox<String> articleComboBox = new JComboBox<>();
            JLabel prixTotalLabel = new JLabel("💰 Total : 0.00 €");
            JTextArea commandeLog = new JTextArea(10, 50);
            commandeLog.setEditable(false);

            Map<String, Integer> articlesCommande = new HashMap<>();
            final double[] totalCommande = {0.0};

            // Charger la ComboBox avec les articles disponibles
            List<String> articlesDispo = service.getArticlesDisponibles();
            for (String article : articlesDispo) {
                articleComboBox.addItem(article); // "REF123 - Marteau"
            }

            JButton btnAjouterArticle = new JButton("Ajouter article");
            JButton btnEnvoyerCommande = new JButton("Envoyer commande");

            JPanel formCommande = new JPanel(new GridLayout(5, 2, 10, 10));
            formCommande.add(new JLabel("Nom de l'acheteur :")); formCommande.add(nomAcheteurField);
            formCommande.add(new JLabel("Article disponible :")); formCommande.add(articleComboBox);
            formCommande.add(new JLabel("Quantité :")); formCommande.add(qteCommandeField);
            formCommande.add(btnAjouterArticle); formCommande.add(btnEnvoyerCommande);

            commandePanel.add(formCommande, BorderLayout.NORTH);
            commandePanel.add(new JScrollPane(commandeLog), BorderLayout.CENTER);
            commandePanel.add(prixTotalLabel, BorderLayout.SOUTH);

            btnAjouterArticle.addActionListener(e -> {
                try {
                    String selection = (String) articleComboBox.getSelectedItem();
                    if (selection == null || selection.isEmpty()) return;

                    String ref = selection.split(" - ")[0]; // extrait "REF123"
                    int qte = Integer.parseInt(qteCommandeField.getText());

                    String res = service.consulterStock(ref);
                    String prixStr = res.split("Prix: ")[1].split("€")[0].trim();
                    double prix = Double.parseDouble(prixStr.replace(",", "."));

                    articlesCommande.put(ref, articlesCommande.getOrDefault(ref, 0) + qte);
                    totalCommande[0] += prix * qte;

                    commandeLog.append("🟡 " + ref + " x" + qte + " → " + String.format("%.2f", prix * qte) + " €\n");
                    prixTotalLabel.setText("💰 Total : " + String.format("%.2f", totalCommande[0]) + " €");

                    qteCommandeField.setText("");
                } catch (Exception ex) {
                    commandeLog.append("❌ Erreur : " + ex.getMessage() + "\n");
                }
            });

            btnEnvoyerCommande.addActionListener(e -> {
                String nomAcheteur = nomAcheteurField.getText();
                if (nomAcheteur.isEmpty() || articlesCommande.isEmpty()) {
                    commandeLog.append("❌ Remplir tous les champs et ajouter au moins un article.\n");
                    return;
                }
                try {
                    boolean success = service.creerCommande(nomAcheteur, articlesCommande);
                    if (success) {
                        commandeLog.append("✅ Commande créée avec succès !\n");
                        articlesCommande.clear();
                        nomAcheteurField.setText("");
                        prixTotalLabel.setText("💰 Total : 0.00 €");
                        totalCommande[0] = 0.0;
                    } else {
                        commandeLog.append("❌ Échec de la création de la commande.\n");
                    }
                } catch (Exception ex) {
                    commandeLog.append("❌ Erreur : " + ex.getMessage() + "\n");
                }
            });

            // Onglets
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Articles", articlePanel);
            tabbedPane.addTab("Créer commande", commandePanel);

            frame.add(tabbedPane);
            frame.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "❌ Connexion au serveur RMI échouée : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
