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
package cz.cvut.kbss.annotace.keywordextractor;

import cz.cvut.kbss.annotace.configuration.KerConf;
import cz.cvut.kbss.textanalysis.keywordextractor.KeywordExtractorAPI;
import cz.cvut.kbss.textanalysis.keywordextractor.model.KeywordExtractorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Slf4j
public class KerService implements KeywordExtractorAPI {

    private final RestClient restClient;

    private final KerConf config;

    @Autowired
    public KerService(KerConf config) {
        this.restClient = RestClient.builder().build();
        this.config = config;
    }

    public KeywordExtractorResult process(final String chunks) {

        System.out.println("key-word-extr: " + chunks);

        final String kerUrl = config.getService();

        final File file;
        try {
            file = File.createTempFile("ker-input","");
            Files.writeString(Paths.get(file.toURI()), chunks);

            Resource fileResource = new FileSystemResource(file);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);

            KeywordExtractorResult response = this.restClient.post()
                    .uri(kerUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(KeywordExtractorResult.class);
            log.debug("Keywords: " + response.getKeywords());
            return response;
        } catch (Exception e) {
            log.error("Error computing key phrases.", e);
            return KeywordExtractorResult.createEmpty();
        }
    }
}
