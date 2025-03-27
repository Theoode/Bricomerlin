import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
    public static void main(String[] args) {
        // URL de connexion au format JDBC
        String url = "jdbc:mysql://localhost:3306/bricomerlin"; // Remplace par le nom de ta base
        String user = "root"; // Remplace par ton utilisateur MySQL
        String password = ""; // Remplace par ton mot de passe MySQL 

        try {
            // Charger le driver (inutile en Java 8+ mais peut être nécessaire dans certaines configs)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Établir la connexion
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion réussie à la base de données !");

            // Fermer la connexion (bonne pratique)
            connection.close();
        } catch (ClassNotFoundException e) {
            System.out.println("Erreur : Driver JDBC non trouvé !");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
