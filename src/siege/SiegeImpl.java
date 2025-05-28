package siege;

import rmi.ServiceSiege;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class SiegeImpl extends UnicastRemoteObject implements ServiceSiege {
    protected SiegeImpl() throws RemoteException {
        super();
        System.out.println("Constructeur SiegeImpl lancé");
        }
    }