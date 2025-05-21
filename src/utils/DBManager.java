package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
    private static final String URL = "jdbc:mysql://localhost:3306/bricomerlin";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // ton mot de passe ici

    // Méthode statique pour récupérer une connexion
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
