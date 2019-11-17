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


public class NLP {

    /**
     * @param args
     */
    public static void main( String[] args )
    {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization,
        // NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // read some text in the text variable
        String text = "The team uses supervised learning (SL) to train the network to predict human expert moves in a data set of positions. The team trained a 13-layer policy network from 30 million positions from the data set.";
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        List<String> words = new ArrayList<>();
        List<String> posTags = new ArrayList<>();
        List<String> nerTags = new ArrayList<>();
        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                words.add(word);
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                posTags.add(pos);
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);
                nerTags.add(ne);
            }
        }
        System.out.println(words.toString());
        System.out.println(posTags.toString());
        System.out.println(nerTags.toString());

        System.out.println( "End of Processing" );
    }

}