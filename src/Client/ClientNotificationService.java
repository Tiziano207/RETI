package Client;


import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

import Common.UserDB;


public class ClientNotificationService extends RemoteObject implements NotificationSystemClientInterface {

    private static final long serialVersionUID = 1L;
    private UserDB users;              //un riferimento alla struttura dait locale del client
    /**
     * crea un nuovo callback client
     */
    public ClientNotificationService(UserDB localUsersDB) throws RemoteException {
        super();
        users = localUsersDB;
    }

    public void copyUserDB(UserDB newUserSB) throws RemoteException{
        users.copy(newUserSB);
    }

}

