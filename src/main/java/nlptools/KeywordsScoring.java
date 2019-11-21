package nlptools;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.Morphology;

import java.io.*;
import java.util.*;

public class KeywordsScoring {
    private HashSet<String> keywords;
    private String content;
    private HashSet<String> commonWords;
    private  HashSet<String> entities;
    private Morphology mor;

    public static void main(String[] args) {
        //System.out.println("!!!" + KeywordsExtraction.class.getResource("/test.txt").getPath());
        KeywordsScoring ks = new KeywordsScoring(KeywordsScoring.class.getResource("/summary.txt").getPath(), KeywordsScoring.class.getResource("/DeepBlue.txt").getPath());
    }

    public KeywordsScoring(String spath, String ppath){
        KeywordsExtraction kw = new KeywordsExtraction(ppath, true);
        keywords = kw.getKeywords(false);
        mor = new Morphology();
        commonWords = new HashSet<>();
        loadCommonWords(KeywordsScoring.class.getResource("/common.txt").getPath());
        content = readFromTxt(spath);
        entities = new HashSet<>();
        addEntity(entities);
        String[] words = splitwords(content);
        calculate(words, kw.result.size());
    }

    private void calculate(String[] words, int totalsize) {
        //HashSet<String> text = changetoSet(words);
        int numberKey = 0;
        int numberOcc = 0;
        for(String w : keywords){
            boolean isOcc = false;
            for(String t : words){
                if(t.equals(w)){
                    isOcc = true;
                    numberOcc++;
                }
            }
            if(isOcc){
                numberKey++;
            }
        }
        float score = (numberKey / (float) keywords.size() * (totalsize / (float) (words.length)));
        System.out.println("score: " + score);
    }

    private HashSet<String> changetoSet(String[] splitwords) {
        HashSet<String> result = new HashSet<>();
        for(String s : splitwords){
            if(!commonWords.contains(s))
                result.add(s);
        }
        return  result;
    }

    private void loadCommonWords(String path) {
        try {
            File file = new File(path);
            //InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "gbk");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            System.out.println("x");
            String w = br.readLine();
            while (w != null) {
                commonWords.add(w);
                w = br.readLine();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            System.exit(0);
        }
    }

    private void addEntity(HashSet<String> result){
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie,ner");
        props.setProperty("ner.applyFineGrained", "false");
        //props.setProperty("ner.model", "PERSON, ORGANIZATION");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument cdoc = new CoreDocument(content);
        pipeline.annotate(cdoc);
        System.out.println("---");
        System.out.println("entities found");
        for (CoreEntityMention em : cdoc.entityMentions()) {
            //result.add(em.text());
            System.out.println("\tdetected entity: \t" + em.text() + "\t" + em.entityType());
            if(em.entityType().equals("PERSON") || em.entityType().equals("ORGANIZATION") ){
                if(!commonWords.contains(em.text())) {
                    System.out.println("\tdetected entity: \t" + em.text() + "\t" + em.entityType());
                    result.add(em.text());
                }
            }
        }
    }

    private String[] splitwords(String s){
        String regex0 = "[\\s\\p{Punct}]{2,}";
        s = s.replaceAll(regex0, ",");
        //System.out.println(cont);
        String regex = "[\"\\s,.?!:•]";

        String[] result = s.split(regex);
        for(int i = 0; i< result.length; i++){
            if(!entities.contains(result[i])){
                result[i] = mor.stem(result[i]);
            }
        }
        return result;
    }


    private String readFromTxt(String path) {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(path));
            StringBuilder content = new StringBuilder();

            String s = br.readLine();
            while (s != null) {
                //s = s.toLowerCase();
                content.append(s);
                s = br.readLine();
            }
            br.close();
            return content.toString();
        } catch (Exception e) {
            System.out.println(e.toString());
            return "";
        }
    }
}
