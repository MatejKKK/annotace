package cz.cvut.kbss.service;

import cz.cvut.kbss.annotace.lemmatizerllm.configuration.LLMTextConf;
import cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.LLMService;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LLMApi {

    private final LLMService llmService = new LLMService(new LLMTextConf(
            "GROQ", "GROQ_API_KEY,GROQ_API_KEY_2", "^", "400", "true", "false"
    ));

    public List<List<SingleLemmaResult>> lemmatize(String text, String lang) {
        return llmService.process(text, lang).getResult();
    }
}