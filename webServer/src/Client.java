/**
 * Class to Test Client - Server Connection
 */
import java.io.*;
import java.net.*;
public class Client {
    public static void main(String argv[]) throws Exception  {
        String sentence;
        String modifiedSentence;
        BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
        Socket clientSocket = new Socket("localhost", 6789 );
    }
}