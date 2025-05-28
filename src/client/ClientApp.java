package client;

import rmi.ServicesServeur;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
            ServicesServeur service = (ServicesServeur) Naming.lookup("rmi://localhost/ServiceStock");

            JPanel articlePanel = new JPanel(new BorderLayout());


            JPanel recherchePanel = new JPanel();
            recherchePanel.setLayout(new BoxLayout(recherchePanel, BoxLayout.Y_AXIS));
            recherchePanel.setBorder(BorderFactory.createTitledBorder("Recherche d'articles"));
            JPanel refPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField refField = new JTextField(15);
            JButton btnConsulter = new JButton("🔍 Rechercher par référence");
            refPanel.add(new JLabel("Référence :"));
            refPanel.add(refField);
            refPanel.add(btnConsulter);
            recherchePanel.add(refPanel);
            JPanel famillePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            List<String> familles = service.getFamillesDisponibles();
            JComboBox<String> familleComboBox = new JComboBox<>();
            familleComboBox.addItem(""); // option vide par défaut
            for (String f : familles) {
                familleComboBox.addItem(f);
            }
            JButton btnRechercherFamille = new JButton("🔍 Rechercher par famille");


            famillePanel.add(new JLabel("Famille :"));
            famillePanel.add(familleComboBox);
            famillePanel.add(btnRechercherFamille);
            recherchePanel.add(famillePanel);
            JTextArea rechercheResultArea = new JTextArea(8, 50);
            rechercheResultArea.setEditable(false);
            recherchePanel.add(new JScrollPane(rechercheResultArea));
            JPanel panierPanel = new JPanel(new BorderLayout());
            panierPanel.setBorder(BorderFactory.createTitledBorder("Panier / Commande"));
            DefaultListModel<String> panierModel = new DefaultListModel<>();
            JList<String> panierList = new JList<>(panierModel);
            panierList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JLabel totalLabel = new JLabel("Total : 0.00 €");
            JPanel ajoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField refAjoutField = new JTextField(10);
            JTextField qteAjoutField = new JTextField(5);

            JButton btnAjouterPanier = new JButton("Ajouter au panier");
            JButton btnSupprimerPanier = new JButton("Supprimer du panier");
            JButton btnValiderCommande = new JButton("Valider la commande");

            ajoutPanel.add(new JLabel("Réf :"));
            ajoutPanel.add(refAjoutField);
            ajoutPanel.add(new JLabel("Qté :"));
            ajoutPanel.add(qteAjoutField);
            ajoutPanel.add(btnAjouterPanier);
            ajoutPanel.add(btnSupprimerPanier);
            ajoutPanel.add(btnValiderCommande);
            panierPanel.add(new JScrollPane(panierList), BorderLayout.CENTER);
            panierPanel.add(totalLabel, BorderLayout.SOUTH);
            panierPanel.add(ajoutPanel, BorderLayout.NORTH);
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, recherchePanel, panierPanel);
            splitPane.setResizeWeight(0.5);
            articlePanel.add(splitPane, BorderLayout.CENTER);


            Map<String, Integer> panier = new HashMap<>();
            Map<String, String> refVersNom = new HashMap<>();

            final double[] totalPanier = {0.0};


            btnConsulter.addActionListener(e -> {
                try {
                    String ref = refField.getText().trim();
                    if (ref.isEmpty()) {
                        rechercheResultArea.append("Référence vide\n");
                        return;
                    }
                    String res = service.consulterStock(ref);
                    rechercheResultArea.append("Infos : " + res + "\n");
                } catch (Exception ex) {
                    rechercheResultArea.append("Erreur : " + ex.getMessage() + "\n");
                }
            });
            btnRechercherFamille.addActionListener(e -> {
                try {
                    String fam = (String) familleComboBox.getSelectedItem();
                    if (fam == null || fam.isEmpty()) {
                        rechercheResultArea.append("Veuillez sélectionner une famille.\n");
                        return;
                    }
                    List<String> articles = service.rechercherArticlesParFamille(fam);
                    rechercheResultArea.append("Articles famille " + fam + " :\n");
                    for (String a : articles) rechercheResultArea.append("- " + a + "\n");
                } catch (Exception ex) {
                    rechercheResultArea.append("Erreur famille : " + ex.getMessage() + "\n");
                }
            });
            btnAjouterPanier.addActionListener(e -> {
                try {
                    String ref = refAjoutField.getText().trim();
                    int qte = Integer.parseInt(qteAjoutField.getText().trim());
                    if (ref.isEmpty() || qte <= 0) {
                        rechercheResultArea.append("Réf ou quantité invalide\n");
                        return;
                    }

                    // Récupérer les infos depuis consulterStock
                    String res = service.consulterStock(ref); // attention : on suppose ici que nom est inclus dans res

                    // Extraction du nom et du prix depuis res
                    String nom = res.split("Nom: ")[1].split("Prix:")[0].trim();
                    String prixStr = res.split("Prix: ")[1].split("€")[0].trim();
                    double prix = Double.parseDouble(prixStr.replace(",", "."));

                    int qteActuelle = panier.getOrDefault(ref, 0);
                    panier.put(ref, qteActuelle + qte);
                    refVersNom.put(ref, nom);  // stocker le nom associé à la référence

                    totalPanier[0] += prix * qte;

                    // Mise à jour de la liste du panier
                    panierModel.clear();
                    for (Map.Entry<String, Integer> entry : panier.entrySet()) {
                        String r = entry.getKey();
                        int quantite = entry.getValue();
                        String n = refVersNom.get(r);
                        panierModel.addElement(r + " - " + n + " x" + quantite);
                    }

                    totalLabel.setText("Total : " + String.format("%.2f", totalPanier[0]) + " €");

                    refAjoutField.setText("");
                    qteAjoutField.setText("");
                } catch (Exception ex) {
                    rechercheResultArea.append("Erreur ajout panier : " + ex.getMessage() + "\n");
                }
            });
            btnSupprimerPanier.addActionListener(e -> {
                String selection = panierList.getSelectedValue();
                if (selection == null) return;

                String ref = selection.split(" x")[0];
                Integer qte = panier.get(ref);
                if (qte == null) return;

                try {
                    String res = service.consulterStock(ref);
                    String prixStr = res.split("Prix: ")[1].split("€")[0].trim();
                    double prix = Double.parseDouble(prixStr.replace(",", "."));

                    totalPanier[0] -= prix * qte;
                    panier.remove(ref);

                    panierModel.clear();
                    for (Map.Entry<String, Integer> entry : panier.entrySet()) {
                        panierModel.addElement(entry.getKey() + " x" + entry.getValue());
                    }

                    totalLabel.setText("Total : " + String.format("%.2f", totalPanier[0]) + " €");
                } catch (Exception ex) {
                    rechercheResultArea.append("Erreur suppression panier : " + ex.getMessage() + "\n");
                }
            });
            btnValiderCommande.addActionListener(e -> {
                if (panier.isEmpty()) {
                    rechercheResultArea.append("Le panier est vide !\n");
                    return;
                }
                try {
                    boolean success = service.creerCommande(panier);
                    if (success) {
                        panier.clear();
                        panierModel.clear();
                        rechercheResultArea.append("Commande validée avec succès !\n");
                        totalPanier[0] = 0.0;
                        totalLabel.setText("Total : 0.00 €");
                    } else {
                        rechercheResultArea.append("Échec de la validation commande\n");
                    }
                } catch (Exception ex) {
                    rechercheResultArea.append("Erreur validation commande : " + ex.getMessage() + "\n");
                }
            });


            // Onglet 2 : Ajout de stock
            JPanel stockPanel = new JPanel();
            stockPanel.setLayout(new BoxLayout(stockPanel, BoxLayout.Y_AXIS));
            stockPanel.setBorder(BorderFactory.createTitledBorder("Ajouter du stock"));

            JPanel refStockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField refStockField = new JTextField(15);
            refStockPanel.add(new JLabel("Référence :"));
            refStockPanel.add(refStockField);

            JPanel qteStockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField qteStockField = new JTextField(5);
            qteStockPanel.add(new JLabel("Quantité à ajouter :"));
            qteStockPanel.add(qteStockField);

            JButton btnAjouterStock = new JButton("✅ Ajouter au stock");
            JTextArea stockResultArea = new JTextArea(5, 50);
            stockResultArea.setEditable(false);

            stockPanel.add(refStockPanel);
            stockPanel.add(qteStockPanel);
            stockPanel.add(btnAjouterStock);
            stockPanel.add(new JScrollPane(stockResultArea));


            btnAjouterStock.addActionListener(e -> {
                try {
                    String ref = refStockField.getText().trim();
                    int qte = Integer.parseInt(qteStockField.getText().trim());

                    if (ref.isEmpty() || qte <= 0) {
                        stockResultArea.append("Référence vide ou quantité invalide.\n");
                        return;
                    }

                    boolean success = service.ajouterStockProduit(ref, qte); // ← Appel à ta méthode

                    if (success) {
                        stockResultArea.append("✅ Stock ajouté pour " + ref + " : +" + qte + "\n");
                        refStockField.setText("");
                        qteStockField.setText("");
                    } else {
                        stockResultArea.append("❌ Référence non trouvée : " + ref + "\n");
                    }

                } catch (NumberFormatException nfe) {
                    stockResultArea.append("⚠️ Quantité invalide (doit être un nombre entier).\n");
                } catch (Exception ex) {
                    stockResultArea.append("Erreur : " + ex.getMessage() + "\n");
                }
            });





            // Onglet 3 : Facturation

            JPanel facturationPanel = new JPanel(new BorderLayout());
            JTextField idCommandeField = new JTextField(10);
            JTextArea ticketArea = new JTextArea(20, 50);
            ticketArea.setEditable(false);
            JButton btnAfficherTicket = new JButton("Afficher Ticket");
            JButton btnReglerCommande = new JButton("Régler la commande");
            JLabel statutPaiementLabel = new JLabel("Statut : inconnu");

            // Zone d'entrée pour l’ID de commande
            JPanel topPanel = new JPanel();
            topPanel.add(new JLabel("ID Commande :"));
            topPanel.add(idCommandeField);
            topPanel.add(btnAfficherTicket);
            topPanel.add(btnReglerCommande);
            topPanel.add(statutPaiementLabel);

            facturationPanel.add(topPanel, BorderLayout.NORTH);
            facturationPanel.add(new JScrollPane(ticketArea), BorderLayout.CENTER);

            btnAfficherTicket.addActionListener(e -> {
                try {
                    int idCommande = Integer.parseInt(idCommandeField.getText().trim());
                    File ticket = new File("factures/ticket_" + idCommande + ".txt");
                    if (!ticket.exists()) {
                        ticketArea.setText("❌ Ticket non trouvé pour la commande #" + idCommande);
                        return;
                    }

                    // Lire le contenu du fichier
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new FileReader(ticket))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                    }

                    ticketArea.setText(sb.toString());

                    // Afficher statut paiement
                    String statut = service.getStatutPaiement(idCommande); // Méthode à implémenter côté serveur
                    statutPaiementLabel.setText("Statut : " + statut);

                } catch (Exception ex) {
                    ticketArea.setText("Erreur : " + ex.getMessage());
                }
            });
            btnReglerCommande.addActionListener(e -> {
                try {
                    int idCommande = Integer.parseInt(idCommandeField.getText().trim());
                    boolean success = service.reglerCommande(idCommande); // Méthode à créer côté serveur

                    if (success) {
                        ticketArea.append("\n✅ Commande réglée !");
                        statutPaiementLabel.setText("Statut : Payée");
                    } else {
                        ticketArea.append("\n❌ Échec du paiement (commande déjà payée ?)");
                    }
                } catch (Exception ex) {
                    ticketArea.setText("Erreur : " + ex.getMessage());
                }
            });


            // Onglet Analyse
            JPanel analysePanel = new JPanel(new GridLayout(4, 1, 10, 10));
            JTextField dateField = new JTextField("2025-05-28"); // Format attendu : yyyy-MM-dd
            JButton btnCalculerCA = new JButton("Calculer Chiffre d'Affaires");
            JLabel resultatLabel = new JLabel("💰 Chiffre d'affaires : 0.00 €");

            analysePanel.add(new JLabel("📅 Date (yyyy-MM-dd) :"));
            analysePanel.add(dateField);
            analysePanel.add(btnCalculerCA);
            analysePanel.add(resultatLabel);

            btnCalculerCA.addActionListener(e -> {
                try {
                    String date = dateField.getText();
                    double ca = service.calculerChiffreAffaires(date);
                    resultatLabel.setText("💰 Chiffre d'affaires : " + String.format("%.2f", ca) + " €");
                } catch (Exception ex) {
                    resultatLabel.setText("❌ Erreur : " + ex.getMessage());
                }
            });




            // Onglets
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Articles", articlePanel);
            tabbedPane.addTab("➕ Ajouter Stock", stockPanel);
            tabbedPane.addTab("Facturation", facturationPanel);
            tabbedPane.addTab("Analyse", analysePanel);


            frame.add(tabbedPane);
            frame.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Connexion au serveur RMI échouée : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
