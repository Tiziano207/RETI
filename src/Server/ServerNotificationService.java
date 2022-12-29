package Server;

import java.rmi.*;
import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Client.NotificationSystemClientInterface;
import Common.UserDB;


public class ServerNotificationService extends RemoteServer implements NotificationSystemServerInterface {
 /**
     * lista dei client registrati
     */
    private List<NotificationSystemClientInterface> clients;

    public ServerNotificationService() throws RemoteException {
        super();
        clients = new ArrayList<NotificationSystemClientInterface>();
    }

    public synchronized void registerForCallback(NotificationSystemClientInterface clientInterface) throws RemoteException {
        if (!clients.contains(clientInterface)) {
            clients.add(clientInterface);
            System.out.println("SYSTEM: New client has successful registered to Multicast");
        }
    }

    public synchronized void unregisterForCallback(NotificationSystemClientInterface clientInterface) throws RemoteException {
        if (clients.contains(clientInterface)) {
            clients.remove(clientInterface);
            System.out.println("SYSTEM: Client has successful unregistered ;)");
        } else {
            System.out.println("SYSTEM: Client unable to unregistered");
        }
    }

    /**
     * notifica di una vaiazione di valore dell'azione quando viene richiamato fa il
     * callback a tutti i client registrati
     */

    public void update(UserDB users) throws RemoteException {
        doCallbacks(users);
    }

    private synchronized void doCallbacks(UserDB users) throws RemoteException {
        Iterator<NotificationSystemClientInterface> i = clients.iterator();
        while(i.hasNext()){ 
            NotificationSystemClientInterface client = (NotificationSystemClientInterface)i.next();
            client.copyUserDB(users);
        }
        
    }

    
}