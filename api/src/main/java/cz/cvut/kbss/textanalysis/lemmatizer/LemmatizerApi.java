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
package cz.cvut.kbss.textanalysis.lemmatizer;

import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;

import java.util.List;

public interface LemmatizerApi {

    /**
     * Lemmatizes the given text w.r.t. the given language.
     *
     * @param text text to lemmatize
     * @param lang language to use
     * @return result of the lemmatizations
     * @throws cz.cvut.kbss.textanalysis.exception.UnsupportedLanguageException If the given language is not supported
     */
    LemmatizerResult process(String text, String lang);

    /**
     * Returns a set of languages for which annotation is supported.
     *
     * @return List of languages
     */
    List<String> getSupportedLanguages();


    default boolean processAllTogether(String text) {
        return false;
    }
}
