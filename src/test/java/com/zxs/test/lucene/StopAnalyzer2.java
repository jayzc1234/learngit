package com.zxs.test.lucene;

import org.apache.lucene.analysis.*;

import java.io.Reader;
import java.util.Set;

public class StopAnalyzer2 extends Analyzer {
    private Set stopWords;
    public StopAnalyzer2() {
        stopWords = StopFilter.makeStopSet( StopAnalyzer.ENGLISH_STOP_WORDS_SET.toArray(new String[0]));
    }
    public StopAnalyzer2(String[] stopWords) {
        this.stopWords = StopFilter.makeStopSet(stopWords);
    }
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new StopFilter(true,new LowerCaseFilter(new LetterTokenizer(reader)),
                stopWords);
    }
}
