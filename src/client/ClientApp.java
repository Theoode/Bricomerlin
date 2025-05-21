package client;

import rmi.Services;

import javax.swing.*;
import java.awt.*;
import java.rmi.Naming;
import java.util.List;

public class ClientApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientApp::createUI);
    }

    private static void createUI() {
        JFrame frame = new JFrame("BricoMerlin - Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Composants du haut
        JPanel topPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        JTextField refField = new JTextField();
        JTextField qteField = new JTextField();
        JTextField familleField = new JTextField();
        JButton btnConsulter = new JButton("Consulter l'article");
        JButton btnAcheter = new JButton("Acheter l'article");
        JButton btnRechercherFamille = new JButton("Rechercher par famille");

        JTextArea resultArea = new JTextArea(12, 50);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        topPanel.add(new JLabel("Référence article :"));
        topPanel.add(refField);
        topPanel.add(new JLabel("Quantité (pour achat) :"));
        topPanel.add(qteField);
        topPanel.add(new JLabel("Nom de la famille :"));
        topPanel.add(familleField);
        topPanel.add(btnConsulter);
        topPanel.add(btnAcheter);
        topPanel.add(btnRechercherFamille);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        try {
            Services service = (Services) Naming.lookup("rmi://localhost/ServiceStock");
            resultArea.append("✅ Connecté au serveur RMI !\n");

            btnConsulter.addActionListener(e -> {
                try {
                    String ref = refField.getText();
                    String res = service.consulterStock(ref);
                    resultArea.append("\n🟡 Infos article :\n" + res + "\n");
                } catch (Exception ex) {
                    resultArea.append("❌ Erreur lors de la consultation : " + ex.getMessage() + "\n");
                }
            });

            /*btnAcheter.addActionListener(e -> {
                try {
                    String ref = refField.getText();
                    int qte = Integer.parseInt(qteField.getText());
                    boolean success = service.acheterArticle(ref, qte);
                    resultArea.append("\n🟢 Achat " + (success ? "réussi" : "échoué") + " pour " + qte + " exemplaire(s) de " + ref + "\n");
                } catch (Exception ex) {
                    resultArea.append("❌ Erreur lors de l'achat : " + ex.getMessage() + "\n");
                }
            });*/

            btnRechercherFamille.addActionListener(e -> {
                try {
                    String nomFamille = familleField.getText();
                    List<String> articles = service.rechercherArticlesParFamille(nomFamille);

                    if (articles.isEmpty()) {
                        resultArea.append("\n🔎 Aucun article trouvé dans la famille \"" + nomFamille + "\"\n");
                    } else {
                        resultArea.append("\n🔎 Articles de la famille \"" + nomFamille + "\" :\n");
                        for (String article : articles) {
                            resultArea.append("- " + article + "\n");
                        }
                    }
                } catch (Exception ex) {
                    resultArea.append("❌ Erreur lors de la recherche par famille : " + ex.getMessage() + "\n");
                }
            });

        } catch (Exception e) {
            resultArea.append("❌ Erreur de connexion au serveur : " + e.getMessage() + "\n");
            e.printStackTrace();
        }

        frame.setVisible(true);
    }
}
