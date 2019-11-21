package nlptools;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.stream.Collectors;

public class KeywordsTripleMatch {
    public static void main(String[] args) throws Exception {
        // Create the Stanford CoreNLP pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);


        // Get the texts from test.txt
        String testPathString = NLP_triple.class.getResource("/test.txt").getPath();
        String docString = readFromTxt(testPathString);

        // Annotate an example document.
        Annotation doc = new Annotation(docString);
        pipeline.annotate(doc);

        // Get a group of keywords
        KeywordsExtraction ke = new KeywordsExtraction(testPathString,true);
        HashSet<String> keywords = ke.getKeywords(20);

        // Loop over sentences in the document
        for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
            // Get the OpenIE triples for the sentence
            Collection<RelationTriple> triples =
                    sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);

            // Print the triples
            for (RelationTriple triple : triples) {
                System.out.println(triple.confidence + "\t" +
                        triple.subjectLemmaGloss() + "\t" +
                        triple.relationLemmaGloss() + "\t" +
                        triple.objectLemmaGloss());
            }
        }
        Morphology mor = new Morphology();
        for(String keyword :keywords){
            System.out.println(mor.stem(keyword));
        }


    }

    private static String readFromTxt(String path) {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(path));
            StringBuilder content = new StringBuilder();

            String s;
            while ( (s = br.readLine()) != null) {
                content.append(s+' ');
            }
            br.close();
            return content.toString();
        } catch (Exception e) {
            System.out.println(e.toString());
            return "";
        }
    }
}
