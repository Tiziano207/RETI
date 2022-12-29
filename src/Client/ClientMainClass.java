package Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import Common.UserDB;
import Server.NotificationSystemServerInterface;



public class ClientMainClass {
   
    static String SERVER;
    static int TCPPORT;
    static int UDPPORT;
    static String MULTICAST;
    static int MCASTPORT;
    static String REGHOST;
    static int REGCALLBCK;
    static int REGPORT;
    static int TIMEOUT;
    public static boolean CLOSE;

    private static RegistrationInterface registration;

    public static void main(String[] args) {
        CLOSE = true;
        BufferedReader reader; // stream dal server TCP al client
        BufferedWriter writer; // stream dal client TCP al server
        String message = null; //comandi
        String result = null; 
        Boolean flag = true;
        Boolean logIn_effettuato = false;
        UserDB localUsersDB = new UserDB();
        String username = null;
        /**
         * Lettura del file di config
         */
        configuration();
        try(Socket socket = new Socket(); 
                BufferedReader cmd_line = new BufferedReader(new InputStreamReader(System.in))){  
            /**
            * RMI callbacks
            */
              
            Registry registry = LocateRegistry.getRegistry(REGCALLBCK);
            NotificationSystemServerInterface server = (NotificationSystemServerInterface) registry.lookup("NotificationService");

            NotificationSystemClientInterface callbackObj = new ClientNotificationService(localUsersDB);
            NotificationSystemClientInterface stub = (NotificationSystemClientInterface) UnicastRemoteObject.exportObject(callbackObj, 0);

            /**
             * Multicast
            */
            MulticastSocket ms = new MulticastSocket(MCASTPORT);
            System.out.println("Registering for Multicasting...");
            MulticastingTsk mTask = new MulticastingTsk(ms, MULTICAST);
            Thread mThread = new Thread(mTask);
            mThread.setDaemon(true);
            /**
             * RMI 
             */
            Registry r = LocateRegistry.getRegistry(REGPORT);
            /**
             * Connesione TCP con il Server andata a buon fine 
             */
            System.out.println("< Welcome in WINSOME please Login or Register your Account");  
            socket.connect(new InetSocketAddress(SERVER, TCPPORT));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            do{
                System.out.print("> ");
                message = cmd_line.readLine();
                String[] myArgs = message.split(" ");

                if(!logIn_effettuato){
                    System.out.println("< Please Login");
                }

                if (message.startsWith("logout" ) && myArgs[1].equals(username) && logIn_effettuato ){
                    //invio "logout" al server in modo tale che esso chiuda tutte le connessioni con il client
                    writer.write(message + "\r\n");
                    writer.flush();
                    //Aspetto che il server mi dia la conferma del logout
                    while(!(result = reader.readLine()).equals("")){
                        System.out.println("< " + result);
                    }
                    //se mi dà conferma cambio la condizione false in modo tale da uscire dal ciclo e terminare il processo
                   logIn_effettuato= false;
                   flag = false;

                } else if (message.startsWith("login")) {
                    server.registerForCallback(stub);
                    writer.write(message + "\r\n");
                    writer.flush();

                    while(!(result = reader.readLine()).equals("")){
                        if(result.equals("< OK")){
                            //Il login è stato effettuato e può procedere a eseguire le operazioni
                            System.out.println("< login: " + "Done");
                            logIn_effettuato = true;
                            mThread.start();
                        } else 
                            System.out.println(result);
                    }
                    
                    username = myArgs[1];
                // la richiesta viene eseguita in locale viisto che tramite il meccanismo RMI callback esso viene aggiornato dal server
                } else if (message.startsWith("list followers") && logIn_effettuato) {

                    listFollowersFunction(username, localUsersDB);

                } else if (message.startsWith("register")  && !logIn_effettuato) {
                    //Gestita da RMI
                    result = register(myArgs,r);
                }else if(logIn_effettuato){
                    //Mando il messaggio al Server con conssessione TCP e Pulisco il buffer 
                    
                    writer.write(message + "\r\n");
                    writer.flush();
                    //Attendo che il Server restituisca ciò che ho richiesto
                    while(!(result = reader.readLine()).equals("")){
                        System.out.println("< " + result);
                    }
                }
                
            }while(flag);

            //Chiusura connesione TCP 
            socket.close();

            //Chiudo il canale RMI
            UnicastRemoteObject.unexportObject(callbackObj, false); 
       
            //Disiscrizione callback
            server.unregisterForCallback(stub); 
        
            //Chiusura canale Multicast
            CLOSE = false;
          
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private static void listFollowersFunction(String username, UserDB localUsersDB) {
        String result = "";
        result = "< Ti seguono: "+ localUsersDB.getDB().get(username);
        System.out.println(result.substring(0,result.length()));
    }
    
    /**
    * Metodo per la registrazione tramite meccanismo RMI
    * Con RMI ottengo un riferimento all'oggetto remoto sul quale invocare i metodi 
    * @param myArgs
     * @param r
    */
    private static String register(String[] myArgs, Registry r) {
        String reply = "Register non andato a buon fine, riprova";
        
        try{
            /**
             * richiedo l'oggetto dal nome pubblico
             */
            registration = (RegistrationInterface) r.lookup("RegisterUser");
            ArrayList<String> args = new ArrayList<>();
            for(String i : myArgs){
                args.add(i);
            }
            if(args.size() < 4)
                return reply;
            reply = registration.register(args);
        }catch(Exception e){
            e.printStackTrace();
        }
        return reply;
    }

    /**
    * Metodo per l'acquisizione dei parametri per la configurazione, si basa du buffer IO, il metodo legge il path corrente prende il file di configurazione e estrapola i parametri 
    * 
    */
    private static void configuration() {
        try{
            String configFile = "/File/config.txt";
            String currentPath = new java.io.File(".").getCanonicalPath() + configFile;
            FileReader file;
            file = new FileReader(currentPath);

            BufferedReader buff;
            buff = new BufferedReader(file);

            String line;
            line=buff.readLine();
            while((line = buff.readLine())!=null){
                if(line.startsWith("#")){
                    continue;
                }else if(line.startsWith("SERVER")){
                    String[] split = line.split("=");
                    SERVER = split[1];
                }else if(line.startsWith("TCPPORT")){
                    String[] split = line.split("=");
                    TCPPORT = Integer.parseInt(split[1]);
                }else if(line.startsWith("UDPPORT")){
                    String[] split = line.split("=");
                    UDPPORT = Integer.parseInt(split[1]);
                }else if(line.startsWith("MULTICAST")){
                    String[] split = line.split("=");
                    MULTICAST = split[1];
                }else if(line.startsWith("MCASTPORT")){
                    String[] split = line.split("=");
                    MCASTPORT = Integer.parseInt(split[1]);
                }else if(line.startsWith("REGHOST")){
                    String[] split = line.split("=");
                    REGHOST = split[1];
                }else if(line.startsWith("REGPORT")){
                    String[] split = line.split("=");
                    REGPORT = Integer.parseInt(split[1]);
                }else if(line.startsWith("TIMEOUT")){
                    String[] split = line.split("=");
                    TIMEOUT = Integer.parseInt(split[1]);
                }else if(line.startsWith("REGCALLBCK")){
                    String[] split = line.split("=");
                    REGCALLBCK = Integer.parseInt(split[1]);
                }
            }

            buff.close();
        
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
