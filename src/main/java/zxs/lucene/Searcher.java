package zxs.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;

public class Searcher {
    public static void main(String[] args) throws Exception {
        String indexDir = "H:\\test\\lucene"; //1
        String q ="boy"; //2
        search(indexDir, q);
    }
    public static void search(String indexDir, String q)
            throws Exception {
        Directory dir =  FSDirectory.open(new File(indexDir));
        IndexSearcher is = new IndexSearcher(dir); //3
        QueryParser parser = new QueryParser(Version.LUCENE_CURRENT,"contents", new StandardAnalyzer(Version.LUCENE_CURRENT)); //4
        Query query = parser.parse(q); //4
        long start = System.currentTimeMillis();
        TopDocs hits = is.search(query, 10); //5
        long end = System.currentTimeMillis();
        System.err.println("Found " + hits.totalHits + //6
                " document(s) (in " + (end - start) +
                " milliseconds) that matched query '" +
                q + "':");
        for(int i=0;i<hits.scoreDocs.length;i++) {
            ScoreDoc scoreDoc = hits.scoreDocs[i];
            Document doc = is.doc(scoreDoc.doc); //7
            System.out.println(doc.get("filename")); //8
        }
        is.close(); //9
    }


}
