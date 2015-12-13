import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.IOException;

/**
 * Created by SniPierZz on 07/12/15.
 */
public class Indexer {
    private StandardAnalyzer analyzer;
    private Directory index;
    private IndexWriterConfig config;
    private IndexWriter writer;

    public Indexer(StandardAnalyzer analyzer, Directory index) throws IOException {
        this.index = index;
        this.analyzer = analyzer;
        this.config = new IndexWriterConfig(this.analyzer);
        this.writer = new IndexWriter(this.index,this.config);
    }

    public Directory getIndex(){
        return this.index;
    }
    public void addLine(String title, String description, String url, String image, String topic) throws IOException {
        EnglishStemmer es = new EnglishStemmer();
        Document doc = new Document();
        es.setCurrent(title);
        es.stem();
        doc.add(new TextField("title", es.getCurrent(), Field.Store.YES));
        es.setCurrent(description);
        es.stem();
        doc.add(new TextField("description", es.getCurrent(), Field.Store.YES));
        doc.add(new StringField("url", url, Field.Store.YES));
        doc.add(new StringField("image", image, Field.Store.YES));
        doc.add(new StringField("topic", topic, Field.Store.YES));
        this.writer.addDocument(doc);
    }
    public void closeWriter() throws IOException {
        this.writer.close();
    }


}
