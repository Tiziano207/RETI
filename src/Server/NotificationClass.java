package Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class NotificationClass {
    private int RMI_CALL_BACK_PORT;

    public ServerNotificationService start(int port) {
        this.RMI_CALL_BACK_PORT = port;
        try {
            ServerNotificationService server = new ServerNotificationService();
            NotificationSystemServerInterface stub = (NotificationSystemServerInterface) UnicastRemoteObject.exportObject(server, 39000);

            LocateRegistry.createRegistry(RMI_CALL_BACK_PORT);
            Registry r  = LocateRegistry.getRegistry(RMI_CALL_BACK_PORT);
            r.rebind("NotificationService", stub);

            return server;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
