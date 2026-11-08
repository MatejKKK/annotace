package cz.cvut.kbss.api.controller;

import cz.cvut.kbss.api.dto.LemmatizeRequest;
import cz.cvut.kbss.api.service.LemmatizerService;
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

    private final LemmatizerService lemmatizerService;

    @Autowired
    public LemmatizerController(LemmatizerService lemmatizerService) {
        this.lemmatizerService = lemmatizerService;
    }

    @RequestMapping(value = "/lemma", method = POST)
    public List<List<String>>lemmatize(@RequestBody LemmatizeRequest request) {
        final String text = request.text();
        final String lang = request.lang();

        return lemmatizerService.lemmatize(text, lang).stream()
                .map(
                        its -> its
                                .stream()
                                .map(it -> it.getLemma().concat(it.getTrailingSpaces()))
                                .toList()
                )
                .toList();
    }

    @RequestMapping(value = "/lemma/{lang}/{text}/", method = GET)
    public List<List<String>> lemmatize(
            @PathVariable String lang,
            @PathVariable String text
    ) {
        return lemmatizerService.lemmatize(text, lang).stream()
                .map(
                        its -> its
                                .stream()
                                .map(it -> it.getLemma().concat(it.getTrailingSpaces()))
                                .toList()
                )
                .toList();
    }
}
