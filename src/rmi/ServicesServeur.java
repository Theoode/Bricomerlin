package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.List;
import java.util.Map;

public interface ServicesServeur extends Remote {
    String consulterStock(String reference) throws RemoteException;
    List<String> rechercherArticlesParFamille(String famille) throws RemoteException;
    List<String> getFamillesDisponibles() throws RemoteException;
    boolean ajouterStockProduit(String reference, int quantite) throws RemoteException;
    boolean creerCommande(Map<String, Integer> articles) throws RemoteException;
    List<String> getArticlesDisponibles() throws RemoteException;
    String getStatutPaiement(int idCommande) throws RemoteException;
    boolean reglerCommande(int idCommande) throws RemoteException;
    double calculerChiffreAffaires(String date) throws RemoteException;
}

