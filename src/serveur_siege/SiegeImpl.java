package serveur_siege;

import rmi.ServiceSiege;
import utils.DBManagerMagasin;
import utils.DBManagerSiege;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SiegeImpl extends UnicastRemoteObject implements ServiceSiege {
    protected SiegeImpl() throws RemoteException {
        super();
        System.out.println("Constructeur SiegeImpl lancé");
        }

    @Override
    public String synchroniserTousLesPrix() throws RemoteException {
        StringBuilder log = new StringBuilder();

        try (
                Connection siegeConn = DBManagerSiege.getConnection();
                Connection magasinConn = DBManagerMagasin.getConnection();
                PreparedStatement selectStmt = siegeConn.prepareStatement("SELECT idReference, prixUnitaire FROM article");
                PreparedStatement updateStmt = magasinConn.prepareStatement("UPDATE article SET prixUnitaire = ? WHERE idReference = ?");
        ) {
            ResultSet rs = selectStmt.executeQuery();

            while (rs.next()) {
                String ref = rs.getString("idReference");
                double nouveauPrix = rs.getDouble("prixUnitaire");

                updateStmt.setDouble(1, nouveauPrix);
                updateStmt.setString(2, ref);

                int rows = updateStmt.executeUpdate();
                if (rows > 0) {
                    log.append("✅ ").append(ref).append(" mis à jour à ").append(nouveauPrix).append(" €\n");
                } else {
                    log.append("⚠️ ").append(ref).append(" introuvable dans la base magasin\n");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "❌ Erreur SQL : " + e.getMessage();
        }

        return log.toString();
    }

}