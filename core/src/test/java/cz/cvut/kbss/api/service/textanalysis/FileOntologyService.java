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

import cz.cvut.kbss.textanalysis.lemmatizer.LemmatizerApi;
import cz.cvut.kbss.textanalysis.service.AbstractOntologyService;
import java.io.InputStream;
import java.net.URI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Import(ServiceTestConfiguration.class)
@Primary
@Profile("test")
public class FileOntologyService extends AbstractOntologyService {

    @Autowired
    public FileOntologyService(
        LemmatizerApi lemmatizerApi) {
        super(lemmatizerApi);
    }

    private InputStream getModelStream(URI uri) {
        final String localName = uri.toString().substring(uri.toString().lastIndexOf("/") + 1,uri.toString().lastIndexOf("?") );
        return getClass().getResourceAsStream("/" + localName + ".ttl");
    }

    public Model readOntology(URI uri, final String userName, final String password) {
        final Model model = ModelFactory.createDefaultModel();
        model.read(getModelStream(uri), null, FileUtils.langTurtle);
        return model;
    }
}
