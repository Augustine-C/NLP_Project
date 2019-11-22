package nlptools;

import java.util.*;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;

public class CorefExample {
    public static void main(String[] args) throws Exception {
        String docString = "Barack Obama was born in Hawaii. He is the president. Obama was elected in 2008.";
        CoreferenceResolver cr = new CoreferenceResolver();
        System.out.println(cr.resolveCoreferences(docString));
    }
}
