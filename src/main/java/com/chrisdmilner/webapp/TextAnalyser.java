package com.chrisdmilner.webapp;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class TextAnalyser {
    public static void main(String[] args) {
        FactBook f = new FactBook();

        f.addFact(new Fact<>("Posted", "I love watching rugby!! England isn't the best :))", null));

        ArrayList<Conclusion> conclusions = analyse(f);

        System.out.println(conclusions.toString());
    }

    public static ArrayList<Conclusion> analyse(FactBook f) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();

        ArrayList<Fact> posts = f.getFactsWithName("Posted");
        for (Fact post : posts) {
            conclusions.addAll(analysePost(post));
        }

        return conclusions;
    }

    private static ArrayList<Conclusion> analysePost(Fact post) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();

        String content = (String) post.getValue();

        try {
            // Tokenise the content
//            String[] tokens = new String[] {"I","love","watching","rugby","!","!","England","is","the","best","."};
            InputStream tokenModelIn = new FileInputStream(Util.getResourceURI()+"models/en-token.bin");
            TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
            Tokenizer tokenizer = new TokenizerME(tokenModel);
            String tokens[] = tokenizer.tokenize(content);

            // Tag the tokens
            InputStream posModelIn = new FileInputStream(Util.getResourceURI()+"models/en-pos-maxent.bin");
            POSModel posModel = new POSModel(posModelIn);
            POSTaggerME posTagger = new POSTaggerME(posModel);
            String tags[] = posTagger.tag(tokens);

            // Chunk the content
            InputStream ins = new FileInputStream(Util.getResourceURI()+"models/en-chunker.bin");
            ChunkerModel chunkerModel = new ChunkerModel(ins);
            ChunkerME chunker = new ChunkerME(chunkerModel);
            String[] chunks = chunker.chunk(tokens,tags);

            for (int i = 0; i < tokens.length; i++) {
                System.out.println(tokens[i] + " - " + tags[i] + " - " + chunks[i]);
            }

        } catch (UnsupportedEncodingException e) {
            System.err.println("ERROR converting post string to input stream.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("ERROR reading post input stream");
            e.printStackTrace();
        }

        return conclusions;
    }
}
