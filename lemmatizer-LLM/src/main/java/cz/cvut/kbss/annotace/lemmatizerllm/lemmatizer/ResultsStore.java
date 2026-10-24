package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer;

import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ResultsStore {
    private static final List<List<SingleLemmaResult>> results = new ArrayList<>();

    private static final AtomicInteger currentIndex = new AtomicInteger(0);

    private static final AtomicBoolean lastSuccess = new AtomicBoolean(false);

    public static int addResults(List<List<SingleLemmaResult>> threadResults, int index) {
        synchronized (currentIndex) {
            if (index > currentIndex.get()) {
                lastSuccess.set(false);
                return index - currentIndex.get();
            }
            currentIndex.set(index + 1);
        }
        lastSuccess.set(true);
        synchronized (results) {
            results.addAll(threadResults);
        }
        return 0;
    }

    static List<List<SingleLemmaResult>> getResults() {
        System.out.println("STORE: " + lastSuccess.get());
        synchronized (results) {
            return results;
        }
    }
}
