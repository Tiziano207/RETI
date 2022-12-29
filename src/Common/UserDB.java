package Common;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.io.Serializable;

public class UserDB implements Serializable{
    private static final long serialVersionUID = 1;
    private ConcurrentHashMap<String, ArrayList<String>> users ;
    private ConcurrentHashMap<String, ArrayList<String>> tags ;
    
    public UserDB(){
        users = new ConcurrentHashMap<>();
        tags = new ConcurrentHashMap<>();
    }

    public Set<String> getUsers (){
        return users.keySet();
    }

    public synchronized void register(String username, ArrayList<String> myArgs){
        
        if(!users.containsKey(username)){
            tags.putIfAbsent(username, new ArrayList<String>());
            myArgs.remove("register");
            tags.get(username).addAll(myArgs);
            users.putIfAbsent(username, new ArrayList<String>());    
        }
        else System.out.println("Username already in WINSOME");
    }

    public synchronized void addFollows(String wantToFollow, String username){
        if(!users.get(wantToFollow).contains(username))
            users.get(wantToFollow).add(username);
        else System.out.println("You already follow" + wantToFollow);
    }

    public synchronized ArrayList<String> getUserWithSameTags(String username){
        ArrayList<String> listWithSameTags = new ArrayList<String>();
        ArrayList<String> tagsOfUser = new ArrayList<String>();

        tagsOfUser = tags.get(username);
        for(String tag : tagsOfUser){
            for(String user : tags.keySet()){
                if((tags.get(user).contains(tag)) && (user != username))
                    listWithSameTags.add(username);
            }
        }
        return listWithSameTags;
    }

    public ConcurrentHashMap<String, ArrayList<String>> getDB (){
        return users;
    }

    public ConcurrentHashMap<String, ArrayList<String>> getTags(){
        return tags;
    }
    
    public void copy(UserDB newDB) {
        users = newDB.getDB(); 
    }
}
