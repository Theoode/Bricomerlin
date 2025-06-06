package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface ServiceSiege extends Remote {
    Map<String, Double> getPrixArticles() throws RemoteException;

    String synchroniserTousLesPrix() throws RemoteException;

}

