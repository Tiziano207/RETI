package Server;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PostDB implements Serializable{
    private static final long serialVersionUID = 1;
       
    private AtomicInteger idPost;
    private ConcurrentHashMap<Integer,Post> post; 

    public PostDB(){
        super();
        post = new ConcurrentHashMap<>();
        idPost = new AtomicInteger(0);
    }

    public ConcurrentHashMap<Integer,Post> getPost(){
        return post;
    }

    public synchronized int createPost(String autore, String titolo, String contenuto){
        idPost.incrementAndGet();
        Post newPost = new Post(idPost.get(), autore, titolo, contenuto);
        post.put(idPost.get(),newPost);
        return idPost.get();
    }

}
