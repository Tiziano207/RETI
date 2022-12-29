package Server;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import Client.RegistrationInterface;
import Common.UserDB;

public class RegistrationClass extends RemoteServer implements RegistrationInterface {
    private UserDB users;
    private CredentialDB credentialDB;
    private WalletDB wallets;
    private int RMI_Port;
    private String RECOVERY_FILE_PATH;

    public RegistrationClass(UserDB users, CredentialDB credentialDB, WalletDB wallets, int PORT, String RECOVERY_FILE_PATH){
        this.users = users;
        this.credentialDB = credentialDB;
        this.wallets = wallets;
        credentialDB = new CredentialDB();
        RMI_Port = PORT;
        this.RECOVERY_FILE_PATH = RECOVERY_FILE_PATH;
    }

    @Override
    public String register(ArrayList<String> myArgs) throws RemoteException {
        String username = myArgs.get(1);
        String password = myArgs.get(2);
        
        myArgs.remove(username);
        myArgs.remove(password);
        //Scrittura in cache
        synchronized(wallets){
            wallets.createWallet(username);
            //scrittura sul file Json
            writeOnDBWallet();
        }
        //Scrittura in cache
        synchronized(credentialDB){
            credentialDB.register(username, password);
            //scrittura sul file Json
            writeOnDBCredentials();
        }
        //Scrittura in cache
        synchronized(users){
            users.register(username, myArgs); 
            // Scrittura sul file JSON
            writeOnDBUsers();
        }
        System.out.println("SYSTEM:: Utente inserito nel database");
        return null;
    }

    public void start() {
        try{ 
            RegistrationInterface stub = (RegistrationInterface) UnicastRemoteObject.exportObject(this, 0);
            LocateRegistry.createRegistry(RMI_Port);
            Registry r = LocateRegistry.getRegistry(RMI_Port);
            r.rebind("RegisterUser", stub);
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }
    
    private void writeOnDBWallet() {
        // Scrittura sul file JSON
        ObjectMapper objectMapper = new ObjectMapper();   
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
        // Writing to a file
            File file= new File(RECOVERY_FILE_PATH + "/" + "wallets.json");
            objectMapper.writeValue(file, wallets);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeOnDBUsers(){
        // Scrittura sul file JSON
        ObjectMapper objectMapper = new ObjectMapper();   
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
        // Writing to a file
            File file= new File(RECOVERY_FILE_PATH + "/" + "utentiRegistrati.json");
            objectMapper.writeValue(file, users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeOnDBCredentials(){
        // Scrittura sul file JSON
        ObjectMapper objectMapper = new ObjectMapper();   
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
        // Writing to a file
            File file= new File(RECOVERY_FILE_PATH + "/" + "credentials.json");
            objectMapper.writeValue(file, credentialDB);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
