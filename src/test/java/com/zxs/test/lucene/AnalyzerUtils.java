package com.zxs.test.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class AnalyzerUtils {

    public static AttributeSource[] tokensFromAnalysis(Analyzer analyzer,
                                                       String text) throws IOException {
        TokenStream stream = //1
                analyzer.tokenStream("contents", new StringReader(text)); //1
        ArrayList tokenList = new ArrayList();
        while (true) {
            if (!stream.incrementToken())
                break;
            tokenList.add(stream.cloneAttributes());
        }
        Object o = tokenList.get(0);
        return (AttributeSource[]) tokenList.toArray(new AttributeSource[0]);
    }
    public static void displayTokens(Analyzer analyzer,
                                     String text) throws IOException {
        AttributeSource[] tokens = tokensFromAnalysis(analyzer, text);
        for (int i = 0; i < tokens.length; i++) {
            AttributeSource token = tokens[i];
            TermAttribute term = (TermAttribute) token.addAttribute(TermAttribute.class);
            System.out.print("[" + term.term() + "] "); //2
        }
    }



    public static void displayTokensWithFullDetails(Analyzer analyzer,
                                                    String text) throws IOException {
        AttributeSource[] tokens = tokensFromAnalysis(analyzer, text);
        int position = 0;
        for (int i = 0; i < tokens.length; i++) {
            AttributeSource token = tokens[i];
            TermAttribute term = (TermAttribute) token.addAttribute(TermAttribute.class);
            PositionIncrementAttribute posIncr = (PositionIncrementAttribute) token.addAttribute(PositionIncrementAttribute.class);
            OffsetAttribute offset = (OffsetAttribute) token.addAttribute(OffsetAttribute.class);
            TypeAttribute type = (TypeAttribute) token.addAttribute(TypeAttribute.class);
            int increment = posIncr.getPositionIncrement();
            if (increment > 0) {
                position = position + increment;
                System.out.println();
                System.out.print(position + ": ");
            }
            System.out.print("[" +
                    term.term() + ":" +
                    offset.startOffset() + "->" +
                    offset.endOffset() + ":" +
                    type.type() + "] ");
        }
        System.out.println();
    }

    public static void main(String[] args) throws IOException {

        displayTokensWithFullDetails(new SimpleAnalyzer(),
                "The quick brown fox 13");
    }
}
