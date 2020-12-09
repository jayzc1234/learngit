package com.zxs.test.lucene;

import junit.framework.TestCase;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;

public class IndexSearch extends TestCase {
    private FSDirectory directory = FSDirectory.open(new File("H:\\test\\lucene"));

    public IndexSearch() throws IOException {
    }

    private void indexSingleFieldDocs(Field[] fields) throws Exception {
        IndexWriter writer = new IndexWriter(directory,
                new WhitespaceAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
        for (int i = 0; i < fields.length; i++) {
            Document doc = new Document();
            doc.add(fields[i]);
            writer.addDocument(doc);
        }
        writer.optimize();
        writer.close();
    }
    public void testWildcard() throws Exception {
        indexSingleFieldDocs(new Field[]
                { new Field("contents", "wild", Field.Store.YES, Field.Index.ANALYZED),
                        new Field("contents", "child", Field.Store.YES, Field.Index.ANALYZED),
                        new Field("contents", "mild", Field.Store.YES, Field.Index.ANALYZED),
                        new Field("contents", "mildew", Field.Store.YES, Field.Index.ANALYZED) });
        IndexSearcher searcher = new IndexSearcher(directory);
        Query query = new WildcardQuery(new Term("contents", "?ild*")); //#1
        TopDocs matches = searcher.search(query, 10);
        assertEquals("child no match", 3, matches.totalHits);
        assertEquals("score the same", matches.scoreDocs[0].score,
                matches.scoreDocs[1].score, 0.0);
        assertEquals("score the same", matches.scoreDocs[1].score,
                matches.scoreDocs[2].score, 0.0);


        QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, "", new Analyzer() {
            @Override
            public TokenStream tokenStream(String fieldName, Reader reader) {
                return null;
            }
        });
    }


    public void testFuzzy() throws Exception {
//        indexSingleFieldDocs(new Field[] { new Field("contents",
//                "fuzzy",
//                Field.Store.YES,
//                Field.Index.ANALYZED),
//                new Field("contents",
//                        "wuzzy",
//                        Field.Store.YES,
//                        Field.Index.ANALYZED)
//        });
//        IndexSearcher searcher = new IndexSearcher(directory);
//        Query query = new FuzzyQuery(new Term("contents", "wuzza"));
//        TopDocs matches = searcher.search(query, 10);
//        int totalHits = matches.totalHits;
//        assertEquals("both close enough", 4, matches.totalHits);
//        assertTrue("wuzzy closer than fuzzy",
//                matches.scoreDocs[0].score != matches.scoreDocs[1].score);
//        Document doc = searcher.doc(matches.scoreDocs[0].doc);
//        assertEquals("wuzza bear", "wuzzy", doc.get("contents"));


        IndexSearcher searcher = new IndexSearcher(directory);
        QueryParser queryParser = new QueryParser(Version.LUCENE_CURRENT,"contents",new WhitespaceAnalyzer());
        Query parse = queryParser.parse("wuzza~");
        TopDocs matches = searcher.search(parse, 10);
        assertEquals("both close enough", 6, matches.totalHits);
        assertTrue("wuzzy closer than fuzzy",
                matches.scoreDocs[0].score != matches.scoreDocs[1].score);
        Document doc = searcher.doc(matches.scoreDocs[0].doc);
        assertEquals("wuzza bear", "wuzzy", doc.get("contents"));
        Query query = new MatchAllDocsQuery("field");
    }

    public void testGrouping() throws Exception {
        WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();
        Query query = new QueryParser(Version.LUCENE_CURRENT,
                "subject",
                analyzer).parse("(agile OR extreme) AND methodology");
        System.out.println(query);
//        TopDocs matches = searcher.search(query, 10);
//        assertTrue(TestUtil.hitsIncludeTitle(searcher, matches,
//                "Extreme Programming Explained"));
//        assertTrue(TestUtil.hitsIncludeTitle(searcher,
//                matches,
//                "The Pragmatic Programmer"));
    }



    public void testRangeQuery() throws Exception {
        IndexSearcher searcher = new IndexSearcher(directory);
        WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();
        Query query =new QueryParser(Version.LUCENE_CURRENT,
                "subject",
                analyzer).parse(
                "pubmonth:[200401 TO 200412]");
        assertTrue(query instanceof TermRangeQuery);
//        Hits hits = searcher.search(query,10);
//        assertHitsIncludeTitle(hits, "Lucene in Action");
//        query = QueryParser.parse(
//                "{200201 TO 200208}", "pubmonth", analyzer);
//        hits = searcher.search(query);
//        assertEquals("JDwA in 200208", 0, hits.length());


        Query q = new QueryParser(Version.LUCENE_CURRENT,"field", analyzer).parse("\"exact phrase\"");
        assertEquals("zero slop",
                "\"exact phrase\"", q.toString("field"));
        QueryParser qp = new QueryParser(Version.LUCENE_CURRENT,"field", analyzer);
        qp.setPhraseSlop(5);
        q = qp.parse("\"sloppy phrase\"");
        assertEquals("sloppy, implicitly",
                "\"sloppy phrase\"~5", q.toString("field"));


        Query q1 = new QueryParser(Version.LUCENE_CURRENT,"field", analyzer).parse("PrefixQuery*");
        assertEquals("lowercased",
                "prefixquery*", q.toString("field"));
        QueryParser qp1 = new QueryParser(Version.LUCENE_CURRENT,"field", analyzer);
        qp.setLowercaseExpandedTerms(false);
        q = qp.parse("PrefixQuery*");
        assertEquals("not lowercased",
                "PrefixQuery*", q.toString("field"));

    }

}
