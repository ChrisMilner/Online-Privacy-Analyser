package com.chrisdmilner.webapp;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.*;
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
            MinedPost mp = (MinedPost) post.getValue();
            if (mp.getContent() == null) continue;

            ArrayList<String> keywords = KeywordTextAnalyser.analyse(mp.getContent());
            String conclusionName = "Keyword Shared";
            if (mp.isByUser()) conclusionName = "Keyword Posted";

            for (String kw : keywords) {
                ArrayList<Fact> sources = new ArrayList<>();
                sources.add(post);
                conclusions.add(new Conclusion<>(conclusionName, kw, 1, sources));
            }
        }

        return conclusions;
    }

    protected static String[][] analysePost(String post) {
        String[] clauses = post.split("\\. |\n|\t");

        int count = 0;
        for (int i = 0; i < clauses.length; i++) {
            if (clauses[i].length() < 2)
                clauses[i] = null;
            else count++;
        }

        String[] processedClauses = new String[count];
        int index = 0;
        for (int i = 0; i < clauses.length; i++) {
            if (clauses[i] != null)
                processedClauses[index++] = clauses[i];

            System.out.println("Clause: " + clauses[i]);
        }

        String[][] clauseTriplets = new String[processedClauses.length][3];

        for (int i = 0; i < processedClauses.length; i++) {
            clauseTriplets[i] = getSPOTriplets(processedClauses[i]);
        }

        return clauseTriplets;
    }

    private static String[] getSPOTriplets(String clause) {
        Parse tree = getParseTree(clause);
        tree = tree.getChildren()[0];

        Parse subjectNode = getSubject(tree);
        String subject;
        if (subjectNode == null) subject = "I";
        else subject = subjectNode.getCoveredText();

        Parse predicateNode = getPredicate(tree);
        String predicate;
        if (predicateNode == null) predicate = null;
        else predicate = predicateNode.getCoveredText();

        Parse objectNode = getObject(tree, predicateNode);
        String object;
        if (objectNode == null) object = null;
        else object = objectNode.getCoveredText();

        System.out.println("Subject  Predicate  Object");
        System.out.println(subject + "  " + predicate + "  " + object);

        return new String[] {subject, predicate, object};
    }

    private static Parse getParseTree(String clause) {
        System.out.println("Parsing '" + clause + "'");

        try {
            InputStream is = new FileInputStream(Util.getResourceURI() + "models/en-parser-chunking.bin");
            ParserModel model = new ParserModel(is);

            Parser parser = ParserFactory.create(model);
            Parse topParses[] = ParserTool.parseLine(clause, parser, 1);

            for (Parse p : topParses) {
                p.show();
                System.out.println();
            }

            is.close();

            return topParses[0];
        } catch (FileNotFoundException fe) {
            System.err.println("ERROR Parser Model is Missing.");
            fe.printStackTrace();
        } catch (IOException ioe) {
            System.err.println("ERROR reading the Parser file.");
            ioe.printStackTrace();
        }

        return null;
    }

    private static Parse getSubject(Parse tree) {
        Parse np = breadthFirstParseSearch(tree.getChildren(), "NP", 1, false);

        if (np == null) return null;

        Parse subjectNode = breadthFirstParseSearch(np.getChildren(), "NN(.*)", -1, false);

        return np;
    }

    private static Parse getPredicate(Parse tree) {
        Parse vp = breadthFirstParseSearch(tree.getChildren(), "VP", 1, false);

        Parse predicateNode = breadthFirstParseSearch(vp.getChildren(), "VB(.*)", -1, true);

        return predicateNode;
    }

    private static Parse getObject(Parse tree, Parse predicateNode) {
        Parse[] predicateSiblings = predicateNode.getParent().getChildren();

        Parse match = null;
        for (Parse node : predicateSiblings) {
            if (node.getType().matches("NP|PP")) {
                match = breadthFirstParseSearch(node.getChildren(), "NN(.*)", -1, false);
                if (match != null) return match.getParent();
            }

            if (node.getType().matches("ADJP")) {
                match = breadthFirstParseSearch(node.getChildren(), "JJ(.*)", -1, false);
                if (match != null) return match.getParent();
            }
        }

        return null;
    }

    private static Parse breadthFirstParseSearch(Parse[] tree, String type, int maxDepth, boolean deepest) {
        if (maxDepth == 0) return null;

        Parse match = null;
        int total = 0;
        for (Parse node : tree) {
            if (node.getType().matches(type)) {
                if (!deepest) return node;

                match = node;
            }

            total += node.getChildCount();
        }

        if (total == 0) return match;

        Parse[] nextLayer = new Parse[total];
        int index = 0;
        for (Parse node : tree) {
            for (Parse subnode : node.getChildren()) {
                nextLayer[index++] = subnode;
            }
        }

        Parse lower = breadthFirstParseSearch(nextLayer, type, maxDepth - 1, deepest);
        if (lower == null) return match;
        else return lower;
    }
}
