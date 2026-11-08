package cz.cvut.kbss.textanalysis.termextractor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TermExtractorResult {

    @JsonProperty
    private List<Paragraph> paragraphs;
}
