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
    private StanfordCoreNLP pipeline;
    private Annotation doc;
    private List<Map.Entry<String, Float>> result;

    public static void main(String[] args) throws Exception {
        System.out.println("!!!" + KeywordsExtraction.class.getResource("/test.txt").getPath());
        KeywordsExtraction k = new KeywordsExtraction(KeywordsExtraction.class.getResource("/test.txt").getPath());
        ArrayList<String> keywords = k.getKeywords(20);
        for(String w : keywords){
            System.out.println(w);
        }
    }

    public KeywordsExtraction(String path) {
        this.commonWords = new HashSet<>();
        loadCommonWords(KeywordsExtraction.class.getResource("/common.txt").getPath());
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie");
        doc = new Annotation(readFromTxt(path));
        pipeline = new StanfordCoreNLP(props);
        pipeline.annotate(doc);
        HashMap<String, Float> values = calculateTF(splitwords(doc));
        result = sortValue(values);
        for(Map.Entry<String, Float> e : result){
            System.out.println(e.getKey() + " " + e.getValue());
        }

    }
    /**
     *return most repeated top n words
     */
    public ArrayList<String> getKeywords(int number){
        ArrayList<String> keywords = new ArrayList<>();
        for(int i  = 0; i < number; i++){
            keywords.add(result.get(i).getKey());
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
        BufferedReader br;
        try {
//            File file = new File(path);
//
//            InputStreamReader reader = new InputStreamReader
//                    (new FileInputStream(file), "gbk");

            br = new BufferedReader(new FileReader(path));
            StringBuilder content = new StringBuilder();

            String s = br.readLine();
            while (s != null) {
                content.append(s);
                System.out.println(s);
                s = br.readLine();
            }
            br.close();
            return content.toString();
        } catch (Exception e) {
            System.out.println(e.toString());
            return "";
        }
    }

    private ArrayList<String> splitwords(Annotation doc){
        ArrayList<String> result = new ArrayList<>();
        for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)){
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)){
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                if(!commonWords.contains(word)){
                    result.add(word);
                    //System.out.println(word);
                }
            }
        }
        return result;
    }

    private HashMap<String, Float> calculateTF(ArrayList<String> words){
        HashMap<String, Integer> wordCount = new HashMap<>();
        HashMap<String, Float> TFValues = new HashMap<>();
        for(String word : words){
            if(wordCount.get(word) == null){
                wordCount.put(word, 1);
            }
            else {
                wordCount.put(word, wordCount.get(word) + 1);
            }
        }

        int wordLen = words.size();
        //traverse the HashMap
        Iterator<Map.Entry<String, Integer>> iter = wordCount.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String, Integer> entry = iter.next();
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

//    private  HashMap<String, Float> calculateIDF(ArrayList<String> words){
//        HashSet<String> uwords = new HashSet<>();
//
//        HashMap<String, Integer> wordPassageNum = new HashMap<String, Integer>();
//        for(String w : words){
//            if(!commonWords.contains(w)) {
//                uwords.add(w);
//            }
//        }
//
//        HashMap<String, Float> wordIDF = new HashMap<String, Float>();
//        Iterator<Map.Entry<String, Integer>> iter_dict = wordPassageNum.entrySet().iterator();
//        while(iter_dict.hasNext())
//        {
//            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>)iter_dict.next();
//            float value = (float)Math.log( 1 / (Float.parseFloat(entry.getValue().toString())) );
//            wordIDF.put(entry.getKey().toString(), value);
//            //System.out.println(entry.getKey().toString() + "=" +value);
//        }
//        return wordIDF;
//    }


}
