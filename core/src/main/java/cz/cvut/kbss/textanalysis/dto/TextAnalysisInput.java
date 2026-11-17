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
package cz.cvut.kbss.textanalysis.dto;

import java.net.URI;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents input passed to the label analysis service.
 * <p>
 * Mainly contains the content to analyze and identification of the vocabulary whose terms will be used in the label
 * analysis.
 */
@Getter
@Setter
@Accessors(chain = true)
public class TextAnalysisInput {

    /**
     * Text content to analyze.
     */
    private String content;

    /**
     * URI of the repository containing vocabulary whose terms are used in the label analysis.
     */
    private URI vocabularyRepository;

    /**
     * Username to access the repository
     */
    private String vocabularyRepositoryUserName;

    /**
     * Password to access the repository
     */
    private String vocabularyRepositoryPassword;

    /**
     * URI of the context containing vocabulary whose terms are used in the label analysis. Optional.
     * <p>
     * If not specified, the whole {@link #vocabularyRepository} is searched for terms.
     */
    private Set<URI> vocabularyContexts;

    /**
     * Language for annotation.
     */
    private String language;
}
