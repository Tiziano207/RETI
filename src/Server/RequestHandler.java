package Server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.Socket;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import Common.UserDB;

public class RequestHandler implements Runnable {
    String RECOVERY_FILE_PATH;
    Socket clientSocket;
    UserDB users;
    WalletDB wallets; 
    PostDB posts; 
    String username;
    Boolean login_effettuato = false;
    CredentialDB credentials;
    ServerNotificationService notificationService; 
    int UDPPORT;
    int MCASTPORT;
    String MULTICAST;

    public RequestHandler(Socket activeSocket, UserDB users, WalletDB wallets, PostDB posts, CredentialDB credentials,
            ServerNotificationService notificationService, int UDPPORT, int MCASTPORT, String MULTICAST,String RECOVERY_FILE_PATH) {
                clientSocket = activeSocket;
                this.users = users;
                this.wallets = wallets;
                this.posts = posts;
                this.credentials = credentials;
                this.notificationService = notificationService;
                this.UDPPORT = UDPPORT;
                this.MULTICAST = MULTICAST;
                this.MCASTPORT = MCASTPORT;
                this.RECOVERY_FILE_PATH = RECOVERY_FILE_PATH;
    }

    @Override
    public void run() {
        String request;
        boolean online = true;
        String reply = null;

        while(online){
           /**
            * Avvio la connessione e apro i buffer per inviare e ricevere dati 
            */
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));){          
      
            while ((request = reader.readLine()) != null && online)  {
                /**
                 * entro in un ciclo il quale decodifica la richiesta del client che avviene tramite stringa e passa la richiesta alla funzione di riferimento
                 * il ciclo continua finoa che non riceve un oggetto di tipo null
                 */
    
                
                String[] myArgs = request.split(" ");
              
                if (myArgs[0].equals("logout")) {
                    reply = logoutHandler(myArgs,online);                 
                } else if (myArgs[0].equals("login")) {
                    try{
                        //Appena eseguito il login l'utente riceve il database aggiornato
                        notificationService.update(this.users);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    reply = loginHandler(myArgs);
                } else if (request.startsWith("list user") && login_effettuato) {
                    reply = listUserHandler();
                } else if (request.startsWith("list following")) {
                    reply = listFollowingHandler();
                } else if (myArgs[0].equals("follow")) {
                    reply = followUserHandler(myArgs);
                } else if (myArgs[0].equals("unfollow")) {
                    reply = unfollowUserHandler(myArgs);
                } else if (myArgs[0].equals("blog")) {
                    reply = viewBlogHandler();
                } else if (myArgs[0].equals("post")) {
                    reply = createPostHandler(request);
                } else if (request.startsWith("show feed")) {
                    reply = showFeedHandler();
                } else if (request.startsWith("show post")) {
                    reply = showPostHandler(myArgs);
                } else if (myArgs[0].equals("delete")) {
                    reply = deletePostHandler(myArgs);
                } else if (myArgs[0].equals("rewin")) {
                    reply = rewinPostHandler(myArgs);
                } else if (myArgs[0].equals("rate")) {
                    reply = ratePostHandler(myArgs);
                } else if (myArgs[0].equals("comment")) {
                    reply = addCommentHandler(request);
                } else if (request.startsWith("wallet btc")) {
                    reply = getWalletBTCHandler();
                } else if (myArgs[0].equals("wallet")) {
                    reply = getWalletHandler();
                } else 
                    reply = invalidOptionHandler();
               
                //if (!myArgs[0].equals("logout")) {                                
                    writer.write(reply + "\n\r\n");
                    writer.flush();
    
                //}
                
            }
            reader.close();
            writer.close();
            clientSocket.close();
            return;
            }catch(Exception e){
                e.printStackTrace();
                break;
            }
        }
    }
/**
 * Il metodo logout si occupa della gestione del logout dell'utente, riceve in input la stringa arrivata dal client e decide se è possibile effettuare il logout oppure l'username non è corretto
 * cambia il valore online in modo tale da gestire l'uscita dal ciclo
 * @param myArgs
 * @param online
 * @return
 */
    private String logoutHandler(String[] myArgs, boolean online) {
        if(myArgs[1].equals(username)){
            System.out.println("Username: " + username + "\nmyArgs: " + myArgs[1]);
            login_effettuato = false;
            online= false;
            return "Bye Bye <"+ myArgs[1] + ">" ;    
        }
        return "Username non valido" ;
    }

    private String invalidOptionHandler() {
        String reply = "Select operation:";
    
        reply = reply + "\n\t\"register username password\" -per registrare un utente";
        reply = reply + "\n\t\"login username password\" -per eseguire il login";
        reply = reply + "\n\t\"logout username\" -per eseguire il logout";
        reply = reply + "\n\t\"list users\" -per mostrare tutti gli Utenti con un almeno un Tag uguale al tuo";
        reply = reply + "\n\t\"list followers\" -per mostrare i tuoi followers";
        reply = reply + "\n\t\"list following\" -per mostrare chi segui";
        reply = reply + "\n\t\"follow user IDutente\" -per seguire un utente";
        reply = reply + "\n\t\"unfollow user IDutente\" -per smettere di seguire un utente";
        reply = reply + "\n\t\"view blog\" -per mostrare il tuo blog";
        reply = reply + "\n\t\"post \"Title\" \"Content\"\" -per postare sul tuo blog";
        reply = reply + "\n\t\"show post IDpost\" -per mostrare un post";
        reply = reply + "\n\t\"show feed\" -per mostrare i post di tutti gli utenti che segui";
        reply = reply + "\n\t\"delete post IDpost\" -per eliminare un post che hai creato";
        reply = reply + "\n\t\"rewin IDpost\" -per inoltrare un post di un altro utente";
        reply = reply + "\n\t\"rate IDpost +1/-1\" -per votare un post di un altro utente";
        reply = reply + "\n\t\"comment IDpost comment\" -per commentare un post di un altro utente";
        reply = reply + "\n\t\"wallet\" -per vedere il saldo del tuo Wallet";
        reply = reply + "\n\t\"wallet btc\" -per vedere il saldo del tuo Wallet in BTC";
        return reply;
    }

    private String getWalletHandler() {
        String result = null;
        synchronized(wallets){
            for(Wallet w : wallets.getWallets()){
                if(w.getUser().equals(username)){
                    result = "Il Saldo del tuo Wallet è di: " + w.getSaldo() + "\n";
                    for(String t : w.getTransactionDates().keySet()){
                        result = result + t.toString() + " : " + w.getTransactionDates().get(t) + "\n";
                    }
                }
            }
        }
        return result.substring(0,result.length()-1);
    }

    private String getWalletBTCHandler() {
        String result = null;
        float walletSaldo = 0.0f;
        synchronized(wallets){
            for(Wallet w : wallets.getWallets()){
                if(w.getUser().equals(username))
                    walletSaldo = w.getSaldo();
            }
        }
        try{
            
            URL url= new URL("https://www.random.org/decimal-fractions/?num=1&dec=10&col=2&format=html&rnd=new");
            InputStream stream= url.openStream();
            stream = new BufferedInputStream(stream);
            Reader r = new InputStreamReader(stream);
            BufferedReader in = new BufferedReader(r);
            String temp = in.readLine();
            float variation = 0.0f;

            while(temp != null){
                if(temp.startsWith("<pre class=\"data\">")){
                    String [] token = temp.split(" ");
                    String [] token1 = token[1].split("<");
                    String [] token2 = token1[0].split(">");
                    variation = Float.parseFloat(token2[1]);
                    variation = variation + 1;
                    //variation btc = 1 winsome 
    
                    result = "Il Saldo del tuo Wallet in BTC è di: "  + variation*walletSaldo;
                    break;
                }

                temp = in.readLine();
            }
            
            stream.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        return result;
    }

    private String addCommentHandler(String request) {
        String[] myArgs = request.split(" "); 
        String req = request;
        int id = Integer.parseInt(myArgs[1]);
        
        //Post non presente nel DB
        synchronized(posts){
            if(!posts.getPost().keySet().contains(id))
                return "Il post che stai cercando di commentare non esiste";
        
            //Non puo icommentare i tuoi stessi post
            if(posts.getPost().get(id).getAutore().equals(username))
                return "Non puoi commentare i tuoi stessi post";
        
            //Devi prima seguire gli utenti prima di commentare
            if(!users.getDB().get(posts.getPost().get(id).getAutore()).contains(username))
                return "Devi seguire l'Utente prima di commentare";
       
            req = myArgs[2];
            posts.getPost().get(id).addComments(username, req);
            //Scrittura sul file Json
            writeOnDBPosts();
        }
        return "Hai commentato il Post:" + id + " con: " + req ;
    }

    private synchronized String ratePostHandler(String[] myArgs) {
        int id = Integer.parseInt(myArgs[1]);
        //Post non presente nel DB
        
        if(!posts.getPost().keySet().contains(id))
            return "Il post che stai cercando di votare non esiste";
        
        //Non puoi votare i tuoi stessi post 
        if( posts.getPost().get(id).getAutore().equals(username))
            return "Non puoi valutare un tuo stesso post";

        //Hai già valutato il post
        if(posts.getPost().get(id).getUpVotes().contains(username) || posts.getPost().get(id).getDownVotes().contains(username))
            return "Hai già valutato questo post";

        if(myArgs[2].equals("+1")){
            posts.getPost().get(id).addUpVotes(username);
            //Scrittura sul file Json
            writeOnDBPosts();
            return "Hai valutato positivamente il post: " + id;
        }
        
        if (myArgs[2].equals("-1") ){
            posts.getPost().get(id).addDownVotes(username);
            //Scrittura sul file Json
            writeOnDBPosts();
            return "Hai valutato negativamente il post: " + id;
        }
        return "Non è stato possibile valutare il Post";
       
    }

    private synchronized String rewinPostHandler(String[] myArgs) {
        int id = Integer.parseInt(myArgs[1]);
        //Post non presente nel DB
        if(!posts.getPost().keySet().contains(id)){
            return "Il post che stai cercando non esiste";
        }
        int idNewPost = posts.createPost(username, posts.getPost().get(id).getTitolo() + " <Rewined by: " + posts.getPost().get(id).getAutore(), posts.getPost().get(id).getContenuto() + ">");
        //Scrittura sul file Json
        writeOnDBPosts();
        return "Hai condiviso il post " + "(" + id + ") "+  "di " + posts.getPost().get(id).getAutore() + " L'ID del nuovo post è: " + idNewPost;
    }

    private synchronized String deletePostHandler(String[] myArgs) {

        int id = Integer.parseInt(myArgs[2]);
        //Post non presente nel DB
        if(!posts.getPost().keySet().contains(id)){
            return "Il post che stai cercando di eliminare non esiste";
        }
        //Puoi rimuovere solo i tuoi post
        if(posts.getPost().get(id).getAutore().equals(username)){
            posts.getPost().remove(id);
            //Scrittura sul file Json
            writeOnDBPosts();
            return "Hai rimosso il post: <" +  id + ">";
        }
        return "Non puoi rimuovere i post degli altri";
    }

    private synchronized String showPostHandler(String[] myArgs) {
        int id = Integer.parseInt(myArgs[2]);
        //Post non presente nel dattabase
        if(!posts.getPost().containsKey(id))
            return "ID non valido";
        
        String result = null;
        result ="IDpost: " + posts.getPost().get(id).getIdPost() + "\n";
        result = result + "Titolo: " + posts.getPost().get(id).getTitolo() + "\n";
        result = result + "Autore: " + posts.getPost().get(id).getAutore() + "\n";
        result = result + "Voti: " + posts.getPost().get(id).getUpVotes().size() + " positivi, " + posts.getPost().get(id).getDownVotes().size() + " negativi" + "\n";
        result = result + "Commenti: " + posts.getPost().get(id).getComments().size();   

        return result;
    }

    private String showFeedHandler() {
        /**
         * Creo un HashMap per capire chi segue l'utente
         */
        ArrayList<String> following = new ArrayList<>(); 
        String result = null;

        synchronized(users){
            for(String i : users.getDB().keySet()){
                if(users.getDB().get(i).contains(username)){
                    if(!following.contains(i)){
                        following.add(i);
                    }
                }
            }
        }
        synchronized(posts){
            for(Integer id : posts.getPost().keySet()){
                if(following.contains(posts.getPost().get(id).getAutore())){
                    result ="IDpost: " + posts.getPost().get(id).getIdPost() + "\n";
                    result = result + "Titolo: " + posts.getPost().get(id).getTitolo() + "\n";
                    result = result + "Autore: " + posts.getPost().get(id).getAutore();    
                }
            }
        }   
        return result;
    }

    private String createPostHandler(String request) {
        String result = null;
        String [] args = request.split("\"");    
        String titolo ;
        String  contenuto;
        
        //Titolo o testo troppo lunghi
        if(args[1].length() > 22)
            return "Il Titolo non deve superare i 20 caratteri";
        if(args[3].length() > 502)
            return "Il Testo non deve superare i 500 caratteri";
        
        titolo = args[1];
        contenuto = args[3];
        synchronized(posts){
            int id = posts.createPost(username,titolo, contenuto);
            //scrittura sul file json
            writeOnDBPosts();
            result = "Nuovo Post creato <ID = " + id +">";
        }
        return result;
    }

    private String viewBlogHandler() {
        String result = null;
        synchronized(posts){
            for(Integer id : posts.getPost().keySet()){
                if(posts.getPost().get(id).getAutore().equals(username)){
                    result = result + "< IDpost: " + posts.getPost().get(id).getIdPost() + "\n";
                    result = result + "< Titolo: " + posts.getPost().get(id).getTitolo() + "\n";
                    result = result + "< Autore: " + posts.getPost().get(id).getAutore() + "\n";
                }
            }
        }
        return result.substring(0,result.length()-1);
    }

    private synchronized String unfollowUserHandler(String[] myArgs) {
        //Utente non presente nel DB
        if(!users.getDB().keySet().contains(myArgs[1]))
            return "Utente non trovato";
        
        //Utente trovato
        if(users.getDB().get(myArgs[1]).contains(username)){
            users.getDB().get(myArgs[1]).remove(username);
            //Scrittura sul file Json
            writeOnDBUsers();
            //Notifica a tutti gli utenti dell'aggiornamento del dtatabase tramite RMI Callbacks
            try {
                notificationService.update(users);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        
            return "Done! Hai smesso di seguire l'Utente <" + myArgs[1] + ">";
        }    
        return "Non stai seguendo l'utente <" + myArgs[1] + ">";
    }

    private synchronized String followUserHandler(String[] myArgs) {
        //Nel DB non esiste l'utente che si sta cercando
        if(!users.getDB().keySet().contains(myArgs[1]))
            return "Utente non trovato";
        //Utente trovato nel DB    
        if(!users.getDB().get(myArgs[1]).contains(username)){
            users.getDB().get(myArgs[1]).add(username);
           //scrittura sul file Json
            writeOnDBUsers();
           //Notifica a tutti gli utenti del database aggiornato
            try{
                System.out.println(users.getDB());
                notificationService.update(users);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        
            return "Done! Stai seguendo l'Utente: <" + myArgs[1] + ">";
        }
       
        return "Già segui <" + myArgs[1] + ">";
    }

    private String listFollowingHandler() {
        String result = "Stai seguendo: \n";
        HashMap<String,ArrayList<String>> following= new HashMap<>();
        synchronized(users){
            for(String i : users.getDB().keySet()){
                for(String j : users.getDB().get(i)){
                    if(!following.keySet().contains(j)){
                        following.put(j,new ArrayList<>());
                        following.get(j).add(i);
                    }else{
                        if(!following.get(j).contains(i))
                            following.get(j).add(i);
                    }
                }
            }
        
            for(String i : following.get(username)){
                result = result  + "\t" + i + "\n";
            }
        }
       result= result.substring(0,result.length()-1);
        return result;
    }

    private String listUserHandler() {
        String result = "\tUtente\t|\tTag";
        result = result + "\n-------------------------------\n";
        ArrayList<String> alreadyChecked = new ArrayList<>();

        synchronized(users){
            for(String i : users.getTags().get(username)){
                for(String j : users.getTags().keySet()){
                    if(users.getTags().get(j).contains(i) && !alreadyChecked.contains(j) && j.equals(username) ){
                        alreadyChecked.add(j);
                        result = result + "\t" + j + "\t|\t" + users.getTags().get(j).toString() +"\n";
                    }
                }
            }
        }
        return result;
    }

    private synchronized String loginHandler(String[] myArgs) {
        
        //DB vuoti quidni utente non inserito
        if(credentials.getCredentialDB().keySet().isEmpty())
            return "You are not registered";
        
        //DB non vouto ma utente non registrato
        if(!credentials.getCredentialDB().keySet().contains(myArgs[1]))
            return "You are not registered";
        
        //DB non vouto e utente registrato
        if(credentials.getCredentialDB().get(myArgs[1]).equals(myArgs[2]) && !login_effettuato){
            login_effettuato = true;
            username = myArgs[1];
            return "< OK";
        }

        if(login_effettuato)    
            return "Hai già effettuato il Login";
        else        
            return "Utente o Password Errati";
    }

    private synchronized void writeOnDBUsers(){
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

    private synchronized void writeOnDBPosts(){
        // Scrittura sul file JSON
    ObjectMapper objectMapper = new ObjectMapper();   
       objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
           try {
           // Writing to a file
               File file= new File(RECOVERY_FILE_PATH + "/" + "postsDB.json");
               objectMapper.writeValue(file, posts);
           } catch (IOException e) {
               e.printStackTrace();
           }
   }
}
