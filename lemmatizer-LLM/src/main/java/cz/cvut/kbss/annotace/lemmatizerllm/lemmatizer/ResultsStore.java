package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer;

import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ResultsStore {
    @Getter
    private static final List<List<SingleLemmaResult>> results = new ArrayList<>();

    private static final AtomicInteger currentIndex = new AtomicInteger(0);

    public static boolean addResults(List<List<SingleLemmaResult>> threadResults, int index) {
        if (index > currentIndex.get()) return false;
        currentIndex.set(index + 1);
        synchronized (results) {
            results.addAll(threadResults);
        }
        return true;
    }
}
