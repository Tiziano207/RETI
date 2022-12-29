package Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Common.UserDB;

public interface NotificationSystemClientInterface extends Remote{
    public void copyUserDB(UserDB newUserSB) throws RemoteException;
}
