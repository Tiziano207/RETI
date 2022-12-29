package Server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class RewardingCalculationTsk implements Runnable {
    private ServerMulticastingService multicastSender; 
    private long UPDATE;
    HashMap<String, Integer> commentiFatti;

    public RewardingCalculationTsk(ServerMulticastingService multicastSender) {
        this.multicastSender = multicastSender;
        this.UPDATE = ServerMainClass.UPDATE;
        this.commentiFatti = new HashMap<>();
    }

    @Override
    public void run() {
        while (true){
        try {
            HashMap<Integer,Post> postsPast = new HashMap<Integer,Post>();
            postsPast = copyDB(ServerMainClass.posts);
            Thread.sleep(UPDATE);
            rewardingCalculation(postsPast);
            writeOnDBWallets();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        multicastSender.send();
    }
    }
/**
 * Metodo che calcola il guadagno prendendo in input la struttura non aggiornata e salvata prima di eseguire la Thread Sleep
 * @param postsPast
 */
    private synchronized void rewardingCalculation(HashMap<Integer,Post> postsPast) {
     //per ogni persona nel DB 
        for( String u : ServerMainClass.users.getTags().keySet()){
            double rewardAutore = 0;
            double rewardCuratore = 0;
            float guadagno = 0;
            try{
            //pero ogni post nel DB
                for(int id : ServerMainClass.posts.getPost().keySet()){
                    ServerMainClass.posts.getPost().get(id).valutato();
                    if(u.equals(ServerMainClass.posts.getPost().get(id).getAutore())){
                        //Calcolo quanti voti in più ho per ogni post
                        int differenceUpvote =  ServerMainClass.posts.getPost().get(id).getUpVotes().size() - postsPast.get(id).getUpVotes().size();
                        int differenceDownvote = ServerMainClass.posts.getPost().get(id).getDownVotes().size() - postsPast.get(id).getDownVotes().size();       
                        //il reward autore è log(max(votiPOsitivi-votiNegativi,0) +1)
                        rewardAutore = Math.log(Math.max(differenceUpvote - differenceDownvote, 0) + 1);
                    }
            
                    //Conto i commenti fatti e se ci sono commenti sotto lo stesso post della stessa persona
                    while(ServerMainClass.posts.getPost().get(id).getComments().iterator().hasNext()){ 
                        if(!commentiFatti.keySet().contains(u))
                            commentiFatti.put(u, 1);            
                        commentiFatti.replace(u, commentiFatti.get(u), commentiFatti.get(u)+1);
                    }
                    //per ogni user conto il rewardCuratore
                    for(String user : commentiFatti.keySet())
                        rewardCuratore =  Math.log(2/(1+Math.pow(Math.E, - (commentiFatti.get(user)))) + 1);
                    //Conto il guadagno totale contanto rewardAutore e rewardCuratore dividendoli per quante volte è stato valutato il post
                    guadagno = guadagno + (float) ((rewardAutore + rewardCuratore)/ServerMainClass.posts.getPost().get(id).getValutazione());
                    //per ogni User vado ad aggiornare il wallet con il guadagno
                    for(Wallet w : ServerMainClass.wallets.getWallets()){
                        if(w.getUser().equals(u)){
                            if(guadagno>0)
                                w.setSaldo (guadagno);
                            else
                                guadagno = 0;
                            writeOnDBWallets();  
                        }
                    }
                }
           
            }catch(NullPointerException e){
                return;
            }   
        }
        return;
    }
    /**
     * Metodo che consente di copiare il contenuto della struttura dati del DB presente nel servee in un'altra, questo metodo serve per calcolare la differenza da i voti positivi e negativi
     * @param oldPostDB
     * @return
     */
    public synchronized HashMap<Integer,Post> copyDB(PostDB oldPostDB) {
        HashMap<Integer,Post> newPostDB = new  HashMap<Integer,Post>();
     
        Set<Entry<Integer, Post>> entries = oldPostDB.getPost().entrySet();
            for (Map.Entry<Integer, Post> mapEntry : entries) {
                newPostDB.put(mapEntry.getKey(), mapEntry.getValue());
            }
        return newPostDB;
    }
    /**
    * Metodo che consente di scrivere sul file JSON i cambiamenti dei Wallt
    */
    private synchronized void writeOnDBWallets(){
        // Scrittura sul file JSON
        ObjectMapper objectMapper = new ObjectMapper();   
       objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
           try {
           // Writing to a file
               File file= new File(ServerMainClass.RECOVERY_FILE_PATH + "/" + "wallets.json");
               objectMapper.writeValue(file, ServerMainClass.wallets);
           } catch (IOException e) {
               e.printStackTrace();
           }
        }
}


