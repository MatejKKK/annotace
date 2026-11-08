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
package cz.cvut.kbss.api.service.textanalysis;

import cz.cvut.kbss.textanalysis.dto.TextAnalysisInput;
import cz.cvut.kbss.textanalysis.service.HtmlAnnotationService;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {HtmlAnnotationService.class, FileOntologyService.class})
@Import(ServiceTestConfiguration.class)
@ActiveProfiles("test")
public class AnnotationIntegrationTest {

    @Autowired
    private HtmlAnnotationService sut;

    @Test
    public void testAnnotateQuotedStrings() throws IOException {

        final TextAnalysisInput input = new TextAnalysisInput()
            .setVocabularyRepository(URI.create("https://slovník.gov.cz/generický/testovaci-slovnik"))
            .setLanguage("cs")
            .setContent(Jsoup
                .parse(new File(HtmlAnnotationService.class.getResource("/test-1574.html").getFile()),"utf-8").toString());

        final String s = sut.annotate(true, input);

        final int count = StringUtils.countMatches(s,
            "https://slovník.gov.cz/generický/testovaci-slovnik-0-0/pojem/parcela");

        assertEquals(4, count);
    }

    @ParameterizedTest
    @MethodSource("getIdempotentTexts")
    public void testAnnotateIsIdempotent(final String file) throws IOException {

        final String content = Jsoup
            .parse(new File(HtmlAnnotationService.class.getResource("/" + file).getFile()),"utf-8").toString();

        final TextAnalysisInput input = new TextAnalysisInput()
            .setVocabularyRepository(URI.create("https://slovník.gov.cz/generický/testovaci-slovnik"))
            .setLanguage("cs")
            .setContent(content);

        final String annotatedContent1 = sut.annotate(true, input);

        input.setContent(annotatedContent1);
        final String annotatedContent2 = sut.annotate(true, input);

        // hard to compare directly due to blank nodes
        assertEquals(annotatedContent1.length(), annotatedContent2.length());
    }

    private static Stream<Arguments> getIdempotentTexts() {
        return Stream.of(
            Arguments.of("test-simple.html"),
            Arguments.of("test-simple-2.html")
        );
    }
}
