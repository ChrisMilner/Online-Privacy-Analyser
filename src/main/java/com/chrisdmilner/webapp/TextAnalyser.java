package com.chrisdmilner.webapp;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class TextAnalyser {

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

            if (mp.isByUser()) conclusions.addAll(analysePost(post));
        }

        return conclusions;
    }

    protected static ArrayList<Conclusion> analysePost(Fact post) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();
        MinedPost mp = (MinedPost) post.getValue();

        System.out.println("      Analysing: " + mp.getContent().split("\n")[0]);
        System.out.println("         " + mp.getLocation());
        System.out.println("         " + mp.getPlace());

        if (mp.getLocation() != null) {
            ArrayList<Fact> sources = new ArrayList<>();
            sources.add(post);
            String coord = mp.getLocation().getLatitude() + ", " + mp.getLocation().getLongitude();
            System.out.println(coord);
            conclusions.add(new Conclusion<>("Location", coord, Analyser.getConfidenceFromSource(post), sources));
        } else if (mp.getPlace() != null) {
            ArrayList<Fact> sources = new ArrayList<>();
            sources.add(post);
            String place = mp.getPlace().getFullName() + ", " + mp.getPlace().getCountry();
            conclusions.add(new Conclusion<>("Location", place, Analyser.getConfidenceFromSource(post), sources));
        }

        return conclusions;

//        String[] clauses = post.split("\\. |\n|\t");
//
//        int count = 0;
//        for (int i = 0; i < clauses.length; i++) {
//            if (clauses[i].length() < 2)
//                clauses[i] = null;
//            else count++;
//        }
//
//        String[] processedClauses = new String[count];
//        int index = 0;
//        for (int i = 0; i < clauses.length; i++) {
//            if (clauses[i] != null)
//                processedClauses[index++] = clauses[i];
//        }
//
//        ArrayList<Conclusion> conclusions = new ArrayList<>();
//
//        for (int i = 0; i < processedClauses.length; i++) {
//            Conclusion result = analyseClause(processedClauses[i], source);
//            if (result != null) conclusions.add(result);
//        }
//
//        return conclusions;
    }

    private static Conclusion analyseClause(String clause, Fact source) {
        ArrayList<Fact> sources = new ArrayList<>();
        sources.add(source);

        String[] spo = getSPOTriplets(clause);

        if (spo[0] == null || spo[1] == null || spo[2] == null)
            return null;

        String sbj = spo[0].toLowerCase();
        if (!sbj.equals("i") && !sbj.equals("we")) {
            if (spo[3].equals("be")) {
                String rebuiltClause = spo[0] + " " + spo[1] + " " + spo[2];

                System.out.println("Conclusion: Belief: " + rebuiltClause);
                return new Conclusion<>("Belief", rebuiltClause, 1, sources);
            } else return null;
        }

        System.out.println("Conclusion: Statement: " + spo[3] + ":" + spo[2]);
        return new Conclusion<>("Statement", spo[3] + ":" + spo[2], 1, sources);
    }

    private static String[] getSPOTriplets(String clause) {
        Parse tree = getParseTree(clause);
        tree = tree.getChildren()[0];

        Parse predicateNode = getPredicate(tree);
        String predicate;
        if (predicateNode == null) predicate = null;
        else predicate = predicateNode.getCoveredText();

        String lemmatized = null;
        if (predicateNode != null)
            lemmatized = lemmatizeVerb(predicate, predicateNode.getType());

        Parse objectNode = null;
        if (predicateNode != null) objectNode = getObject(predicateNode);
        String object;
        if (objectNode == null) object = null;
        else object = objectNode.getCoveredText();


        Parse subjectNode = null;
        if (predicateNode != null) subjectNode = getSubject(predicateNode);
        String subject;
        if (subjectNode == null) subject = "I";
        else subject = subjectNode.getCoveredText();

        System.out.println("Subject | Predicate | Object");
        System.out.println(subject + " | " + predicate + " | " + object + "\n\n");

        return new String[] {subject, predicate, object, lemmatized};
    }

    protected static String lemmatizeVerb(String verb, String tag) {
        try {
            InputStream dictLemmatizer = new FileInputStream(Util.getResourceURI() + "models/en-lemmatizer.dict");
            DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(dictLemmatizer);

            String[] lemmas = lemmatizer.lemmatize(new String[] {verb}, new String[] {tag});
            return lemmas[0];
        } catch (FileNotFoundException fe) {
            System.err.println("ERROR Lemmatizer File Not Found");
            fe.printStackTrace();
        } catch (IOException ioe) {
            System.err.println("ERROR Reading the Lemmatizer file");
            ioe.printStackTrace();
        }

        return null;
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

    private static Parse getSubject(Parse verb) {
        Parse tree = verb.getParent();
        while (!tree.getType().equals("VP")) {
            tree = tree.getParent();
        }
        tree = tree.getParent();

        Parse[] children = tree.getChildren();

        boolean pastVerb = false;
        Parse[] possibleSubjects = null;
        int j = 0;
        for (int i = children.length - 1; i >= 0; i--) {
            if (!pastVerb && children[i].getType().equals("VP")) {
                pastVerb = true;
                if (i == 0) return null;
                possibleSubjects = new Parse[i];
                continue;
            }

            if (pastVerb) {
                possibleSubjects[j++] = children[i];
            }
        }

        Parse np = null;
        for (Parse p : possibleSubjects) {
            System.out.println(p.getType() + " type");
            if (p.getType().matches("NP")) {
                np = p;
                break;
            }
        }

//        Parse np = breadthFirstParseSearch(tree.getChildren(), "NP", 1, false);
//        if (np == null) return null;
//        Parse subjectNode = breadthFirstParseSearch(np.getChildren(), "NN(.*)", -1, false);

        return np;
    }

    private static Parse getPredicate(Parse tree) {
        Parse vp = breadthFirstParseSearch(tree.getChildren(), "VP", 1, false);

        if (vp == null) return null;

        Parse predicateNode = breadthFirstParseSearch(vp.getChildren(), "VB(.*)", -1, true);

        return predicateNode;
    }

    private static Parse getObject(Parse predicateNode) {
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
