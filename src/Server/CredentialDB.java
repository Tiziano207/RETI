package Server;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class CredentialDB implements Serializable{
    private ConcurrentHashMap<String,String> credential;

    public CredentialDB(){
        credential = new ConcurrentHashMap<String,String>();
    }

    public ConcurrentHashMap<String, String> getCredentialDB(){
        return credential;
    }

    public void register(String usr, String psw){
        credential.putIfAbsent(usr, psw);
        return;
    }
}
