import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.bson.Document;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;

/**
 * Created by peveloper on 20/11/15.
 */


public class Main {

    public static void main(String [] args) throws IOException, URISyntaxException {
        final StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = new RAMDirectory();
        final Indexer indexer;
        indexer = new Indexer(analyzer,index);
        MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
        MongoDatabase db = mongoClient.getDatabase("youtube");
        FindIterable<Document> iterable = db.getCollection("youtube").find();
        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(final org.bson.Document document) {
                String url = (String) document.get("videoId");
                String title = (String) document.get("title");
                String description = (String) document.get("description");
                String image = (String) document.get("thumbnail");
                String topic = (String) document.get("topic");
                try {
                    indexer.addLine(title,description,url,image,topic);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        indexer.closeWriter();

        QueryManager queryManager = new QueryManager(DirectoryReader.open(index),analyzer);
        String host = "localhost";
        int port = 8887;

        WebSocketServer server = new SimpleServer(new InetSocketAddress(host, port), queryManager);
        server.run();

    }
}
