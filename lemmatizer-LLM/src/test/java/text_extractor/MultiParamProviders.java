package text_extractor;

import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static text_extractor.Helper.ALL_KEPT;
import static text_extractor.Helper.BREAK;

class MultiParamProviders {

    private final static String ONE_SPACE = " ";
    private final static String TWO_SPACES = "  ";
    private final static String THREE_SPACES = "   ";
    private final static String FOUR_SPACES = "    ";
    private final static String FIVE_SPACES = "     ";
    private final static String SIX_SPACES = "      ";
    private final static String SEVEN_SPACES = "       ";
    private final static String EIGHT_SPACES = "        ";

    private static SingleLemmaResult simpleFactory(String token) {
        SingleLemmaResult result = new SingleLemmaResult();
        result.setToken(token);
        result.setLemma("");
        result.setNegated(false);
        return result;
    }

    private static SingleLemmaResult simpleFactory(String token, String leadingSpaces, String trailingSpaces) {
        SingleLemmaResult result = new SingleLemmaResult();
        result.setToken(token);
        result.setLeadingSpaces(leadingSpaces);
        result.setTrailingSpaces(trailingSpaces);
        result.setLemma("");
        result.setNegated(false);
        return result;
    }

    static Stream<Arguments> multiParamProviderSpaces() {
        return Stream.of(
                arguments(
                        "slovo",
                        List.of(simpleFactory("slovo")),
                        List.of(simpleFactory("slovo", "", ""))
                ),
                arguments(
                        ONE_SPACE + "slovo" + ONE_SPACE,
                        List.of(simpleFactory("slovo")),
                        List.of(simpleFactory("slovo", ONE_SPACE, ONE_SPACE))
                ),
                arguments(
                        "slovo" + TWO_SPACES,
                        List.of(simpleFactory("slovo")),
                        List.of(simpleFactory("slovo", "", TWO_SPACES))
                ),
                arguments(
                        TWO_SPACES + "slovo",
                        List.of(simpleFactory("slovo")),
                        List.of(simpleFactory("slovo", TWO_SPACES, ""))
                ),
                arguments("Starý" + ONE_SPACE +
                                "člověk" + TWO_SPACES +
                                "chodí" + THREE_SPACES +
                                "do" + FOUR_SPACES +
                                "kostela" + FIVE_SPACES +
                                "častěji" + SIX_SPACES +
                                "než" + SEVEN_SPACES +
                                "mladší" + EIGHT_SPACES +
                                "lidé",
                        List.of(
                                simpleFactory("Starý"),
                                simpleFactory("člověk"),
                                simpleFactory("chodí"),
                                simpleFactory("do"),
                                simpleFactory("kostela"),
                                simpleFactory("častěji"),
                                simpleFactory("než"),
                                simpleFactory("mladší"),
                                simpleFactory("lidé")
                        ),
                        List.of(
                                simpleFactory("Starý", "", ONE_SPACE),
                                simpleFactory("člověk", ONE_SPACE, TWO_SPACES),
                                simpleFactory("chodí", TWO_SPACES, THREE_SPACES),
                                simpleFactory("do", THREE_SPACES, FOUR_SPACES),
                                simpleFactory("kostela", FOUR_SPACES, FIVE_SPACES),
                                simpleFactory("častěji", FIVE_SPACES, SIX_SPACES),
                                simpleFactory("než", SIX_SPACES, SEVEN_SPACES),
                                simpleFactory("mladší", SEVEN_SPACES, EIGHT_SPACES),
                                simpleFactory("lidé", EIGHT_SPACES, "")
                        )
                ),
                arguments("AA" + ONE_SPACE +
                                "AA" + TWO_SPACES +
                                "AA" + THREE_SPACES +
                                "BB" + FOUR_SPACES +
                                "BB" + FIVE_SPACES +
                                "AA" + SIX_SPACES +
                                "BB" + SEVEN_SPACES +
                                "BB" + EIGHT_SPACES +
                                "BB",
                        List.of(
                                simpleFactory("AA"),
                                simpleFactory("AA"),
                                simpleFactory("AA"),
                                simpleFactory("BB"),
                                simpleFactory("BB"),
                                simpleFactory("AA"),
                                simpleFactory("BB"),
                                simpleFactory("BB"),
                                simpleFactory("BB")
                        ),
                        List.of(
                                simpleFactory("AA", "", ONE_SPACE),
                                simpleFactory("AA", ONE_SPACE, TWO_SPACES),
                                simpleFactory("AA", TWO_SPACES, THREE_SPACES),
                                simpleFactory("BB", THREE_SPACES, FOUR_SPACES),
                                simpleFactory("BB", FOUR_SPACES, FIVE_SPACES),
                                simpleFactory("AA", FIVE_SPACES, SIX_SPACES),
                                simpleFactory("BB", SIX_SPACES, SEVEN_SPACES),
                                simpleFactory("BB", SEVEN_SPACES, EIGHT_SPACES),
                                simpleFactory("BB", EIGHT_SPACES, "")
                        )
                )
        );
    }



    static Stream<Arguments> multiParamProviderParagraphExtractor() {
        return Stream.of(
                arguments("""
                        Cílem knihy 200 | Město | Fyzické vystavěné prostředí je popis fyzického vystavěného prostředí hl. m. Prahy jako sídla v krajině. Kniha řeší
                        stavby a prostor, který je obklopuje – popisuje a analyzuje „scénu“ pro lidské činnosti, kterými se naopak zabývá kniha 300 | Využití území.
                        Dále také navazuje a částečně se prolíná s knihou 100 | Krajina, a to zejména v tématech, u kterých nelze jednoznačně oddělit prostředí vystavěné
                        od krajinného. Čtenář knihy 200 získá informace o vývoji fyzického vystavěného prostředí, o jeho hodnotách, a dále o charakteristice prostorového
                        uspořádání města a také vztazích a vzorcích vznikajících v městském prostoru. Témata uvedená v knize jsou zpravidla řešena v rozsahu lokalit
                        městské krajiny, a to zejména lokalit vystavěného prostředí. U některých témat je řešené území rozšířeno, aby nedocházelo k umělému rozdělování
                        fenoménů. Kniha začíná úvodní kapitolou 1, která kromě celkového úvodu a návodu na orientaci v dokumentu obsahuje témata souvislostí mezi městem
                        a krajinou a souvisejících strategických dokumentů. Kniha pokračuje kapitolou 2 Vývoj vystavěného prostředí, která popisuje historii města a
                        hodnoty v ní založené. V podkapitole 2.1 popisuje historické městské prostředí – jak se utvářelo a jakými etapami v Praze prošlo. Navazuje popis
                        hodnot města jako souboru nemovitých statků kulturního dědictví. Druhá obsahová kapitola 3 se zabývá současným prostorovým uspořádáním města.
                        Začíná uvedením současného pojetí územního plánování hlavního města pomocí strukturálně zaměřeného přístupu, postaveného na převažujícím
                        charakteru lokalit (🡪 3.1). Dále jsou řešeny morfologické charakteristiky zástavby (🡪 3.2). Následující podkapitola 3.3 prezentuje veřejná
                        prostranství pomocí analýzy veřejné přístupnosti, uličních prostranství a jejich morfologie. Popis prostorového uspořádání města je završen
                        kompozicí a vizuálními podmínkami (🡪 3.4), které se zaobírají tématy historických vedut, významných pohledů na město, a stavebních dominant.
                        Významné pohledy na město byly nově pro ÚAP 2024 revidovány a vznikla nová podoba aplikace, kde je možné je prohlížet.
                        """, 1, 1, 100),
                arguments("Short text", 1, 1, 100),
                arguments("""
                        AAA
                        
                        
                        
                        BBB
                        
                        
                        
                        CCC
                        """, 3, 1, 50),
                arguments("""
                        AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                        
                        
                        
                        BB
                        """, 2, 2, 5)
        );
    }


    static Stream<Arguments> multiParamProviderSentenceExtractor() {
        return Stream.of(
                arguments("""
                        Cílem knihy 200 | Město | Fyzické vystavěné prostředí je popis fyzického vystavěného prostředí hl. m. Prahy jako sídla v krajině. Kniha řeší
                        stavby a prostor, který je obklopuje – popisuje a analyzuje „scénu“ pro lidské činnosti, kterými se naopak zabývá kniha 300 | Využití území.
                        Dále také navazuje a částečně se prolíná s knihou 100 | Krajina, a to zejména v tématech, u kterých nelze jednoznačně oddělit prostředí vystavěné
                        od krajinného. Čtenář knihy 200 získá informace o vývoji fyzického vystavěného prostředí, o jeho hodnotách, a dále o charakteristice prostorového
                        uspořádání města a také vztazích a vzorcích vznikajících v městském prostoru. Témata uvedená v knize jsou zpravidla řešena v rozsahu lokalit
                        městské krajiny, a to zejména lokalit vystavěného prostředí. U některých témat je řešené území rozšířeno, aby nedocházelo k umělému rozdělování
                        fenoménů. Kniha začíná úvodní kapitolou 1, která kromě celkového úvodu a návodu na orientaci v dokumentu obsahuje témata souvislostí mezi městem
                        a krajinou a souvisejících strategických dokumentů. Kniha pokračuje kapitolou 2 Vývoj vystavěného prostředí, která popisuje historii města a
                        hodnoty v ní založené. V podkapitole 2.1 popisuje historické městské prostředí – jak se utvářelo a jakými etapami v Praze prošlo. Navazuje popis
                        hodnot města jako souboru nemovitých statků kulturního dědictví. Druhá obsahová kapitola 3 se zabývá současným prostorovým uspořádáním města.
                        Začíná uvedením současného pojetí územního plánování hlavního města pomocí strukturálně zaměřeného přístupu, postaveného na převažujícím
                        charakteru lokalit (🡪 3.1). Dále jsou řešeny morfologické charakteristiky zástavby (🡪 3.2). Následující podkapitola 3.3 prezentuje veřejná
                        prostranství pomocí analýzy veřejné přístupnosti, uličních prostranství a jejich morfologie. Popis prostorového uspořádání města je završen
                        kompozicí a vizuálními podmínkami (🡪 3.4), které se zaobírají tématy historických vedut, významných pohledů na město, a stavebních dominant.
                        Významné pohledy na město byly nově pro ÚAP 2024 revidovány a vznikla nová podoba aplikace, kde je možné je prohlížet.
                        """, 16, 360),
                arguments("Short text", 1, 100)
        );
    }

    static String paragraphs(String... parts) {
        return String.join(BREAK, parts);
    }

    static Stream<Arguments> multiParamProviderProcessTest() {
        final String fourSentences = "The first sentence is exactly this long here. The second sentence is exactly this long too."
                + " The third sentence is exactly this long also. The fourth sentence is exactly this long still.";
        final String ninetyChars = "The first sentence of the paragraph is long enough. The second sentence of it is long too.";
        final String longSentence = "This one single sentence is deliberately much longer than the limit for a sentence chunk"
                + " so it has to stay whole";
        final String longParagraph = "Alpha beta gamma delta epsilon zeta eta theta. Iota kappa lambda mu nu xi omicron pi."
                + " Rho sigma tau upsilon phi chi psi omega. One two three four five six seven eight."
                + " Nine ten eleven twelve thirteen fourteen.";
        final String czech = paragraphs(
                "Cílem knihy je popis fyzického prostředí hl. m. Prahy jako sídla v krajině."
                        + " Kniha řeší stavby a prostor, který je obklopuje.",
                "Druhá kapitola se zabývá historií města. Jak se utvářelo a jakými etapami prošlo?",
                "Třetí kapitola popisuje současné uspořádání! Začíná strukturálním přístupem.");

        //        description,                          text, surviving text, max paragraph, max sentence, paragraph chunks, sentence chunks
        return Stream.of(
                // --- the smallest inputs ---
                arguments("empty text",
                        "", ALL_KEPT, 100, 50, 1, 0),
                arguments("one short sentence",
                        "Hello world.", ALL_KEPT, 100, 50, 1, 1),
                arguments("one sentence without a terminator",
                        "Hello world without terminator", ALL_KEPT, 100, 50, 1, 1),
                arguments("whitespace-only paragraph",
                        paragraphs("Before.", "   ", "After."), ALL_KEPT, 100, 100, 1, 1),

                // --- sentences of one paragraph ---
                arguments("sentences are merged up to the sentence limit",
                        fourSentences, ALL_KEPT, 500, 100, 1, 2),
                arguments("limit below two sentences: every sentence alone",
                        fourSentences, ALL_KEPT, 500, 60, 1, 4),
                arguments("a sentence longer than the limit stays whole between short ones",
                        "Tiny start. " + longSentence + ". Tiny end.", ALL_KEPT, 500, 50, 1, 3),
                arguments("two 26-character sentences are not glued into one (28 + 28 is not below 56)",
                        "a".repeat(26) + ". " + "b".repeat(26), ALL_KEPT, 100, 30, 1, 2),
                arguments("question marks, exclamation marks and line breaks",
                        "Is it ready? Yes it is ready! Then we can start now.\nNew line sentence continues here and goes on.",
                        ALL_KEPT, 100, 60, 1, 2),

                // --- paragraphs ---
                arguments("short paragraphs end up in one paragraph chunk and in one sentence chunk",
                        paragraphs("Short one.", "Other short.", "Third one."), ALL_KEPT, 100, 100, 1, 1),
                arguments("paragraph break counts towards the paragraph limit (48 + 4 + 48 > 96)",
                        paragraphs("a".repeat(48), "b".repeat(48)), ALL_KEPT, 48, 100, 2, 2),
                arguments("two paragraphs merge only below 0.9 * limit (56 > 54, although 56 <= 60)",
                        paragraphs("a".repeat(24), "b".repeat(24)), ALL_KEPT, 100, 60, 1, 2),
                arguments("paragraph limit keeps long paragraphs apart",
                        paragraphs(ninetyChars, ninetyChars, ninetyChars), ALL_KEPT, 50, 100, 3, 3),
                arguments("paragraph limit allows a pair, the sentence limit then separates them again",
                        paragraphs(ninetyChars, ninetyChars, ninetyChars), ALL_KEPT, 100, 100, 2, 3),
                arguments("an oversized paragraph is split by sentences, its neighbour is not touched",
                        paragraphs(longParagraph, "Short tail paragraph."), ALL_KEPT, 60, 80, 2, 5),
                arguments("tiny limits: nothing is merged, nothing is lost",
                        paragraphs("aaa bbb.", "ccc ddd."), ALL_KEPT, 1, 1, 2, 2),
                arguments("empty paragraph between two paragraphs",
                        "Before the gap there is a sentence of some length." + BREAK + BREAK
                                + "After the gap there is a sentence of some length.",
                        ALL_KEPT, 100, 200, 1, 1),
                arguments("leading and trailing paragraph break",
                        BREAK + "Only real paragraph here with a sentence of some length." + BREAK,
                        ALL_KEPT, 100, 200, 1, 1),

                // --- content that is changed on purpose ---
                arguments("bracket content is removed",
                        "The book (see chapter 3. Details) describes the city. Next (🡪 3.1) sentence follows here"
                                + " and is long enough to matter." + BREAK + "Second paragraph (with a note) ends here.",
                        "The book describes the city. Next sentence follows here and is long enough to matter."
                                + BREAK + "Second paragraph ends here.",
                        100, 80, 1, 3),

                // --- realistic text ---
                arguments("Czech text with an abbreviation and three paragraphs",
                        czech, ALL_KEPT, 150, 120, 1, 4)
        );
    }

    /** Limits {maxParagraphLength, maxSentenceLength} from "everything alone" to "everything in one chunk". */
    static Stream<Arguments> multiParamProviderProcessLimits() {
        return Stream.of(
                arguments(1, 1), arguments(10, 20), arguments(30, 40), arguments(50, 60),
                arguments(100, 100), arguments(200, 150), arguments(10_000, 10_000));
    }
}
