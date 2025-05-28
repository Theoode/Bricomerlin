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
            JPanel articlePanel = new JPanel();
            articlePanel.setLayout(new BoxLayout(articlePanel, BoxLayout.Y_AXIS));

            JTextArea articleResultArea = new JTextArea(10, 50);
            articleResultArea.setEditable(false);

            // Section 1 : Rechercher un article par référence
            JPanel refPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField refField = new JTextField(15);
            JButton btnConsulter = new JButton("🔍 Consulter article");
            refPanel.setBorder(BorderFactory.createTitledBorder("Recherche par référence"));
            refPanel.add(new JLabel("Référence :"));
            refPanel.add(refField);
            refPanel.add(btnConsulter);

            // Section 2 : Rechercher par famille
            JPanel famillePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField familleField = new JTextField(15);
            JButton btnRechercherFamille = new JButton("🔍 Rechercher famille");
            famillePanel.setBorder(BorderFactory.createTitledBorder("Recherche par famille"));
            famillePanel.add(new JLabel("Famille :"));
            famillePanel.add(familleField);
            famillePanel.add(btnRechercherFamille);

            // Section 3 : Ajouter du stock
            JPanel ajoutStockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField refAjoutField = new JTextField(10);
            JTextField qteAjoutField = new JTextField(5);
            JButton btnAjouterStock = new JButton("➕ Ajouter au stock");
            ajoutStockPanel.setBorder(BorderFactory.createTitledBorder("Ajout de stock"));
            ajoutStockPanel.add(new JLabel("Référence :"));
            ajoutStockPanel.add(refAjoutField);
            ajoutStockPanel.add(new JLabel("Quantité :"));
            ajoutStockPanel.add(qteAjoutField);
            ajoutStockPanel.add(btnAjouterStock);

            // Ajout des sections à l'onglet
            articlePanel.add(refPanel);
            articlePanel.add(famillePanel);
            articlePanel.add(ajoutStockPanel);
            articlePanel.add(new JScrollPane(articleResultArea));

            // Événements
            btnConsulter.addActionListener(e -> {
                try {
                    String ref = refField.getText().trim();
                    if (ref.isEmpty()) {
                        articleResultArea.append("Référence vide\n");
                        return;
                    }
                    String res = service.consulterStock(ref);
                    articleResultArea.append("Infos : " + res + "\n");
                } catch (Exception ex) {
                    articleResultArea.append("Erreur : " + ex.getMessage() + "\n");
                }
            });

            btnRechercherFamille.addActionListener(e -> {
                try {
                    String nomFamille = familleField.getText().trim();
                    if (nomFamille.isEmpty()) {
                        articleResultArea.append("⚠️ Nom de famille vide\n");
                        return;
                    }
                    List<String> articles = service.rechercherArticlesParFamille(nomFamille);
                    articleResultArea.append("Articles de la famille \"" + nomFamille + "\" :\n");
                    for (String a : articles) articleResultArea.append("- " + a + "\n");
                } catch (Exception ex) {
                    articleResultArea.append(" Erreur famille : " + ex.getMessage() + "\n");
                }
            });

            btnAjouterStock.addActionListener(e -> {
                try {
                    String ref = refAjoutField.getText().trim();
                    int qte = Integer.parseInt(qteAjoutField.getText().trim());
                    if (ref.isEmpty() || qte <= 0) {
                        articleResultArea.append(" Référence ou quantité invalide\n");
                        return;
                    }
                    boolean success = service.ajouterStockProduit(ref, qte);
                    articleResultArea.append(success
                            ? "Stock mis à jour pour " + ref + " (+ " + qte + ")\n"
                            : "Échec de la mise à jour du stock\n");
                } catch (NumberFormatException nfe) {
                    articleResultArea.append("Quantité invalide\n");
                } catch (Exception ex) {
                    articleResultArea.append("Erreur ajout stock : " + ex.getMessage() + "\n");
                }
            });


            // Onglet 2 : Créer une commande
            JPanel commandePanel = new JPanel(new BorderLayout());
            JTextField nomAcheteurField = new JTextField(20);
            JTextField qteCommandeField = new JTextField(5);
            JComboBox<String> articleComboBox = new JComboBox<>();
            JComboBox<String> panierComboBox = new JComboBox<>();
            JLabel prixTotalLabel = new JLabel("💰 Total : 0.00 €");
            JTextArea commandeLog = new JTextArea(10, 50);
            commandeLog.setEditable(false);

            Map<String, Integer> articlesCommande = new HashMap<>();
            final double[] totalCommande = {0.0};

            List<String> articlesDispo = service.getArticlesDisponibles();
            for (String article : articlesDispo) {
                articleComboBox.addItem(article); // Format : "REF123 - Marteau"
            }

            JButton btnAjouterArticle = new JButton("Ajouter article");
            JButton btnEnvoyerCommande = new JButton("Envoyer commande");
            JButton btnSupprimerArticle = new JButton("Supprimer article");

            JPanel formCommande = new JPanel(new GridLayout(6, 2, 10, 10));
            formCommande.add(new JLabel("Articles en stock :")); formCommande.add(articleComboBox);
            formCommande.add(new JLabel("Quantité :")); formCommande.add(qteCommandeField);
            formCommande.add(btnAjouterArticle); formCommande.add(btnEnvoyerCommande);
            formCommande.add(new JLabel("Supprimer du panier :")); formCommande.add(panierComboBox);
            formCommande.add(btnSupprimerArticle);

            commandePanel.add(formCommande, BorderLayout.NORTH);
            commandePanel.add(new JScrollPane(commandeLog), BorderLayout.CENTER);
            commandePanel.add(prixTotalLabel, BorderLayout.SOUTH);

            // Ajouter un article au panier
            btnAjouterArticle.addActionListener(e -> {
                try {
                    String selection = (String) articleComboBox.getSelectedItem();
                    if (selection == null || selection.isEmpty()) return;

                    String ref = selection.split(" - ")[0];
                    int qte = Integer.parseInt(qteCommandeField.getText());

                    String res = service.consulterStock(ref);
                    String prixStr = res.split("Prix: ")[1].split("€")[0].trim();
                    double prix = Double.parseDouble(prixStr.replace(",", "."));

                    int qteTotale = articlesCommande.getOrDefault(ref, 0) + qte;
                    articlesCommande.put(ref, qteTotale);
                    totalCommande[0] += prix * qte;

                    // Mise à jour ou ajout dans la ComboBox panier
                    boolean existe = false;
                    for (int i = 0; i < panierComboBox.getItemCount(); i++) {
                        String item = panierComboBox.getItemAt(i);
                        if (item.startsWith(ref + " ")) {
                            panierComboBox.removeItemAt(i);
                            existe = true;
                            break;
                        }
                    }
                    panierComboBox.addItem(ref + " x" + qteTotale);

                    commandeLog.append(ref + " x" + qte + " → " + String.format("%.2f", prix * qte) + " €\n");
                    prixTotalLabel.setText("Total : " + String.format("%.2f", totalCommande[0]) + " €");
                    qteCommandeField.setText("");
                } catch (Exception ex) {
                    commandeLog.append("Erreur : " + ex.getMessage() + "\n");
                }
            });

            // Supprimer un article du panier
            btnSupprimerArticle.addActionListener(e -> {
                String selection = (String) panierComboBox.getSelectedItem();
                if (selection == null || selection.isEmpty()) return;

                String ref = selection.split(" x")[0];

                Integer qte = articlesCommande.get(ref);
                if (qte == null) return;

                try {
                    String res = service.consulterStock(ref);
                    String prixStr = res.split("Prix: ")[1].split("€")[0].trim();
                    double prix = Double.parseDouble(prixStr.replace(",", "."));

                    totalCommande[0] -= prix * qte;
                    prixTotalLabel.setText("Total : " + String.format("%.2f", totalCommande[0]) + " €");

                    articlesCommande.remove(ref);
                    panierComboBox.removeItem(selection);

                    commandeLog.append("❌ Supprimé : " + ref + " x" + qte + "\n");
                } catch (Exception ex) {
                    commandeLog.append("Erreur lors de la suppression : " + ex.getMessage() + "\n");
                }
            });

            // Envoyer la commande
            btnEnvoyerCommande.addActionListener(e -> {
                if (articlesCommande.isEmpty()) {
                    commandeLog.append("Ajouter au moins un article.\n");
                    return;
                }

                try {
                    boolean success = service.creerCommande(articlesCommande);
                    if (success) {
                        commandeLog.append("✅ Commande créée avec succès !\n");
                        articlesCommande.clear();
                        panierComboBox.removeAllItems();
                        prixTotalLabel.setText("Total : 0.00 €");
                        totalCommande[0] = 0.0;
                    } else {
                        commandeLog.append("❌ Échec de la création de la commande.\n");
                    }
                } catch (Exception ex) {
                    commandeLog.append("Erreur : " + ex.getMessage() + "\n");
                }
            });



            // Onglets
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Articles", articlePanel);
            tabbedPane.addTab("Créer commande", commandePanel);

            frame.add(tabbedPane);
            frame.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Connexion au serveur RMI échouée : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
