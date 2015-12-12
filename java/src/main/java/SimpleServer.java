import org.apache.lucene.queryparser.classic.ParseException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.javatuples.Quintet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class SimpleServer extends WebSocketServer {
    QueryManager queryManager;
    public SimpleServer(InetSocketAddress address, QueryManager queryManager) {
        super(address);
        this.queryManager = queryManager;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("new connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("received message from " + conn.getRemoteSocketAddress() + ": " + message);
        JSONParser parser=new JSONParser();
        JSONObject mes = new JSONObject();
        try {
            mes = (JSONObject) parser.parse(message);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
            System.out.println("error reading json");
        }
        JSONArray resultsArray = new JSONArray();
        try {
            ArrayList<Quintet<String, String, String, String, String>> results = queryManager.queryIndexPseudo((String) mes.get("query"),(String) mes.get("topic"));
            for(Quintet one:results){
                JSONObject jsonResults = new JSONObject();
                String title = (String) one.getValue0();
                String description = (String) one.getValue1();
                String url = (String) one.getValue2();
                String image = (String) one.getValue3();
                String topic = (String) one.getValue4();
                jsonResults.put("title", title);
                jsonResults.put("text", description);
                jsonResults.put("image", image);
                jsonResults.put("topic", topic);
                jsonResults.put("link", url);
                resultsArray.add(jsonResults);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.send(resultsArray.toJSONString());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("an error occured on connection " + conn.getRemoteSocketAddress()  + ":" + ex);
    }


}
