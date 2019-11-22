/*
Make sure you have the following in your `build.gradle`:

'edu.stanford.nlp:stanford-corenlp:3.7.0',
'edu.stanford.nlp:stanford-corenlp:3.7.0:models@jar',
'edu.stanford.nlp:stanford-corenlp:3.7.0:models-english@jar',
*/
package nlptools;


import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.simple.Document;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class CoreferenceResolver {
    private StanfordCoreNLP pipeline;

    public CoreferenceResolver() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref");
        pipeline = new StanfordCoreNLP(props);
    }

    // replaces pronouns
    // "Barack Obama was born in Hawaii.  He is the president. Obama was elected in 2008. Donald Trump succeeded him. Donald is the current president."
    // becomes
    // "Barack Obama was born in Hawaii. Barack Obama is the president. Barack Obama was elected in 2008. Donald Trump succeeded Barack Obama. Donald Trump is the current president."
    public String resolveCoreferences(String paragraph) {
        Annotation document = new Annotation(paragraph);
        pipeline.annotate(document);

        Map<String, List<Two<String, Integer>>> replacementMap = getReplacementMap(document);
        List<List<String>> sentences = getSentences(paragraph);

        // replace pronouns in 'sentences' using 'replacementMap'
        replacementMap.entrySet()
                .forEach(e -> {
                    String replacement = e.getKey();
                    List<Two<String, Integer>> replacementLocations = e.getValue();

                    replacementLocations.forEach(t -> {
                        String replacementText = t.getLeft();
                        int sentenceIndex = t.getRight() - 1; // it's 1-indexed instead of 0-indexed

                        for (int i = 0; i < sentences.get(sentenceIndex).size(); i++) {
                            if (sentences.get(sentenceIndex).get(i).equals(replacementText)) {
                                sentences.get(sentenceIndex).set(i, replacement);
                            }
                        }
                    });
                });

        return sentences.stream()
                .map(s -> s.stream()
                        .reduce(this::combineSentences)
                        .orElse(""))
                .reduce(this::combineSentences)
                .orElse("");
    }

    // left is the replacement word, right is the sentence index, and we replace with the subject (map key value)
    private Map<String, List<Two<String, Integer>>> getReplacementMap(Annotation document) {
        Map<String, List<Two<String, Integer>>> replacementMap = new HashMap<>();

        document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values().forEach(cc -> {
            String subject = cc.getRepresentativeMention().mentionSpan;
            if (!replacementMap.containsKey(subject)) {
                replacementMap.put(subject, new ArrayList<>());
            }

            cc.getMentionsInTextualOrder()
                    .forEach(m -> {
                        Two<String, Integer> e = new Two(m.mentionSpan, m.sentNum);
                        replacementMap.get(subject).add(e);
                    });
        });

        return replacementMap;
    }

    private String combineSentences(String a, String b) {
        return PTBTokenizer.ptb2Text(a + " " + b); // Use ptb2Text to repair punctuation spacing
    }

    // outer list is sentences, inner list is words
    private static List<List<String>> getSentences(String paragraph) {
        return new Document(paragraph).sentences().stream()
                .map(s -> new ArrayList<>(s.words()))
                .collect(toList());
    }

    private class Two<A, B> {
        private A left;
        private B right;

        public Two(A left, B right) {
            this.left = left;
            this.right = right;
        }

        public A getLeft() {
            return left;
        }

        public B getRight() {
            return right;
        }

        @Override
        public String toString() {
            return "Two{" +
                    "left=" + left +
                    ", right=" + right +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Two<?, ?> aTwo = (Two<?, ?>) o;

            if (left != null ? !left.equals(aTwo.left) : aTwo.left != null) return false;
            return right != null ? right.equals(aTwo.right) : aTwo.right == null;

        }

        @Override
        public int hashCode() {
            int result = left != null ? left.hashCode() : 0;
            result = 31 * result + (right != null ? right.hashCode() : 0);
            return result;
        }
    }
}
