package zxs.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class LuceneMain1 {
    private static final File INDEX_DIR = new File("H:\\test\\lucene");

    public static void main(String[] args) throws Exception {
        IndexWriter writer = new IndexWriter(FSDirectory.open(INDEX_DIR), new StandardAnalyzer(Version.LUCENE_CURRENT), true, IndexWriter.MaxFieldLength.LIMITED);
        writer.setUseCompoundFile(false);
        indexDocs(writer);//索引两篇文档，一篇包含"school"，另一篇包含"beer"
        writer.commit();//提交两篇文档到索引文件，形成段(Segment) "_0"
        writer.deleteDocuments(new Term("contents", "school"));//删除包含"school"的文档，其实是删除了两篇文档中的一篇。
        writer.commit();//提交删除到索引文件，形成"_0_1.del"
        writer.deleteDocuments(new Term("contents", "beer"));//删除包含"beer"的文档，其实是删除了两篇文档中的另一篇。
        writer.commit();//提交删除到索引文件，形成"_0_2.del"
        indexDocs(writer);//索引两篇文档，和上次的文档相同，但是Lucene无法区分，认为是另外两篇文档。
        writer.commit();//提交两篇文档到索引文件，形成段"_1"
        writer.deleteDocuments(new Term("contents", "beer"));//删除包含"beer"的文档，其中段"_0"已经无可删除，段"_1"被删除一篇。
        writer.close();//提交删除到索引文件，形成"_1_1.del"
    }

    public static void readStruct(IndexWriter writer) throws Exception {

    }

    public static void readStruct2() throws Exception {
        IndexWriter writer = new IndexWriter(FSDirectory.open(INDEX_DIR), new StandardAnalyzer(Version.LUCENE_CURRENT), true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        File f = new File("F:\\work\\doc文档\\账号.txt");
        doc.add(new Field("path", f.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("modified",DateTools.timeToString(f.lastModified(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("contents", new FileReader(f)));
        writer.addDocument(doc);
    }
    public static void indexDocs(IndexWriter writer) throws Exception {
        Document doc = new Document();
        doc.add(new Field("path", "C://", Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("contents", new FileReader(new File("H:\\test\\lucene.txt"))));
        writer.addDocument(doc);
//        writer.optimize();
    }
}
