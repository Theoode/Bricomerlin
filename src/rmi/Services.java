package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

// Créer l'interface de l'objet distant
public interface Services extends Remote {
    String consulterStock(String reference) throws RemoteException;
    List<String> rechercherArticlesParFamille(String famille) throws RemoteException;
    boolean ajouterStockProduit(String reference, int quantite) throws RemoteException;
    boolean creerCommande(Map<String, Integer> articles) throws RemoteException;
    List<String> getArticlesDisponibles() throws RemoteException;
    boolean acheterArticle(String reference, int quantite) throws RemoteException;
}