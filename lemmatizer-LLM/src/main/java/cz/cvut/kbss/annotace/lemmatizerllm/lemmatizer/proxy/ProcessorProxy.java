package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.proxy;

import cz.cvut.kbss.annotace.lemmatizerllm.model.Language;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;

import java.util.List;
import java.util.stream.Stream;

public interface ProcessorProxy extends Runnable {

    public ProcessorProxy setText(String text, String lang, Stream<String> keys);

    public List<List<SingleLemmaResult>> process(String text, String lang);

    public List<List<SingleLemmaResult>> process();

    public void setLanguage(Language language);
    public void setPriority(int priority);
    public int getPriority();
    public void setIndex(int index);
}
