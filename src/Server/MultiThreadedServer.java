package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import Common.UserDB;

public class MultiThreadedServer {
    private UserDB users;
    private WalletDB wallets;
    private PostDB posts;
    private CredentialDB credentials;
    private ServerNotificationService notificationService;
    private int TCPPORT;
    private int UDPPORT;
    private int MCASTPORT;
    private String MULTICAST;
    private String SERVER;
    private String RECOVERY_FILE_PATH;

    public MultiThreadedServer(UserDB users, WalletDB wallets, PostDB posts, CredentialDB credentials,
            ServerNotificationService notificationService, int TCPPORT, int UDPPORT, String MULTICAST, int MCASTPORT, String SERVER, String RECOVERY_FILE_PATH) {
        this.users = users;
        this.wallets = wallets;
        this.posts = posts;
        this.credentials = credentials;
        this.notificationService = notificationService;
        this.TCPPORT = TCPPORT;
        this.UDPPORT = UDPPORT;
        this.MULTICAST = MULTICAST;
        this.MCASTPORT = MCASTPORT;
        this.SERVER = SERVER;
        this.RECOVERY_FILE_PATH = RECOVERY_FILE_PATH;
    }


    public void start() {
        try(ServerSocket listeningSocket = new ServerSocket()){
            listeningSocket.bind(new InetSocketAddress(SERVER, TCPPORT));
            ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
         
            while(true){
                System.out.println("Waiting for clients");
                Socket activeSocket = listeningSocket.accept();
                System.out.println("SYSTEM:: un utente si è connesso al sistema");
                threadPool.execute(new RequestHandler(activeSocket,users, wallets, posts, credentials, notificationService, UDPPORT, MCASTPORT, MULTICAST, RECOVERY_FILE_PATH));
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
