package Client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RegistrationInterface extends Remote {

    /*
        @Overview: il server pubblica questo metodo che permette ad un utente di registrarsi alla piattaforma
        @Return: un messaggio che indica l'esito dell'operazione
    */
    public String register(ArrayList<String>myArgs) throws RemoteException;
}