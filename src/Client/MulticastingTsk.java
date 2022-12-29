package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class MulticastingTsk implements Runnable{
    MulticastSocket ms;
    static String MULTICAST;
    public MulticastingTsk(MulticastSocket ms, String MULTICAST) {
        this.ms = ms;
        MulticastingTsk.MULTICAST = MULTICAST;
    }

    @Override
    public void run() {
            registerForMulticast(ms);
            while(ClientMainClass.CLOSE){
                //System.out.println(ClientMainClass.CLOSE);
                waitingForMsg();
            }

            unregisterForMulticast(ms);
    }

    private static void unregisterForMulticast(MulticastSocket ms){
        //leave group 
        ms.disconnect();
        //Closing the Datagram Socket
        ms.close();
    }
    private static void registerForMulticast(MulticastSocket ms) {
        /**
         * Apro una connessione Multicasting sul socket su una porta specificata 
         */
        try{
            ms.joinGroup(new InetSocketAddress(InetAddress.getByName(MULTICAST),12354), NetworkInterface.getByInetAddress(InetAddress.getByName("string")));
            //ms.joinGroup(InetAddress.getByName(MULTICAST));           
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void waitingForMsg(){
        byte[] buf = new byte[1024];
        /**
         * Costruisco un Datagram Packet per ricevere i dati
         */
        DatagramPacket dp = new DatagramPacket(buf, 1024);
        try {
            ms.receive(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str = new String (dp.getData(), 0, dp.getLength());
    
        System.out.print(str + "\n> ");
        
    }
}
