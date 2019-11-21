package nlptools;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.util.*;




public class KeywordsExtraction {
    private HashSet<String> commonWords;
    private List<Map.Entry<String, Float>> result;
    private ArrayList<Set<String>> paratexts;

    public static void main(String[] args) throws Exception {
        System.out.println("!!!" + KeywordsExtraction.class.getResource("/test.txt").getPath());
        KeywordsExtraction k = new KeywordsExtraction(KeywordsExtraction.class.getResource("/test.txt").getPath(), true);
        ArrayList<String> keywords = k.getKeywords(20);
        for(String w : keywords){
            System.out.println(w);
        }
    }

    public KeywordsExtraction(String path, boolean useIDF) {
        this.commonWords = new HashSet<>();
        loadCommonWords(KeywordsExtraction.class.getResource("/common.txt").getPath());
        String[] words = splitwords(readFromTxt(path));
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
                result.put(w, tfValues.get(w) * idfValues.get(w));
            }
            return sortValue(result);
        } else {
            return sortValue(tfValues);
        }
    }

    /**
     *return most repeated top n words
     */
    public ArrayList<String> getKeywords(int number){
        ArrayList<String> keywords = new ArrayList<>();
        int i = 0;
        int count = 0;
        while (count < number){
            String keyword = result.get(i).getKey();
            i++;
            try{
                Integer.parseInt(keyword);

            } catch (NumberFormatException | NullPointerException nfe){
                keywords.add(keyword);
                count++;
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
                s = s.toLowerCase();
                paratexts.add(changetoSet(splitwords(s)));
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

    private Set<String> changetoSet(String[] splitwords) {
        Set<String> result = new HashSet<>();
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
        String regex = "[\"\\s,.?!]";
        return s.split(regex);
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
        for(String word : w){
            int count = 0;
            for(Set<String> paraText : paratexts){
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
