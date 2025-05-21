package client;

import rmi.Services;

import javax.swing.*;
import java.awt.*;
import java.rmi.Naming;

public class ClientApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientApp::createUI);
    }

    private static void createUI() {
        JFrame frame = new JFrame("BricoMerlin - Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField refField = new JTextField();
        JTextField qteField = new JTextField();
        JButton btnConsulter = new JButton("Consulter l'article");
        JButton btnAcheter = new JButton("Acheter l'article");

        JTextArea resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        topPanel.add(new JLabel("Référence article :"));
        topPanel.add(refField);
        topPanel.add(new JLabel("Quantité (pour achat) :"));
        topPanel.add(qteField);
        topPanel.add(btnConsulter);
        topPanel.add(btnAcheter);

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

        } catch (Exception e) {
            resultArea.append("❌ Erreur de connexion au serveur : " + e.getMessage() + "\n");
            e.printStackTrace();
        }

        frame.setVisible(true);
    }
}
