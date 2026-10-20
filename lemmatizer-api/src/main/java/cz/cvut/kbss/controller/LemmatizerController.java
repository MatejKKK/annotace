package cz.cvut.kbss.controller;

import cz.cvut.kbss.dto.LemmatizeRequest;
import cz.cvut.kbss.service.LLMApi;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class LemmatizerController {

    private final LLMApi llmApi;

    @Autowired
    public LemmatizerController(LLMApi llmApi) {
        this.llmApi = llmApi;
    }

    @RequestMapping(value = "/lemma", method = POST)
    public List<List<String>>lemmatize(@RequestBody LemmatizeRequest request) {
        final String text = request.text();
        final String lang = request.lang();

        return llmApi.lemmatize(text, lang).stream()
                .map(it -> it.stream().map(SingleLemmaResult::getLemma).toList())
                .toList();
    }

    @RequestMapping(value = "/lemma/{lang}/{text}/", method = GET)
    public List<List<String>> lemmatize(
            @PathVariable String lang,
            @PathVariable String text
    ) {
        return llmApi.lemmatize(text, lang).stream()
                .map(it -> it.stream().map(SingleLemmaResult::getLemma).toList())
                .toList();
    }
}
