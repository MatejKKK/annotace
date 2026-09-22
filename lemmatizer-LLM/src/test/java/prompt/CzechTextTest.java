package prompt;

import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CzechTextTest extends AbstractTextTest {

    @Test
    @Override
    public void testSimpleText() {
        final LemmatizerResult result =
                lemmatizer.process("Starý člověk chodí do kostela častěji než mladší lidé.",
                        "cz");
        final List<String> lemmas = List.of("Starý", "člověk", "chodit", "do", "kostel", "často", "než", "mladý", "lidé", ".");
        test(lemmas, result, 90);
    }

    @Test
    @Override
    public void testOneParagraphText() {
        final LemmatizerResult result =
                lemmatizer.process("""
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
                        """, "cz");
        final List<String> lemmas = List.of(
                // Věta 1
                "Cíl", // Cílem
                "kniha", // knihy
                "200", // 200
                "|", // |
                "Město", // Město
                "|", // |
                "Fyzický", // Fyzické
                "vystavěný", // vystavěné
                "prostředí", // prostředí
                "být", // je
                "popis", // popis
                "fyzický", // fyzického
                "vystavěný", // vystavěného
                "prostředí", // prostředí
                "hl.", // hl.
                "m.", // m.
                "Praha", // Prahy
                "jako", // jako
                "sídlo", // sídla
                "v", // v
                "krajina", // krajině
                ".", // .

                // Věta 2
                "Kniha", // Kniha
                "řešit", // řeší
                "stavba", // stavby
                "a", // a
                "prostor", // prostor
                ",", // ,
                "který", // který
                "on", // je
                "obklopovat", // obklopuje
                "–", // –
                "popisovat", // popisuje
                "a", // a
                "analyzovat", // analyzuje
                "„", // „
                "scéna", // scénu
                "“", // “
                "pro", // pro
                "lidský", // lidské
                "činnost", // činnosti
                ",", // ,
                "který", // kterými
                "se", // se
                "naopak", // naopak
                "zabývat", // zabývá
                "kniha", // kniha
                "300", // 300
                "|", // |
                "Využití", // Využití
                "území", // území
                ".", // .

                // Věta 3
                "Daleko", // Dále
                "také", // také
                "navazovat", // navazuje
                "a", // a
                "částečně", // částečně
                "se", // se
                "prolínat", // prolíná
                "s", // s
                "kniha", // knihou
                "100", // 100
                "|", // |
                "Krajina", // Krajina
                ",", // ,
                "a", // a
                "ten", // to
                "zejména", // zejména
                "v", // v
                "téma", // tématech
                ",", // ,
                "u", // u
                "který", // kterých
                "nelze", // nelze
                "jednoznačně", // jednoznačně
                "oddělit", // oddělit
                "prostředí", // prostředí
                "vystavěný", // vystavěné
                "od", // od
                "krajinný", // krajinného
                ".", // .

                // Věta 4
                "Čtenář", // Čtenář
                "kniha", // knihy
                "200", // 200
                "získat", // získá
                "informace", // informace
                "o", // o
                "vývoj", // vývoji
                "fyzický", // fyzického
                "vystavěný", // vystavěného
                "prostředí", // prostředí
                ",", // ,
                "o", // o
                "jeho", // jeho
                "hodnota", // hodnotách
                ",", // ,
                "a", // a
                "daleko", // dále
                "o", // o
                "charakteristika", // charakteristice
                "prostorový", // prostorového
                "uspořádání", // uspořádání
                "město", // města
                "a", // a
                "také", // také
                "vztah", // vztazích
                "a", // a
                "vzorec", // vzorcích
                "vznikající", // vznikajících
                "v", // v
                "městský", // městském
                "prostor", // prostoru
                ".", // .

                // Věta 5
                "Téma", // Témata
                "uvedený", // uvedená
                "v", // v
                "kniha", // knize
                "být", // jsou
                "zpravidla", // zpravidla
                "řešený", // řešena
                "v", // v
                "rozsah", // rozsahu
                "lokalita", // lokalit
                "městský", // městské
                "krajina", // krajiny
                ",", // ,
                "a", // a
                "ten", // to
                "zejména", // zejména
                "lokalita", // lokalit
                "vystavěný", // vystavěného
                "prostředí", // prostředí
                ".", // .

                // Věta 6
                "U", // U
                "některý", // některých
                "téma", // témat
                "být", // je
                "řešený", // řešené
                "území", // území
                "rozšířený", // rozšířeno
                ",", // ,
                "aby", // aby
                "docházet", // nedocházelo
                "k", // k
                "umělý", // umělému
                "rozdělování", // rozdělování
                "fenomén", // fenoménů
                ".", // .

                // Věta 7
                "Kniha", // Kniha
                "začínat", // začíná
                "úvodní", // úvodní
                "kapitola", // kapitolou
                "1", // 1
                ",", // ,
                "který", // která
                "kromě", // kromě
                "celkový", // celkového
                "úvod", // úvodu
                "a", // a
                "návod", // návodu
                "na", // na
                "orientace", // orientaci
                "v", // v
                "dokument", // dokumentu
                "obsahovat", // obsahuje
                "téma", // témata
                "souvislost", // souvislostí
                "mezi", // mezi
                "město", // městem
                "a", // a
                "krajina", // krajinou
                "a", // a
                "související", // souvisejících
                "strategický", // strategických
                "dokument", // dokumentů
                ".", // .

                // Věta 8
                "Kniha", // Kniha
                "pokračovat", // pokračuje
                "kapitola", // kapitolou
                "2", // 2
                "Vývoj", // Vývoj
                "vystavěný", // vystavěného
                "prostředí", // prostředí
                ",", // ,
                "který", // která
                "popisovat", // popisuje
                "historie", // historii
                "město", // města
                "a", // a
                "hodnota", // hodnoty
                "v", // v
                "on", // ní
                "založený", // založené
                ".", // .

                // Věta 9
                "V", // V
                "podkapitola", // podkapitole
                "2.1", // 2.1
                "popisovat", // popisuje
                "historický", // historické
                "městský", // městské
                "prostředí", // prostředí
                "–", // –
                "jak", // jak
                "se", // se
                "utvářet", // utvářelo
                "a", // a
                "jaký", // jakými
                "etapa", // etapami
                "v", // v
                "Praha", // Praze
                "projít", // prošlo
                ".", // .

                // Věta 10
                "Navazovat", // Navazuje
                "popis", // popis
                "hodnota", // hodnot
                "město", // města
                "jako", // jako
                "soubor", // souboru
                "nemovitý", // nemovitých
                "statek", // statků
                "kulturní", // kulturního
                "dědictví", // dědictví
                ".", // .

                // Věta 11
                "Druhá", // Druhá
                "obsahový", // obsahová
                "kapitola", // kapitola
                "3", // 3
                "se", // se
                "zabývat", // zabývá
                "současný", // současným
                "prostorový", // prostorovým
                "uspořádání", // uspořádáním
                "město", // města
                ".", // .

                // Věta 12
                "Začínat", // Začíná
                "uvedení", // uvedením
                "současný", // současného
                "pojetí", // pojetí
                "územní", // územního
                "plánování", // plánování
                "hlavní", // hlavního
                "město", // města
                "pomoc", // pomocí
                "strukturálně", // strukturálně
                "zaměřený", // zaměřeného
                "přístup", // přístupu
                ",", // ,
                "postavený", // postaveného
                "na", // na
                "převažující", // převažujícím
                "charakter", // charakteru
                "lokalita", // lokalit
                "(", // (
                "🡪", // 🡪
                "3.1", // 3.1
                ")", // )
                ".", // .

                // Věta 13
                "Daleko", // Dále
                "být", // jsou
                "řešený", // řešeny
                "morfologický", // morfologické
                "charakteristika", // charakteristiky
                "zástavba", // zástavby
                "(", // (
                "🡪", // 🡪
                "3.2", // 3.2
                ")", // )
                ".", // .

                // Věta 14
                "Následující", // Následující
                "podkapitola", // podkapitola
                "3.3", // 3.3
                "prezentovat", // prezentuje
                "veřejný", // veřejná
                "prostranství", // prostranství
                "pomocí", // pomocí
                "analýza", // analýzy
                "veřejný", // veřejné
                "přístupnost", // přístupnosti
                ",", // ,
                "uliční", // uličních
                "prostranství", // prostranství
                "a", // a
                "jejich", // jejich
                "morfologie", // morfologie
                ".", // .

                // Věta 15
                "Popis", // Popis
                "prostorový", // prostorového
                "uspořádání", // uspořádání
                "město", // města
                "být", // je
                "završený", // završen
                "kompozice", // kompozicí
                "a", // a
                "vizuální", // vizuálními
                "podmínka", // podmínkami
                "(", // (
                "🡪", // 🡪
                "3.4", // 3.4
                ")", // )
                ",", // ,
                "který", // které
                "se", // se
                "zaobírat", // zaobírají
                "téma", // tématy
                "historický", // historických
                "veduta", // vedut
                ",", // ,
                "významný", // významných
                "pohled", // pohledů
                "na", // na
                "město", // město
                ",", // ,
                "a", // a
                "stavební", // stavebních
                "dominanta", // dominant
                ".", // .

                // Věta 16
                "Významný", // Významné
                "pohled", // pohledy
                "na", // na
                "město", // město
                "být", // byly
                "nově", // nově
                "pro", // pro
                "ÚAP", // ÚAP
                "2024", // 2024
                "revidovaný", // revidovány
                "a", // a
                "vzniknout", // vznikla
                "nový", // nová
                "podoba", // podoba
                "aplikace", // aplikace
                ",", // ,
                "kde", // kde
                "být", // je
                "možný", // možné
                "on", // je
                "prohlížet", // prohlížet
                "." // .
        );
        test(lemmas, result, 90);
    }
}
