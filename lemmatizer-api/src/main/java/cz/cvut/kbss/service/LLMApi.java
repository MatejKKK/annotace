package cz.cvut.kbss.service;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMConfSimpleFactory;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMService;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LLMApi {

    private final LLMService llmService =  new LLMService(LLMConfSimpleFactory.createLLMConf(
        "GROQ", "^", "400", "true", "GROQ_API_KEY,GROQ_API_KEY_2"
    ));

    public List<List<SingleLemmaResult>> lemmatize(String text, String lang) {
        return llmService.process(text, lang).getResult();
    }
}
