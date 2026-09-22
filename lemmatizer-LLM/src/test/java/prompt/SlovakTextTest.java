package prompt;

import cz.cvut.kbss.textanalysis.lemmatizer.model.LemmatizerResult;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SlovakTextTest extends AbstractTextTest {

    @Test
    @Override
    public void testSimpleText(String input, String expected) {
        final LemmatizerResult result =
                lemmatizer.process("Otvorením okna v budove sa narušia jeho izolačné vlastnosti",
                        "sk");
        final List<String> lemmas = List.of(
                "Otvorenie", "okno", "v", "budova", "sa", "narušiť", "narúšiť", "jeho", "izolačné", "izolačný", "vlastnosť");
        test(lemmas, result, 90);
    }

    /**
     * @link <a href="https://uniba.sk/o-univerzite/historia/budova-univerzity-na-safarikovom-namesti-c-6/">source</a>
     */
    @Test
    @Override
    public void testOneParagraphText(String input, String expected) {
        final LemmatizerResult result =
                lemmatizer.process("""
                        Pôvodným vlastníkom pozemku bol Ján Pálffy. V roku 1918 štát pozemok skonfiškoval. 
                        Začiatkom dvadsiatych rokov bol vrátený dedičom J. Pálffyho, ktorí sa uzavretím 
                        dohody s Mestským notárskym úradom (ďalej MNÚ) dňa 19.januára 1921 zaviazali opraviť 
                        chátrajúce budovy. Neskôr pozemok odkúpila Továreň na káble a. s. v Bratislave, 
                        ktorá ho 22.3.1924 predala štátu, v zastúpení Ministerstvom obchodu a verejných prác 
                        za 2,6 mil Kč.
                        """, "sk");
        final List<String> lemmas = List.of(
                // Veta 1
                "Pôvodný", // Pôvodným
                "vlastník", // vlastníkom
                "pozemok", // pozemku
                "byť", // bol
                "Ján", // Ján
                "Pálffy", // Pálffy
                ".", // .

                // Veta 2
                "V", // V
                "rok", // roku
                "1918", // 1918
                "štát", // štát
                "pozemok", // pozemok
                "skonfiškovať", // skonfiškoval
                ".", // .

                // Veta 3
                "Začiatok", // Začiatkom
                "dvadsiatych", // dvadsiatych
                "rok", // rokov
                "byť", // bol
                "vrátený", // vrátený
                "dedič", // dedičom
                "J.", // J.
                "Pálffy", // Pálffyho
                ",", // ,
                "ktorý", // ktorí
                "sa", // sa
                "uzavretie", // uzavretím
                "dohoda", // dohody
                "s", // s
                "Mestský", // Mestským
                "notársky", // notárskym
                "úrad", // úradom
                "(", // (
                "ďaleko", // ďalej
                "MNÚ", // MNÚ
                ")", // )
                "deň", // dňa
                "19.", // 19.
                "január", // januára
                "1921", // 1921
                "zaviazať", // zaviazali
                "opraviť", // opraviť
                "chátrajúci", // chátrajúce
                "budova", // budovy
                ".", // .

                // Veta 4
                "Neskoro", // Neskôr
                "pozemok", // pozemok
                "odkúpiť", // odkúpila
                "Továreň", // Továreň
                "na", // na
                "kábel", // káble
                "a.", // a.
                "s.", // s.
                "v", // v
                "Bratislava", // Bratislave
                ",", // ,
                "ktorý", // ktorá
                "on", // ho
                "22.3.1924", // 22.3.1924
                "predať", // predala
                "štát", // štátu
                ",", // ,
                "v", // v
                "zastúpenie", // zastúpení
                "Ministerstvo", // Ministerstvom
                "obchod", // obchodu
                "a", // a
                "verejný", // verejných
                "práca", // prác
                "za", // za
                "2,6", // 2,6
                "mil", // mil
                "Kč", // Kč
                "." // .
        );
        test(lemmas, result, 85);
    }
}