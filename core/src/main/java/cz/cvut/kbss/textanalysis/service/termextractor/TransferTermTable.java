package cz.cvut.kbss.textanalysis.service.termextractor;

import cz.cvut.kbss.textanalysis.model.QueryResult;
import cz.cvut.kbss.textanalysis.termextractor.model.Term;
import lombok.Getter;

import java.util.List;
import java.util.Set;

public class TransferTermTable {

    private record TermTransferWithLabel(
            String type,
            long id,
            String label
    ) {}

    @Getter
    private final List<TransferTerm> termTransfers;
    @Getter
    private final Set<Term> terms;
    private long currentTermId = 1;

    public TransferTermTable(List<QueryResult> queryResultList) {
        final List<TermTransferWithLabel> terms = queryResultList.stream()
                .map(it -> new TermTransferWithLabel(it.getType(), ++currentTermId, it.getLabel()))
                .toList();

        this.termTransfers = terms.stream()
                .map(it -> new TransferTerm(it.type(), it.id()))
                .toList();

        this.terms = Set.copyOf(
                terms.stream()
                        .map(it -> new Term(it.label(), it.id()))
                        .toList()
        );
    }
}
