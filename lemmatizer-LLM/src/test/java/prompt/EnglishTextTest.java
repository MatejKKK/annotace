package prompt;

import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import org.junit.jupiter.api.Test;

import java.util.List;

public class EnglishTextTest extends AbstractTextTest {

    @Test
    @Override
    public void testSimpleText() {
        final LemmatizerResult result = lemmatizer.process("UK is going down.", "en");
        final List<String> lemmas = List.of("UK", "be", "go", "down", ".");
        test(lemmas, result, 95);
    }

    /**
     * @link <a href="https://en.wikipedia.org/wiki/English_language">English</a>
     */
    @Test
    @Override
    public void testOneParagraphText() {
        final LemmatizerResult result =
                lemmatizer.process("""
                        English is either the official language, or one of the official languages, 
                        of 57 sovereign states and 30 dependent territories, making it the most 
                        geographically widespread language in the world. In the United Kingdom, 
                        the United States and Australia, it is the dominant language for historical 
                        reasons without being explicitly defined by law. It is a co‑official language 
                        of the United Nations, the European Union, and many other international and 
                        regional organisations. It has also become the de facto lingua franca of 
                        diplomacy, science, technology, international trade, logistics, tourism, 
                        aviation, entertainment, and the Internet.[7] Ethnologue estimated that there 
                        were over 1.4 billion speakers worldwide as of 2021.
                        """, "en");
        final List<String> lemmas = List.of(
                // Sentence 1
                "English", // English
                "be", // is
                "either", // either
                "the", // the
                "official", // official
                "language", // language
                ",", // ,
                "or", // or
                "one", // one
                "of", // of
                "the", // the
                "official", // official
                "language", // languages
                ",", // ,
                "of", // of
                "57", // 57
                "sovereign", // sovereign
                "state", // states
                "and", // and
                "30", // 30
                "dependent", // dependent
                "territory", // territories
                ",", // ,
                "make", // making
                "it", // it
                "the", // the
                "most", // most
                "geographically", // geographically
                "widespread", // widespread
                "language", // language
                "in", // in
                "the", // the
                "world", // world
                ".", // .

                // Sentence 2
                "In", // In
                "the", // the
                "United", // United
                "Kingdom", // Kingdom
                ",", // ,
                "the", // the
                "United", // United
                "States", // States
                "and", // and
                "Australia", // Australia
                ",", // ,
                "it", // it
                "be", // is
                "the", // the
                "dominant", // dominant
                "language", // language
                "for", // for
                "historical", // historical
                "reason", // reasons
                "without", // without
                "be", // being
                "explicitly", // explicitly
                "define", // defined
                "by", // by
                "law", // law
                ".", // .

                // Sentence 3
                "It", // It
                "be", // is
                "a", // a
                "co-official", // co‑official
                "language", // language
                "of", // of
                "the", // the
                "United", // United
                "Nations", // Nations
                ",", // ,
                "the", // the
                "European", // European
                "Union", // Union
                ",", // ,
                "and", // and
                "many", // many
                "other", // other
                "international", // international
                "and", // and
                "regional", // regional
                "organisation", // organisations
                ".", // .

                // Sentence 4
                "It", // It
                "have", // has
                "also", // also
                "become", // become
                "the", // the
                "de", // de
                "facto", // facto
                "lingua", // lingua
                "franca", // franca
                "of", // of
                "diplomacy", // diplomacy
                ",", // ,
                "science", // science
                ",", // ,
                "technology", // technology
                ",", // ,
                "international", // international
                "trade", // trade
                ",", // ,
                "logistics", // logistics
                ",", // ,
                "tourism", // tourism
                ",", // ,
                "aviation", // aviation
                ",", // ,
                "entertainment", // entertainment
                ",", // ,
                "and", // and
                "the", // the
                "Internet", // Internet
                ".", // .
                "[", // [
                "7", // 7
                "]", // ]

                // Sentence 5
                "Ethnologue", // Ethnologue
                "estimate", // estimated
                "that", // that
                "there", // there
                "be", // were
                "over", // over
                "1.4", // 1.4
                "billion", // billion
                "speaker", // speakers
                "worldwide", // worldwide
                "as", // as
                "of", // of
                "2021", // 2021
                "." // .
        );
        test(lemmas, result, 95);
    }
}