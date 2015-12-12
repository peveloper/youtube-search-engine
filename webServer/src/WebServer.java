import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by peveloper on 11/12/15.
 */
public class WebServer {


    public static void main(String[] args) throws Exception{
        ServerSocketChannel welcomeSocket = ServerSocketChannel.open().bind(new InetSocketAddress(6789));
        System.out.println("Server running ...");

        while(true) {
            SocketChannel connectionSocket = welcomeSocket.accept();
            String clientIp = connectionSocket.getLocalAddress().toString().substring(1, connectionSocket.getLocalAddress().toString().length() - 5);
            System.out.println("New incoming connection from " + clientIp);
        }
    }
}
