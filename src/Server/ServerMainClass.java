package Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import Common.UserDB;

public class ServerMainClass {
    static String SERVER;
    static int TCPPORT;
    static int UDPPORT;
    public static String MULTICAST;
    static int MCASTPORT;
    static String REGHOST;
    static int REGPORT;
    static int REGCALLBCK;
    static int TIMEOUT;
    static long UPDATE;

    //dove sono contenuti i file per ripristinare lo stato del sistema
    public final static String RECOVERY_FILE_PATH = "./src/Server/Recovery";
    private final static String usersDB = "utentiRegistrati.json"; 
    private final static String postsDB = "postsDB.json";
    private final static String walletsDB = "wallets.json";
    private final static String credentialsDB = "credentials.json";

  
    public volatile static WalletDB wallets = new WalletDB();
    public volatile static PostDB posts = new PostDB();
    public volatile static CredentialDB credentials = new CredentialDB();
    public volatile static UserDB users = new UserDB();
    public static void main(String[] args) {
        /**
         * Funzione che legge il file e inizializza le variiabili 
         */
        configuration();
        /**
         * Funzione che legge i DB che per comodità sono in .json, dopo aver chiamato la funzione avrò gli oggetti users, wallets, posts, credentials inizializzati all'ultimo backup
         */
        try {
            restoreBackup();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
          /**
        * RMI Riferimento all'oggetto remoto
        */
        new RegistrationClass(users,credentials,wallets,REGPORT,RECOVERY_FILE_PATH).start(); 
        /**
        * RMI CallBack
        */
       ServerNotificationService notificationService = new NotificationClass().start(REGCALLBCK);
       /**
        * Multicast Sender
        */

        ServerMulticastingService multicastSender = new ServerMulticastingService().start(MCASTPORT,MULTICAST);
        RewardingCalculationTsk rewardingTask = new RewardingCalculationTsk(multicastSender);
        Thread threadDeamonMulticast = new Thread(rewardingTask);
        threadDeamonMulticast.setDaemon(true);
        threadDeamonMulticast.start();
    
        new MultiThreadedServer(users,wallets,posts,credentials, notificationService,TCPPORT, UDPPORT, MULTICAST, MCASTPORT, SERVER, RECOVERY_FILE_PATH).start();
        
    }
    /**
     * Funzione utilizzata per il restore dei dati 
     * @throws ClassNotFoundException
     */
    private static void restoreBackup() throws ClassNotFoundException {
        Boolean usersTAKEN = false;
        Boolean postsTAKEN = false;
        Boolean walletsTAKEN = false;
        Boolean credentialsTAKEN = false;
        
        File recoveryDir = new File(RECOVERY_FILE_PATH);
        ObjectMapper objectMapper = new ObjectMapper();
        for(File dir : recoveryDir.listFiles()){
            if(dir.getName().equals(usersDB)){
                Path path = Path.of(RECOVERY_FILE_PATH, usersDB);
		        File file = new File(path.toString());
		        try {
			        users = objectMapper.readValue(file, UserDB.class);
                    usersTAKEN = true;
		        } catch (IOException e) {
			        e.printStackTrace();
		        }

           }else if(dir.getName().equals(postsDB)){
                Path path = Path.of(RECOVERY_FILE_PATH, postsDB);
                File file = new File(path.toString());
                try {
                    posts = objectMapper.readValue(file, PostDB.class);
                    postsTAKEN = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            
            }else if(dir.getName().equals(walletsDB)){
                Path path = Path.of(RECOVERY_FILE_PATH, walletsDB);
                File file = new File(path.toString());
                try {
                    wallets = objectMapper.readValue(file, WalletDB.class);
                    walletsTAKEN = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else if(dir.getName().equals(credentialsDB)){
                Path path = Path.of(RECOVERY_FILE_PATH, credentialsDB);
                File file = new File(path.toString());
                try {
                    credentials = objectMapper.readValue(file, CredentialDB.class);
                    credentialsTAKEN = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    }
            }

        }

        if(!usersTAKEN){           
            //Cosa fas se non c'è il file
		    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    try {
			    // Writing to a file
                Path path = Path.of(RECOVERY_FILE_PATH, usersDB);
                File file = new File(path.toString());
			    objectMapper.writeValue(file, users);
		    } catch (IOException e) {
			    e.printStackTrace();
		    }

        }

      if(!postsTAKEN){           
            //Cosa fas se non c'è il file
		    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    try {
			    // Writing to a file
                Path path = Path.of(RECOVERY_FILE_PATH, postsDB);
                File file = new File(path.toString());
			    objectMapper.writeValue(file, posts);
		    } catch (IOException e) {
			    e.printStackTrace();
		    }
        }
        if(!walletsTAKEN){           
            //Cosa fas se non c'è il file
		    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    try {
			    // Writing to a file
                
                Path path = Path.of(RECOVERY_FILE_PATH, walletsDB);
                File file = new File(path.toString());
			    objectMapper.writeValue(file, wallets);
		    } catch (IOException e) {
			    e.printStackTrace();
		    }
        }

        
        if(!credentialsTAKEN){           
            //Cosa fas se non c'è il file
		    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    try {
			    // Writing to a file
                Path path = Path.of(RECOVERY_FILE_PATH, credentialsDB);
                File file = new File(path.toString());
			    objectMapper.writeValue(file, credentials);
		    } catch (IOException e) {
			    e.printStackTrace();
		    }
        }
        System.out.println("SYSTEM:: DB restored");

}  
    
    /**
     * Funzione utilizzata per accedere al file di configurazione  
     */
    private static void configuration() {
        try{
            //String configFile = "/File/config.txt";
            
            String currentPath = new java.io.File(".").getCanonicalPath();
            Path path = Path.of(currentPath.toString(), "File","config.txt");
           // System.out.println("PATH" + currentPath);
            FileReader file;
            file = new FileReader(path.toString());

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
                }else if(line.startsWith("REGCALLBCK")){
                    String[] split = line.split("=");
                    REGCALLBCK= Integer.parseInt(split[1]);
                }else if(line.startsWith("UPDATE")){
                    String[] split = line.split("=");
                    UPDATE = Integer.parseInt(split[1]);          
                }else if(line.startsWith("TIMEOUT")){
                    String[] split = line.split("=");
                    TIMEOUT = Integer.parseInt(split[1]);
                }
            }

            buff.close();
        
        }catch(IOException e){
            System.out.println(e);
        }
    }
}
