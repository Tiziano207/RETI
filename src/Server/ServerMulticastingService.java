package Server;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ServerMulticastingService {

    MulticastSocket ms;
    int port;
    String group;
    public ServerMulticastingService start(int MCASTPORT, String MULTICAST) {
        port = MCASTPORT;
        group = MULTICAST;
        try{
            ms = new MulticastSocket();
            return this;
        }catch (Exception e){
            e.printStackTrace();
        }
            return this;
    }

    public void end() {
        try{
            ms.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void send(){
        String msg = "[E' stato aggiornato il saldo sui Wallet]";
        //Create a Datagram socket and send 
       
        try {
            DatagramPacket dp;
            dp = new DatagramPacket(msg.getBytes(), msg.length(),InetAddress.getByName(group), port);
            ms.send(dp);
        } catch (Exception e) {
            e.printStackTrace();
        }
       
    }
}
