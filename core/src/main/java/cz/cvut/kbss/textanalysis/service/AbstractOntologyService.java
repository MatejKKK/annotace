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
package cz.cvut.kbss.textanalysis.service;

import cz.cvut.kbss.textanalysis.lemmatizer.LemmatizerApi;
import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import cz.cvut.kbss.textanalysis.model.QueryResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Slf4j
public abstract class AbstractOntologyService implements OntologyService {

    private final LemmatizerApi lemmatizerServiceApi;

    @Autowired
    public AbstractOntologyService(final LemmatizerApi morphoDitaService) {
        this.lemmatizerServiceApi = morphoDitaService;
    }

    /**
     * Reads ontology with the given URI;
     *
     * @param uri logical ontology URI to load
     * @param userName (optional) username to log with
     * @param password (optional) password to log with
     * @return Jena Model containing the ontology
     */
    public abstract Model readOntology(final URI uri, final String userName, final String password);

    public List<QueryResult> analyzeModel(final Set<URI> uriSet, final String username, final String password, final String lang) {
        List<QueryResult> allGraphs = new ArrayList<>();
        List<QueryResult> singleGraph;

        for(URI uri : uriSet) {
            singleGraph = analyzeModel(readOntology(uri, username, password), lang);
            allGraphs.addAll(singleGraph);
        }
        return allGraphs;
    }

    public List<QueryResult> analyzeModel(Model model, String lang) {
        List<QueryResult> queryResultList = new ArrayList<>();
        log.debug("Analyzing ontology model to get all labels");
        String query =
            "SELECT ?s ?p ?o WHERE {"
                + "?s ?p ?o ."
                + "?s a <" + SKOS.Concept.toString() + "> . "
                + "FILTER (lang(?o) = '" + lang + "') ."
                + "FILTER (?p IN (<" + SKOS.prefLabel.toString()+ ">, <" + SKOS.hiddenLabel.toString() + ">, <" + SKOS.altLabel + ">))"
                + "}";

        try (final QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {
            final ResultSet resultSet = queryExecution.execSelect();

            while (resultSet.hasNext()) {
                QuerySolution querySolution = resultSet.nextSolution();
                RDFNode s = querySolution.get("s");
                RDFNode p = querySolution.get("p");
                RDFNode o = querySolution.get("o");
                if (!o.asLiteral().getString().isEmpty()) {
                    QueryResult queryResultobject =
                            new QueryResult(s.asNode().toString(), o.asLiteral().getString(), p.asNode().toString());
                    queryResultList.add(queryResultobject);
                } else {
                    QueryResult queryResultobject =
                            new QueryResult(s.asNode().toString(), "null", "null");
                    queryResultList.add(queryResultobject);
                }
            }
        }
        //printList(queryResultList);
        log.debug("number of retrieved labels is: " + queryResultList.size());
        //Store all labels in one string and call lemmatizer only once then map the queryResult
        // objects to corresponding sub-array
        final StringBuilder sb = new StringBuilder();
        for (QueryResult qr : queryResultList) {
            sb.append(qr.getLabel().trim()).append("\n\n\n\n");
        }
        final String ontologieLabels = sb.toString();

        log.debug("Morphological analysis for ontology labels has started:");
        LemmatizerResult lemmatizerResult =
            lemmatizerServiceApi.process(ontologieLabels, lang);

        if (lemmatizerResult.getResult().size() == queryResultList.size()) {
            int i = 0;
            for (QueryResult queryResult : queryResultList) {
                queryResult.setSingleLemmaResults(lemmatizerResult.getResult().get(i));
                i++;
            }
        }
        else {
            log.warn("Query result list has different size in compare to lemmatized query result list");
            for (List<SingleLemmaResult> singleLemmaResults : lemmatizerResult.getResult()) {
                String fullToken = paragraphObserver(singleLemmaResults);
                int count = 0;
                for (QueryResult queryResult : queryResultList) {
                    if (queryResult.getSingleLemmaResults() != null) {
                        continue;
                    }
                    if (fullToken.contains(queryResult.getLabel().trim())) {
                        ++count;
                    }
                }
                if (count == 0) {
                    continue;
                }
                if (count == 1) {
                    for (QueryResult queryResult : queryResultList) {
                        if (fullToken.contains(queryResult.getLabel().trim())) {
                            queryResult.setSingleLemmaResults(singleLemmaResults);
                            break;
                        }
                    }
                }
                else {
                    //TODO add when having multiple matches
                    for (QueryResult queryResult : queryResultList) {
                        if (fullToken.contains(queryResult.getLabel().trim())) {
                            queryResult.setSingleLemmaResults(singleLemmaResults);
                            break;
                        }
                    }
                }
            }
        }
        log.debug("Morphological analysis for ontology labels has finished");
        return queryResultList;
    }

    private static String paragraphObserver(List<SingleLemmaResult> results) {
        if (results.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size() - 1; ++i) {
            sb.append(results.get(i).getToken().concat(results.get(i).getTrailingSpaces()));
        }
        sb.append(results.getLast().getToken());
        return sb.toString();
    }
}
