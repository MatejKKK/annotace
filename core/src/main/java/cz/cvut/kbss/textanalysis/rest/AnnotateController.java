/*
 * Annotace
 * Copyright (C) 2026 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.textanalysis.rest;

import cz.cvut.kbss.textanalysis.dto.TextAnalysisInput;
import cz.cvut.kbss.textanalysis.service.HtmlAnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AnnotateController {

    private final HtmlAnnotationService service;

    @Autowired
    public AnnotateController(HtmlAnnotationService service) {
        this.service = service;
    }

    @RequestMapping(value = "/annotate", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_XML_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public String annotate(@RequestParam(value = "enableKeywordExtraction", defaultValue = "false")
                           Boolean enableKeywordExtraction,
                           @RequestBody TextAnalysisInput input) {
        return service.annotate(enableKeywordExtraction, input);
    }

    @RequestMapping(value = "/annotate-with-term-extractor", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_XML_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public String annotateWithTermExtractor(
                           @RequestBody TextAnalysisInput input) {
        return service.annotate(input);
    }

    @GetMapping(value = "/languages", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getSupportedLanguages() {
        return service.getSupportedLanguages();
    }
}
