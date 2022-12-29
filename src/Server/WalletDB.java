package Server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class WalletDB implements Serializable{
    private ArrayList<Wallet> wallets;
    private AtomicInteger idWallet;
    
    public WalletDB(){
        this.wallets = new ArrayList<Wallet>();
        this.idWallet = new AtomicInteger(0);
    }

    public synchronized ArrayList<Wallet> getWallets(){
        return wallets;
    }

    public synchronized void createWallet(String username){
        for(Wallet i : wallets){
            if(i.getUser() == username){
                System.out.println("Utente già in possesso di un Wallet");
                return;
            }
        }
        Wallet newWallet = new Wallet(username, idWallet.get());
        wallets.add(newWallet);
        idWallet.incrementAndGet();
    }
}
