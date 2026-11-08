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
package cz.cvut.kbss.api.service.textanalysis.html2rdfa;

import static org.mockito.Mockito.lenient;


import cz.cvut.kbss.textanalysis.Constants;
import cz.cvut.kbss.textanalysis.keywordextractor.KeywordExtractorAPI;
import cz.cvut.kbss.textanalysis.lemmatizer.LemmatizerApi;
import cz.cvut.kbss.textanalysis.service.ChunkAnnotationService;
import cz.cvut.kbss.textanalysis.service.HtmlAnnotationService;
import cz.cvut.kbss.api.service.textanalysis.TestChunkFactory;
import java.io.File;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HTMLParserTest {

    @InjectMocks
    private HtmlAnnotationService sut;

    @Mock
    private LemmatizerApi api;

    @Mock
    private KeywordExtractorAPI ker;

    @Mock
    private ChunkAnnotationService chunkAnnotationService;

    @BeforeEach
    public void init() {
        TestChunkFactory.createTestChunks().forEach(
            (cText,cAnnotation) -> lenient().when(chunkAnnotationService.process(cText)).thenReturn(cAnnotation)
        );
    }

    @Test public void testFindChunks() {
        final String[] chunks = TestChunkFactory.createTestChunks().keySet().toArray(new String[] {});
        final String html =
            "<html><body><p>" + chunks[1] + "</p><p>" + chunks[2] + "</p></body></html>";
        final Document doc = Jsoup.parse(html);
        final Document doc2 = sut.annotate(chunkAnnotationService,doc,"cs");
        Assertions.assertEquals(3, doc2.select("span[typeof='" + Constants.NS_TERMIT_PREFIX + ":výskyt-termu']").size());
    }

    @Test public void testParseInvalidHtmlSuccessfully() {
        final String html = "<p>Test chunk 1<div></p>Test chunk 2</div>";
        final Document doc = Jsoup.parse(html);
        sut.annotate(chunkAnnotationService,doc,"cs");
    }

    @Test public void testParseInvalidInputFail() {
        Assertions.assertThrows(Exception.class, () -> sut.annotate(chunkAnnotationService, null, "cs"));
    }

    @Test public void testAnnotateMetropolitanPlanSuccessfully()
        throws IOException {
        sut.annotate(chunkAnnotationService, Jsoup
            .parse(new File(HtmlAnnotationService.class.getResource("/mpp.html").getFile()),
                "utf-8"), "cs");
    }
}
