import cz.cvut.kbss.textanalysis.lemmatizer.model.SingleLemmaResult;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class MultiParamProviders {

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

    private final static String ONE_SPACE = " ";
    private final static String TWO_SPACES = "  ";
    private final static String THREE_SPACES = "   ";
    private final static String FOUR_SPACES = "    ";
    private final static String FIVE_SPACES = "     ";
    private final static String SIX_SPACES = "      ";
    private final static String SEVEN_SPACES = "       ";
    private final static String EIGHT_SPACES = "        ";

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

    static Stream<Arguments> multiParamProviderTokens() {
        return Stream.of(
                arguments("""
                        java.lang.RuntimeException: Error sending request: API error: status=429, body={"error":{"message":"Rate limit reached for model `openai/gpt-oss-20b` in organization `org_01m2jqatmbe5gsxj6afjp7zd6d` service tier `on_demand` on tokens per minute (TPM): Limit 8000, Used 5420, Requested 2757. Please try again in 1.3275s. Need more tokens? Upgrade to Dev Tier today at https://console.groq.com/settings/billing","type":"tokens","code":"rate_limit_exceeded"}}
                        """, 1_327.5),
                arguments("""
                        java.lang.RuntimeException: Error sending request: API error: status=429, body={"error":{"message":"Rate limit reached for model `openai/gpt-oss-120b` in organization `org_01m2jqatmbe5gsxj6afjp7zd6d` service tier `on_demand` on tokens per minute (TPM): Limit 8000, Used 5420, Requested 2757. Please try again in 670.5ms. Need more tokens? Upgrade to Dev Tier today at https://console.groq.com/settings/billing","type":"tokens","code":"rate_limit_exceeded"}}
                        """, 670.5)
        );
    }
}
