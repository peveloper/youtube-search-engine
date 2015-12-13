import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;


/**
 * Created by SniPierZz on 11/12/15.
 */


public class WebServer extends WebSocketServer {

    public WebServer(InetSocketAddress address){
        super(address);
        System.out.println(address);

    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake ch){
        System.out.println("DIOCANE");
    }
    @Override
    public void onClose(WebSocket ws, int i, String s, boolean b){
        System.out.println("Dioporco");
    }
    @Override
    public void onMessage(WebSocket ws, String s){
        System.out.println(s);
    }

    @Override
    public void onError(WebSocket ws, Exception e){
        System.out.println(e.toString());
    }
}