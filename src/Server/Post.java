package Server;

import java.io.Serializable;
import java.util.ArrayList;

public class Post implements  Serializable{
    private static final long serialVersionUID = 1;
    private int idPost;
    private String autore;
    private String titolo;
    private String contenuto;
    private int valutazione;
    private ArrayList<String> upVotes;
    private ArrayList<String> downVotes;
    private ArrayList<String> comments;

    public Post(int idPost, String autore, String titolo, String contenuto) {
        this.idPost = idPost;
        this.autore = autore;
        this.titolo = titolo;
        this.contenuto = contenuto;
        this.valutazione = 0;
        this.upVotes = new ArrayList<String>();
        this.downVotes = new ArrayList<String>();
        this.comments = new ArrayList<String>();
    }

    public Post(){}
    
    public int getIdPost(){
        return idPost;
    }
    public int getValutazione(){
        return valutazione;
    }
    public void valutato(){
        valutazione++;
        return;
    }
    public String getAutore(){
        return autore;
    }

    public String getTitolo(){
        return titolo;
    }

    public String getContenuto(){
        return contenuto;
    }

    public ArrayList<String> getUpVotes(){
        return upVotes;
    }

    public ArrayList<String> getDownVotes(){
        return downVotes;
    }

    public ArrayList<String> getComments(){
        return comments;
    }

    public void addUpVotes(String user){
        if(!upVotes.contains(user) && !downVotes.contains(user))
            upVotes.add(user);
    } 
    
    public void addDownVotes(String user){
        if(!downVotes.contains(user) && !downVotes.contains(user))
            downVotes.add(user);
    } 
    
    public void removeUpVotes(String user){
        if(upVotes.contains(user))
            upVotes.remove(user);
    }
    
    public void removeDownVotes(String user){
        if(downVotes.contains(user))
            downVotes.remove(user);
    }

    public void addComments(String user, String content){
        comments.add (user + " - "+ content);
    } 
}
