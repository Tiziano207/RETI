package Server;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;


public class Wallet implements Serializable{
    private String username;
    private HashMap<String,Float> transactionDates;
    private int idWallet;
    private float saldo;
 
    public Wallet(){} 
    
    public Wallet(String username, int idWallet){ 
        this.saldo = 0;
        this.idWallet= idWallet;
        this.username = username;
        this.transactionDates = new HashMap<String,Float>();
    }


    public synchronized void setSaldo(float guadagno){
        Timestamp date = new Timestamp(System.currentTimeMillis());
        transactionDates.put(date.toString(), guadagno);
        saldo = saldo + guadagno;
        return;
    }
  
    private void setUser(String user){
        username = user;
        return;
    }
    
    private void setTransactionDates(HashMap<String,Float> tra){
        transactionDates = tra;
        return;
    }
    
    private void setIDwallet(int id){
        idWallet = id;
        return;
    }

    public HashMap<String,Float> getTransactionDates(){
        return transactionDates;
    }

    public String getUser (){
        return username;
    }

    public int getIDwallet(){
        return idWallet;
    }

    public float getSaldo(){
        return saldo;
    }
}
