import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.javatuples.Quintet;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SniPierZz on 07/12/15.
 */
public class QueryManager {
    private IndexReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    private HashMap<String,ScorePair> map;
    public QueryManager(IndexReader reader, Analyzer analyzer){
        this.reader = reader;
        this.analyzer = analyzer;
        searcher = new IndexSearcher(this.reader);
        this.map = new HashMap<String,ScorePair>();

    }

    private class ScorePair implements Comparable<ScorePair>{
        int count = 0;
        double idf;
        String field;
        String term;

        ScorePair(int docfreq, String field, String term) {
            count++;
            //Standard Lucene idf calculation.  This is calculated once per field:term
            idf = Math.pow((1 + Math.log((reader.numDocs() / ((double) docfreq + 1)))),2);
            this.field = field;
            this.term = term;
        }

        void increment() { count++; }

        double score() {
            return Math.sqrt(count) * idf;
        }

        //Standard Lucene TF/IDF calculation, if I'm not mistaken about it.
        public int compareTo(ScorePair pair) {
            if (this.score() < pair.score()) return -1;
            else return 1;
        }
    }

    public ArrayList<Quintet<String, String, String, String, String>> queryIndex(String query,String topic) throws ParseException, IOException {
        ArrayList<Quintet<String, String, String, String, String>> results = new ArrayList<Quintet<String, String, String, String, String>>();
        EnglishStemmer es = new EnglishStemmer();
        es.setCurrent(query);
        es.stem();
        Query titleQuery = new QueryParser("title", this.analyzer).parse(es.getCurrent());
        titleQuery.setBoost(2.5f);
        Query descriptionQuery = new QueryParser("description", this.analyzer).parse(es.getCurrent());

        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(titleQuery, BooleanClause.Occur.SHOULD);
        booleanQuery.add(descriptionQuery, BooleanClause.Occur.SHOULD);
        if (topic!=null){
            Query topicQuery = new QueryParser("topic", this.analyzer).parse(topic);
            topicQuery.setBoost(0.3f);
            booleanQuery.add(topicQuery, BooleanClause.Occur.MUST);

        }
        TopScoreDocCollector collector = TopScoreDocCollector.create(10000);
        searcher.search(booleanQuery.build(),collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        for(int i=0; i<hits.length;i++){
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            Quintet<String,String,String,String,String> oneMatch = new Quintet<String,String,String,String,String>(d.get("title"),d.get("description"),d.get("url"),d.get("image"),d.get("topic"));
            results.add(oneMatch);
        }
        return results;
    }



    public ArrayList<Quintet<String, String, String, String, String>> queryIndexPseudo(String query, String topic) throws ParseException, IOException {
        EnglishStemmer es = new EnglishStemmer();
        es.setCurrent(query);
        es.stem();
        Query titleQuery = new QueryParser("title", this.analyzer).parse(es.getCurrent());
        titleQuery.setBoost(2.0f);
        Query descriptionQuery = new QueryParser("description", this.analyzer).parse(es.getCurrent());
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(titleQuery, BooleanClause.Occur.SHOULD);
        booleanQuery.add(descriptionQuery, BooleanClause.Occur.SHOULD);
        TopScoreDocCollector collector = TopScoreDocCollector.create(25);
        searcher.search(booleanQuery.build(),collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        for(int i=0; i<hits.length;i++){
            int docId = hits[i].doc;
            String[] fields = {"title","description"};
            for(String fieldName: fields){
                try{
                    TermsEnum terms =  reader.getTermVectors(docId).terms(fieldName).iterator();
                    BytesRef bytesRef = terms.next();
                    while(bytesRef!=null){
                        this.putTermInMap(fieldName,terms.term().toString(), terms.docFreq(),map);
                    }
                }catch (Exception e ){}

            }

        }
        return queryIndex(query, topic);
    }

    public void closeReader() throws IOException {
        this.reader.close();
    }

    void putTermInMap(String field, String term, int freq, Map<String,ScorePair> map) {
        String key = field + ":" + term;
        if (map.containsKey(key))
            map.get(key).increment();
        else
            map.put(key,new ScorePair(freq,field,term));
    }


}
