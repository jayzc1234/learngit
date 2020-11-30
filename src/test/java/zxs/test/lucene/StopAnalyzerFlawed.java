package zxs.test.lucene;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;

import java.io.Reader;
import java.util.Set;

/**
 * Stop words actually not necessarily removed due to filtering order
 */
public class StopAnalyzerFlawed extends Analyzer {
    private Set stopWords;
    public StopAnalyzerFlawed() {
        stopWords =
                StopFilter.makeStopSet(StopAnalyzer.ENGLISH_STOP_WORDS_SET.toArray(new String[0]));
    }
    public StopAnalyzerFlawed(String[] stopWords) {
        this.stopWords = StopFilter.makeStopSet(stopWords);
    }
    /**
     * Ordering mistake here
     */
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new LowerCaseFilter(
                new StopFilter(true,new LetterTokenizer(reader),
                        stopWords));
    }


    public void testStopAnalyzerFlawed() throws Exception {
        AttributeSource[] tokens =
                AnalyzerUtils.tokensFromAnalysis(
                        new StopAnalyzer2(), "The quick brown...");
        TermAttribute termAttr = (TermAttribute)
                tokens[0].addAttribute(TermAttribute.class);
        System.out.println(termAttr.term());
    }

    public static void main(String[] args) throws Exception {
        StopAnalyzerFlawed analyzerFlawed = new StopAnalyzerFlawed();
        analyzerFlawed.testStopAnalyzerFlawed();
    }
}