package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Client.NotificationSystemClientInterface;

public interface NotificationSystemServerInterface extends Remote{

    public void registerForCallback(NotificationSystemClientInterface stub) throws RemoteException;
    
    public void unregisterForCallback(NotificationSystemClientInterface clientInterface) throws RemoteException;
}
