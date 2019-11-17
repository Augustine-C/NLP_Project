package nlptools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.naturalli.ForwardEntailer;
import edu.stanford.nlp.naturalli.ForwardEntailerSearchProblem;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicWeights;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

public class NLP_start {
    public static void main(String[] args) {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
//        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, depparse, mention, coref, natlog, openie");
//        props.put("annotators", "tokenize, ssplit, pos, lemma, natlog");
//        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, depparse, mention, coref");


        StanfordCoreNLP coreNLP = new StanfordCoreNLP();
//        File foo = new File("foo.txt");
//        Collection<File> files = new ArrayList<File>();
//        files.add(foo);
//        try {
//			coreNLP.processFiles(files);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // read some text in the text variable
        String text = "The team uses supervised learning (SL) to train the network to predict human expert moves in a data set of positions.";
//        String text = "Pick up the blue block.";
//        String text = "In 1921, Einstein received the Nobel Prize for his original work on the photoelectric effect.";
//        String text = "Did Einstein receive the Nobel Prize?";
//        String text = "Mary saw a ring through the window and asked John for it.";
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);


        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);
                System.out.println("word " + word + " ,pos: " + pos + " ,ne: " + ne);
            }

            System.out.println();
//          Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
//          for (RelationTriple t : triples) {
//        	  System.out.println(t);
//        		  System.out.println(t.subjectLemmaGloss() + "\t" + t.relationLemmaGloss() + "\t" + t.objectLemmaGloss());
//          }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeAnnotation.class);
            System.out.println();
            System.out.println(tree);

//          // this is the Stanford dependency graph of the current sentence

//          SemanticGraph tree = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
//          if (tree == null) {
//            tree = sentence.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class);
//          }

//          NaturalLogicAnnotator annotator = new NaturalLogicAnnotator();
//          annotator.annotatePolarity(CoreMap sentence);
//
            SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
            System.out.println(dependencies);
//
//          NaturalLogicWeights weights = new NaturalLogicWeights();
//
//          ForwardEntailer fe = new ForwardEntailer(5, 10, weights);
//
//          ForwardEntailerSearchProblem p = fe.apply(dependencies, true);

//          IndexedWord predicate = dependencies.getFirstRoot();
//          IndexedWord subject = null;
//          String quantifier = null;
//          Boolean goodToGo = false;
//          Boolean TopLevelNegation = false;
//          Boolean PredicateLevelNegation = false;
//          List<Pair<GrammaticalRelation,IndexedWord>> s = dependencies.childPairs(predicate);
//          for (Pair<GrammaticalRelation,IndexedWord> item : s) {
//        	  if (item.first.toString().equals("nsubj")) {
//        		  subject = item.second;
//        	  }
//        	  if (item.first.toString().equals("cop") && (item.second.originalText().toLowerCase().equals("are") || item.second.originalText().toLowerCase().equals("is"))) {
//        		  goodToGo = true;
//        	  }
//        	  if (item.first.toString().equals("neg")) {
//        		  PredicateLevelNegation = true;
//        	  }
//          }
//
//          List<Pair<GrammaticalRelation,IndexedWord>> t = dependencies.childPairs(subject);
//          for (Pair<GrammaticalRelation,IndexedWord> item : t) {
//        	  if (item.first.toString().equals("det")) {
//        		  quantifier = item.second.lemma().toString();
//        	  }
//        	  if (item.first.toString().equals("neg")) {
//        		  TopLevelNegation = true;
//        	  }
//          }
//
//          if (goodToGo) {
//          String output = "";
//          if (TopLevelNegation) output += "not ";
//          if (quantifier != null) output += quantifier + "(x): " + subject.lemma().toString() + "(x) ^ ";
//          if (PredicateLevelNegation) output += "not(";
//          if (quantifier == null) {
//        	  output += predicate.originalText() + "(" + subject.lemma().toString() + ")";
//          } else {
//        	  output += predicate.originalText() + "(x)";
//          }
//          if (PredicateLevelNegation) output += ")";
//
//          System.out.println(output);
//          } else {
//        	  System.out.println("Could not translate sentence.");
//          }

        }

        // This is the coreference link graph
        // Each chain stores a set of mentions that link to each other,
        // along with a method for getting the most representative mention
        // Both sentence and token offsets start at 1!
        Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);
        System.out.println();
        System.out.println(graph);


    }
}
