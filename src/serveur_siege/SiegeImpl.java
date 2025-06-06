package serveur_siege;

import rmi.ServiceSiege;
import utils.DBManagerSiege;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SiegeImpl extends UnicastRemoteObject implements ServiceSiege {

    protected SiegeImpl() throws RemoteException {
        super();
        System.out.println("Constructeur SiegeImpl lancé");
    }

    @Override
    public Map<String, Double> getPrixArticles() throws RemoteException {
        Map<String, Double> prixMap = new HashMap<>();
        try (Connection conn = DBManagerSiege.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT idReference, prixUnitaire FROM article");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                prixMap.put(rs.getString("idReference"), rs.getDouble("prixUnitaire"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Erreur SQL dans getPrixArticles", e);
        }
        return prixMap;
    }

    @Override
    public String synchroniserTousLesPrix() {
        return "";
    }

}
