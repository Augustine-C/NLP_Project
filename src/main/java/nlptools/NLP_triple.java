package nlptools;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class NLP_triple {

    public static void main(String[] args) throws Exception {
        // Create the Stanford CoreNLP pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie,ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        String testPathString = NLP_triple.class.getResource("/test.txt").getPath();
        String docString = readFromTxt(testPathString);

        // Annotate an example document.
        Annotation doc = new Annotation(docString);
        pipeline.annotate(doc);

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

        CoreDocument cdoc = new CoreDocument(docString);
        pipeline.annotate(cdoc);
        System.out.println("---");
        System.out.println("entities found");
        for (CoreEntityMention em : cdoc.entityMentions())
            System.out.println("\tdetected entity: \t"+em.text()+"\t"+em.entityType());
        System.out.println("---");
        System.out.println("tokens and ner tags");
        String tokensAndNERTags = cdoc.tokens().stream().map(token -> "("+token.word()+","+token.ner()+")").collect(
                Collectors.joining(" "));
        System.out.println(tokensAndNERTags);

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
