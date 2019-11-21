package nlptools;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.Morphology;

import java.io.*;
import java.util.*;




public class KeywordsExtraction {
    private HashSet<String> commonWords;
    private List<Map.Entry<String, Float>> result;
    private ArrayList<String> paratexts;
    private String content;
    private Morphology mor;
    private HashSet<String> entities;

    public static void main(String[] args) throws Exception {
        //System.out.println("!!!" + KeywordsExtraction.class.getResource("/test.txt").getPath());
        KeywordsExtraction k = new KeywordsExtraction(KeywordsExtraction.class.getResource("/DeepBlue.txt").getPath(), true);
        Set<String> keywords = k.getKeywords(false);
        for(String w : keywords){
            System.out.println(w);
        }
    }

    public KeywordsExtraction(String path, boolean useIDF) {
        entities = new HashSet<>();

        mor = new Morphology();
        this.commonWords = new HashSet<>();
        loadCommonWords(KeywordsExtraction.class.getResource("/common.txt").getPath());
        content = readFromTxt(path);
        addEntity(entities);
        String[] words = splitwords(content);
        HashMap<String, Float> tfValues = calculateTF(words);
        HashMap<String, Float> idfValues = calculateIDF(words);
        result = setResult(tfValues, idfValues, useIDF);
//        for(Map.Entry e : result){
//            System.out.println(e.getKey() + " " + e.getValue() + " " + tfValues.get(e.getKey()) + " idf: " + idfValues.get(e.getKey()));
//        }
    }

    private List<Map.Entry<String, Float>> setResult(HashMap<String, Float> tfValues, HashMap<String, Float> idfValues, boolean useIDF) {
        if(useIDF) {
            HashMap<String, Float> result = new HashMap<>();
            for (String w : tfValues.keySet()) {
                result.put(w, tfValues.get(w) *(3 + idfValues.get(w)));
            }

            return sortValue(result);
        } else {
            return sortValue(tfValues);
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
        //System.out.println("---");
        //System.out.println("entities found");
        for (CoreEntityMention em : cdoc.entityMentions()) {
            //result.add(em.text());
//            System.out.println("\tdetected entity: \t" + em.text() + "\t" + em.entityType());
            if(em.entityType().equals("PERSON") || em.entityType().equals("ORGANIZATION") ){
                if(!commonWords.contains(em.text())) {
//                    System.out.println("\tdetected entity: \t" + em.text() + "\t" + em.entityType());
                    result.add(em.text());
                }
            }
        }
    }

    /**
     *return most repeated top numbers of keywords plus special entity names including companies and human names
     */
    public HashSet<String> getKeywords(boolean isSummary){
        HashSet<String> keywords = new HashSet<>();
        int number = 50;
        int i = 0;
        int count = 0;
        if(!isSummary){
            System.out.println(Math.log(result.size()) + " " + result.size());
            number += (Math.log(result.size()) * 10);
        }
        while (count < number){
            String keyword = result.get(i).getKey();
            i++;
            try{
                Integer.parseInt(keyword);
            } catch (NumberFormatException | NullPointerException nfe){
                if(keyword != null){
                    keywords.add(keyword);
                    count++;
                }
            }
        }
        return keywords;
    }

    private void loadCommonWords(String path) {
        try {
            File file = new File(path);
            //InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "gbk");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
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

    private String readFromTxt(String path) {
        paratexts = new ArrayList<>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(path));
            StringBuilder content = new StringBuilder();

            String s = br.readLine();
            while (s != null) {
                //s = s.toLowerCase();
                paratexts.add(s);
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

    private HashSet<String> changetoSet(String[] splitwords) {
        HashSet<String> result = new HashSet<>();
        for(String s : splitwords){
            if(!commonWords.contains(s))
            result.add(s);
        }
        return  result;
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


    private HashMap<String, Float> calculateTF(String[] words){
        HashMap<String, Integer> wordCount = new HashMap<>();
        HashMap<String, Float> TFValues = new HashMap<>();
        for(String word : words){
            if(!commonWords.contains(word)) {
                if (wordCount.get(word) == null) {
                    wordCount.put(word, 1);
                } else {
                    wordCount.put(word, wordCount.get(word) + 1);
                }
            }
        }

        int wordLen = words.length;
        //traverse the HashMap
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            //System.out.println(entry.getKey() + " " + entry.getValue());
            TFValues.put(entry.getKey(), Float.parseFloat(entry.getValue().toString()) / wordLen);
            //System.out.println(entry.getKey().toString() + " = "+  Float.parseFloat(entry.getValue().toString()) / wordLen);
        }
        return TFValues;
    }

    private List<Map.Entry<String, Float>> sortValue(HashMap<String, Float> values){
        List<Map.Entry<String, Float> > result = new ArrayList<>(values.entrySet());
        Collections.sort(result, new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        return result;
    }


    private HashMap<String, Float> calculateIDF(String[] words){
        Set<String> w = changetoSet(words);
        HashMap<String, Integer> map = new HashMap<>();
        HashMap<String, Float> wordIDF = new HashMap<>();
        ArrayList<HashSet<String>> p = new ArrayList<>();
        for(String s : paratexts){
            p.add(changetoSet(splitwords(s)));
        }
        for(String word : w){
            int count = 1;
            for(HashSet<String> paraText : p){
                if(paraText.contains(word)){
                    count++;
                }
            }
            map.put(word, count);
        }

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            float value = (float) Math.log(paratexts.size() / (float)(entry.getValue()));
            wordIDF.put(entry.getKey(), value);
            //System.out.println(entry.getKey().toString() + "=" +value);
        }
        return wordIDF;
    }
}
