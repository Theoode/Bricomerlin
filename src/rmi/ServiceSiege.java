package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServiceSiege extends Remote {
    String synchroniserTousLesPrix() throws RemoteException;
}
