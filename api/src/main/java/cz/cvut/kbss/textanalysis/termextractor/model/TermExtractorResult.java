package cz.cvut.kbss.textanalysis.termextractor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Getter
public class TermExtractorResult {

    @JsonProperty
    private List<Paragraph> paragraphs;
}
