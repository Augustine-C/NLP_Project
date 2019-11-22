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
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

public class KeywordsTripleMatch {
    public static void main(String[] args) throws Exception {
        // Create the Stanford CoreNLP pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);


        CoreferenceResolver cr = new CoreferenceResolver();

        // Get the texts from Article
        String articlePathString = NLP_triple.class.getResource("/DeepBlue.txt").getPath();
        String articleString = readFromTxt(articlePathString);
        //articleString = cr.resolveCoreferences(articleString);
        HashMap<String, ArrayList<RelationTriple>> articlehm = parseTriple(pipeline, articleString);

        // Get the texts from Summary
        String summaryPathString = NLP_triple.class.getResource("/summary.txt").getPath();
        String summaryString = readFromTxt(summaryPathString);
        summaryString = cr.resolveCoreferences(summaryString);
        HashMap<String, ArrayList<RelationTriple>> summaryhm = parseTriple(pipeline, summaryString);

        // Get a group of keywords
        KeywordsExtraction ke = new KeywordsExtraction(articlePathString,true);
        HashSet<String> keywords = ke.getKeywords(20);

        // Print the triples

        for(Map.Entry<String, ArrayList<RelationTriple>> entry: summaryhm.entrySet()){
            if( articlehm.containsKey(entry.getKey())){
                System.out.println("Summary:");
                for (RelationTriple triple : entry.getValue()) {
                    System.out.println(triple.confidence + "\t" +
                            triple.subjectLemmaGloss() + "\t" +
                            triple.relationLemmaGloss() + "\t" +
                            triple.objectLemmaGloss());
                }
                System.out.println("Article:");
                for (RelationTriple triple : articlehm.get(entry.getKey())) {
                    System.out.println(triple.confidence + "\t" +
                            triple.subjectLemmaGloss() + "\t" +
                            triple.relationLemmaGloss() + "\t" +
                            triple.objectLemmaGloss());
                }
            }
        }



    }

    private static HashMap<String,ArrayList<RelationTriple>> parseTriple(StanfordCoreNLP pipeline, String docString){
        // Annotate an example document.
        Annotation doc = new Annotation(docString);
        pipeline.annotate(doc);

        HashSet<RelationTriple> allTriples = new HashSet<>();
        HashMap<String,ArrayList<RelationTriple>> subTrip = new HashMap<>();
        // Loop over sentences in the document
        for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
            // Get the OpenIE triples for the sentence
            Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
            boolean isSucc = allTriples.addAll(triples);
            //System.out.println(isSucc);
            for (RelationTriple triple: triples){
                if(subTrip.containsKey(triple.subjectLemmaGloss())) {
                    subTrip.get(triple.subjectLemmaGloss()).add(triple);
                }else{
                    ArrayList<RelationTriple> arr = new ArrayList<>();
                    arr.add(triple);
                    subTrip.put(triple.subjectLemmaGloss(),arr);
                }
            }
        }
        return  subTrip;
    }


    private static String readFromTxt(String pathString) {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(pathString));
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
