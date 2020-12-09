package com.zxs.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Indexer {

    public static void main(String[] args) throws Exception {
        String indexDir = "H:\\test\\lucene"; //1
        String dataDir = "H:\\test"; //2
        long start = System.currentTimeMillis();
        Indexer indexer = new Indexer(indexDir);
        int numIndexed = indexer.index(dataDir);
        indexer.close();
        long end = System.currentTimeMillis();
        System.out.println("Indexing " + numIndexed + " files took "
                + (end - start) + " milliseconds");
    }
    private IndexWriter writer;
    public Indexer(String indexDir) throws IOException {
        Directory dir =FSDirectory.open(new File(indexDir));
        writer = new IndexWriter(dir, //3
                new StandardAnalyzer(Version.LUCENE_CURRENT), true,
                IndexWriter.MaxFieldLength.UNLIMITED);
    }
    public void close() throws IOException {
        writer.close(); //4
    }
    public int index(String dataDir) throws Exception {
        File[] files = new File(dataDir).listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (!f.isDirectory() &&
                    !f.isHidden() &&
                    f.exists() &&
                    f.canRead() &&
                    acceptFile(f)) {
                indexFile(f);
            }
        }
        return writer.numDocs(); //5
    }
    protected boolean acceptFile(File f) { //6
        return f.getName().endsWith(".txt");
    }
    protected Document getDocument(File f) throws Exception {
        Document doc = new Document();
        doc.add(new Field("contents", new FileReader(f))); //7
        doc.add(new Field("filename", f.getCanonicalPath(), //8
                Field.Store.YES, Field.Index.NOT_ANALYZED));
        return doc;
    }
    private void indexFile(File f) throws Exception {
        System.out.println("Indexing " + f.getCanonicalPath());
        Document doc = getDocument(f);
        if (doc != null) {
            writer.addDocument(doc); //9
        }
    }
}
