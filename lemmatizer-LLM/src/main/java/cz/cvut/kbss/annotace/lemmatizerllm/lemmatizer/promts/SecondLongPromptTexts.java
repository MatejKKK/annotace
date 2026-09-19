package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts;

public final class SecondLongPromptTexts extends AbstractPromptTexts {
    @Override
    public String CZECH() {
        return """
Jsi validátor a oprava výstupu z morfologické lemmatizace českého textu. Dostaneš dva vstupy: (1) PŮVODNÍ TEXT, celý odstavec, který může obsahovat jednu i více vět, a (2) NÁVRH, výsledek lemmatizace vytvořený samostatně věta po větě - a proto může obsahovat chyby vzniklé tím, že jednotlivé věty nebyly posuzovány v kontextu celého odstavce.

Tvým úkolem je návrh zkontrolovat a opravit, ne vytvořit lemmatizaci znovu od začátku.

U každého řádku ve tvaru původní_tvar-základní_tvar prověř zejména:
- Je sloveso v infinitivu a byla odstraněna případná záporka "ne-"?
- Je podstatné jméno v 1. pádu jednotného čísla, nebo (u pomnožných jmen) v 1. pádu množného čísla - a ne ve vymyšleném jednotném čísle tam, kde skutečné jednotné číslo neexistuje?
- Je přídavné jméno nebo příslovce v 1. stupni, i když bylo v návrhu chybně ponecháno ve 2. nebo 3. stupni?
- Je zájmeno v citátové formě?
- Zůstala diakritika stejná, jako by měl mít standardní slovníkový lemma?
- Zůstalo velké/malé písmeno přesně podle originálu, včetně slov na začátku věty, která jsou velká jen díky pozici?
- Odpovídá počet řádků přesně počtu slov a interpunkčních znamének v PŮVODNÍM TEXTU, v nezměněném pořadí, bez chybějících, přidaných nebo slitých řádků?
- Je stejné slovo, které se v odstavci opakuje (i v jiné větě), lemmatizováno pořád stejně?

Pokud je řádek správně, přepiš ho beze změny. Pokud obsahuje chybu v základním tvaru, oprav pouze tuto část. Pokud návrh chybně sloučil dvě slova do jednoho řádku, nebo naopak nějaké slovo vynechal či zdvojil, řádky rozděl nebo doplň tak, aby přesně odpovídaly původnímu textu.

Výstup: pouze opravené řádky ve formátu původní_tvar-základní_tvar, jeden pár na řádek, v původním pořadí, pro celý odstavec najednou. Nepiš žádné odůvodnění, žádné nadpisy, žádné oddělovače vět - jen finální opravené řádky.

Příklad 1:
PŮVODNÍ TEXT: "Nejstarší dcery vařily lepší guláš než jejich matky. Nikdo z rodiny ho ale nikdy nedojedl."
NÁVRH:
Nejstarší-Nejstarý
dcery-dcera
vařily-vařit
lepší-lepší
guláš-guláš
než-než
jejich-jejich
matky-matka
.-.
Nikdo-Nikdo
z rodiny-z rodina
ho-on
ale-ale
nikdy-nikdy
nedojedl-nedojedl
.-.
OPRAVENÝ VÝSLEDEK:
Nejstarší-Starý
dcery-dcera
vařily-vařit
lepší-dobrý
guláš-guláš
než-než
jejich-jejich
matky-matka
.-.
Nikdo-Nikdo
z-z
rodiny-rodina
ho-on
ale-ale
nikdy-nikdy
nedojedl-dojíst
.-.

Příklad 2:
PŮVODNÍ TEXT: "Ředitel podniku slíbil zaměstnancům vyšší mzdy. Zaměstnanci mu ale nevěřili a mzdy zůstaly stejné."
NÁVRH:
Ředitel-Ředitel
podniku-podnik
slíbil-slíbit
zaměstnancům-zaměstnanec
vyšší-vyšší
mzdy-mzda
.-.
Zaměstnanci-Zaměstnankyně
mu-on
ale-ale
nevěřili-věřit
a-a
mzdy-mzda
zůstaly-zůstat
stejné-stejný
.-.
OPRAVENÝ VÝSLEDEK:
Ředitel-Ředitel
podniku-podnik
slíbil-slíbit
zaměstnancům-zaměstnanec
vyšší-vysoký
mzdy-mzda
.-.
Zaměstnanci-Zaměstnanec
mu-on
ale-ale
nevěřili-věřit
a-a
mzdy-mzda
zůstaly-zůstat
stejné-stejný
.-.

Nyní zkontroluj a oprav následující:
PŮVODNÍ TEXT:
""" + this.originalParagraph + """
NÁVRH:
                """;
    }

    @Override
    public String ENGLISH() {
        return """
You are a validator and corrector for the output of English morphological lemmatization. You receive two inputs: (1) ORIGINAL TEXT, a full paragraph that may contain one or more sentences, and (2) DRAFT, a lemmatization produced separately sentence by sentence - which may therefore contain errors caused by each sentence being judged without the context of the whole paragraph.

Your job is to check and correct the draft, not to redo the lemmatization from scratch.

For every line in the form original_form-base_form, check in particular:
- Is the verb in its base/infinitive form, with tense and person removed?
- Is the noun in its singular form, or (for a plurale tantum such as "clothes", "trousers", "scissors") kept in its plural form rather than reduced to an invented singular?
- Is the adjective or adverb in the positive degree, even if the draft incorrectly left it in the comparative or superlative (including irregular/suppletive forms like worse -> bad, best -> good)?
- Is the pronoun in its citation/subject form?
- Does the capitalization exactly match the original, including ordinary words capitalized only because they start a sentence?
- Does the number of lines exactly match the number of words and punctuation marks in the ORIGINAL TEXT, in the original order, with no missing, duplicated, or merged lines?
- Is the same word, if it repeats anywhere in the paragraph (even in a different sentence), lemmatized the same way every time?

If a line is already correct, copy it unchanged. If it contains an error in the base form, fix only that part. If the draft wrongly merged two words into one line, or omitted or duplicated a word, split or add lines so they exactly match the original text.

Output: only the corrected lines in the format original_form-base_form, one pair per line, in the original order, for the whole paragraph at once. Do not write any explanation, headers, or sentence separators - just the final corrected lines.

Example 1:
ORIGINAL TEXT: "The two brothers were the strongest players on the team. Their coach said the younger boys had tried harder than anyone."
DRAFT:
The-The
two-two
brothers-brother
were-be
the-the
strongest-strongest
players-player
on-on
the-the
team-team
.-.
Their-Their
coach-coach
said-say
the-the
younger-younger
boys-boy
had-have
tried-tried
harder-harder
than-than
anyone-anyone
.-.
CORRECTED RESULT:
The-The
two-two
brothers-brother
were-be
the-the
strongest-strong
players-player
on-on
the-the
team-team
.-.
Their-Their
coach-coach
said-say
the-the
younger-young
boys-boy
had-have
tried-try
harder-hard
than-than
anyone-anyone
.-.

Example 2:
ORIGINAL TEXT: "The children wore muddier shoes than usual, and their trousers looked worse too. Nobody noticed until the teacher saw them and laughed."
DRAFT:
The-The
children-child
wore-wear
muddier-muddy
shoes-shoe
than-than
usual-usual
,-,
and-and
their-their
trousers-trouser
looked-look
worse-worse
too-too
.-.
Nobody-Nobody
noticed-notice
until-until
the-the
teacher-teacher
saw-saw
them-they
and-and
laughed-laugh
.-.
CORRECTED RESULT:
The-The
children-child
wore-wear
muddier-muddy
shoes-shoe
than-than
usual-usual
,-,
and-and
their-their
trousers-trousers
looked-look
worse-bad
too-too
.-.
Nobody-Nobody
noticed-notice
until-until
the-the
teacher-teacher
saw-see
them-they
and-and
laughed-laugh
.-.

Now check and correct the following:
ORIGINAL TEXT:
""" + this.originalParagraph + """
DRAFT:
                """;
    }

    @Override
    public String GERMAN() {
        return """
Du bist ein Validierungs- und Korrekturwerkzeug für das Ergebnis einer deutschen morphologischen Lemmatisierung. Du bekommst zwei Eingaben: (1) URSPRÜNGLICHER TEXT, einen ganzen Absatz, der einen oder mehrere Sätze enthalten kann, und (2) ENTWURF, ein Ergebnis, das satzweise einzeln erstellt wurde - und deshalb Fehler enthalten kann, die dadurch entstanden sind, dass die einzelnen Sätze ohne den Kontext des ganzen Absatzes beurteilt wurden.

Deine Aufgabe ist es, den Entwurf zu prüfen und zu korrigieren, nicht die Lemmatisierung von Grund auf neu zu erstellen.

Prüfe bei jeder Zeile im Format Originalform-Grundform besonders:
- Steht das Verb im Infinitiv, und wurde bei trennbaren Verben nur das eigentliche Verb (nicht das Partikel-Wort) auf den einfachen Infinitiv gebracht?
- Steht das Substantiv im Nominativ Singular, oder - bei einem Pluraletantum wie "Eltern", "Leute" - im Nominativ Plural statt in einer erfundenen Singularform?
- Steht das Adjektiv oder Adverb im Positiv, auch wenn der Entwurf es fälschlich im Komparativ oder Superlativ gelassen hat (auch bei unregelmäßiger Steigerung wie besser -> gut, lieber -> gern)?
- Wurde der Artikel oder das Relativpronomen korrekt auf "der" normalisiert, statt in der flektierten Form (die, den, dem ...) belassen zu werden?
- Stimmt die Groß-/Kleinschreibung exakt mit dem Original überein, auch bei Wörtern, die nur wegen der Satzposition groß geschrieben sind?
- Wurden Umlaute und ß in der Grundform beibehalten, ohne sie zu entfernen oder zu ersetzen?
- Entspricht die Anzahl der Zeilen genau der Anzahl der Wörter und Satzzeichen im URSPRÜNGLICHEN TEXT, in unveränderter Reihenfolge, ohne fehlende, doppelte oder zusammengeführte Zeilen?
- Wurde dasselbe Wort, wenn es im Absatz wiederkehrt (auch in einem anderen Satz), immer gleich lemmatisiert?

Wenn eine Zeile schon richtig ist, übernimm sie unverändert. Wenn sie einen Fehler in der Grundform enthält, korrigiere nur diesen Teil. Wenn der Entwurf fälschlich zwei Wörter zu einer Zeile zusammengefasst hat, oder ein Wort ausgelassen oder doppelt aufgeführt hat, teile die Zeile auf bzw. ergänze sie so, dass sie genau dem Originaltext entspricht.

Ausgabe: nur die korrigierten Zeilen im Format Originalform-Grundform, ein Paar pro Zeile, in der ursprünglichen Reihenfolge, für den ganzen Absatz auf einmal. Schreibe keine Begründung, keine Überschriften, keine Satztrenner - nur die finalen korrigierten Zeilen.

Beispiel 1:
URSPRÜNGLICHER TEXT: "Die Köchin kochte die besten Suppen in dem ganzen Dorf. Ihre Gäste kamen jede Woche wieder und lobten sie lauter als früher."
ENTWURF:
Die-Die
Köchin-Köchin
kochte-kochen
die-der
besten-besten
Suppen-Suppe
in-in
dem-der
ganzen-ganz
Dorf-Dorf
.-.
Ihre-Ihre
Gäste-Gäste
kamen-kommen
jede-jeder
Woche-Woche
wieder-wieder
und-und
lobten-loben
sie-sie
lauter-lauter
als-als
früher-frühere
.-.
KORRIGIERTES ERGEBNIS:
Die-Die
Köchin-Köchin
kochte-kochen
die-der
besten-gut
Suppen-Suppe
in-in
dem-der
ganzen-ganz
Dorf-Dorf
.-.
Ihre-Ihre
Gäste-Gast
kamen-kommen
jede-jeder
Woche-Woche
wieder-wieder
und-und
lobten-loben
sie-sie
lauter-laut
als-als
früher-früh
.-.

Beispiel 2:
URSPRÜNGLICHER TEXT: "Der kleine Hund lief schneller als die großen Katzen."
ENTWURF:
Der-Der
kleine-klein
Hund-Hund
lief-laufen
schneller-schneller
als-als
die-die
großen Katzen-groß Katze
.-.
KORRIGIERTES ERGEBNIS:
Der-Der
kleine-klein
Hund-Hund
lief-laufen
schneller-schnell
als-als
die-der
großen-groß
Katzen-Katze
.-.

Prüfe und korrigiere nun Folgendes:
URSPRÜNGLICHER TEXT:
""" + this.originalParagraph + """
ENTWURF:
               """;
    }

    @Override
    public String SLOVAK() {
        return """
Si validátor a opravovač výstupu z morfologickej lematizácie slovenského textu. Dostaneš dva vstupy: (1) PÔVODNÝ TEXT - celý odsek, ktorý môže obsahovať jednu aj viac viet, a (2) NÁVRH - výsledok lematizácie vytvorený samostatne veta po vete, ktorý preto môže obsahovať chyby vzniknuté tým, že jednotlivé vety neboli posudzované v kontexte celého odseku.

Tvoja úloha je návrh skontrolovať a opraviť, nie vytvoriť lematizáciu znova od začiatku.

Pri každom riadku vo tvare pôvodný_tvar-základný_tvar over najmä:
- Je sloveso v infinitíve a bola odstránená prípadná záporka "ne-"?
- Je podstatné meno v 1. páde jednotného čísla, alebo (pri pomnožných menách) v 1. páde množného čísla - a nie vo vymyslenom jednotnom čísle tam, kde skutočné jednotné číslo neexistuje?
- Je prídavné meno alebo príslovka v 1. stupni, aj keď bolo v návrhu nesprávne ponechané v 2. alebo 3. stupni?
- Je zámeno v citátovej forme?
- Zostala diakritika presne taká, akú by mal mať slovníkový základný tvar?
- Zostalo veľké/malé písmeno presne podľa originálu, vrátane slov na začiatku vety, ktoré sú veľké len vďaka pozícii?
- Zodpovedá počet riadkov presne počtu slov a interpunkčných znamienok v PÔVODNOM TEXTE, v nezmenenom poradí, bez chýbajúcich, pridaných alebo zlúčených riadkov?
- Je rovnaké slovo, ktoré sa v odseku opakuje (aj v inej vete), lematizované vždy rovnako?

Ak je riadok správny, prepíš ho bez zmeny. Ak obsahuje chybu v základnom tvare, oprav len túto časť. Ak návrh chybne zlúčil dve slová do jedného riadku, alebo naopak nejaké slovo vynechal či zdvojil, riadky rozdeľ alebo doplň tak, aby presne zodpovedali pôvodnému textu.

Výstup: len opravené riadky vo formáte pôvodný_tvar-základný_tvar, jeden pár na riadok, v pôvodnom poradí, pre celý odsek naraz. Nepíš žiadne odôvodnenie, žiadne nadpisy, žiadne oddeľovače viet - len finálne opravené riadky.

Príklad 1:
PÔVODNÝ TEXT: "Naši najlepší predavači predali viac tovaru než minulý rok. Zákazníci si ale sťažovali, že ceny boli vyššie ako predtým."
NÁVRH:
Naši-Náš
najlepší-najlepší
predavači-predavač
predali-predať
viac-viac
tovaru-tovar
než-než
minulý-minulý
rok-rok
.-.
Zákazníci-Zákazník
si-si
ale-ale
sťažovali-sťažovať
,-,
že-že
ceny-cena
boli-byť
vyššie-vyššie
ako-ako
predtým-predtým
.-.
OPRAVENÝ VÝSLEDOK:
Naši-Náš
najlepší-dobrý
predavači-predavač
predali-predať
viac-veľa
tovaru-tovar
než-než
minulý-minulý
rok-rok
.-.
Zákazníci-Zákazník
si-si
ale-ale
sťažovali-sťažovať
,-,
že-že
ceny-cena
boli-byť
vyššie-vysoký
ako-ako
predtým-predtým
.-.

Príklad 2:
PÔVODNÝ TEXT: "Bez nožníc si dievčatá nemohli ostrihať vlasy tak krátko, ako chceli."
NÁVRH:
Bez-Bez
nožníc-nožnica
si-si
dievčatá-dievča
nemohli-nemohli
ostrihať-ostrihať
vlasy-vlasy
tak-tak
krátko-krátko
ako-ako
chceli-chcieť
.-.
OPRAVENÝ VÝSLEDOK:
Bez-Bez
nožníc-nožnice
si-si
dievčatá-dievča
nemohli-môcť
ostrihať-ostrihať
vlasy-vlas
tak-tak
krátko-krátko
ako-ako
chceli-chcieť
.-.

Teraz skontroluj a oprav nasledujúce:
PÔVODNÝ TEXT:
""" + this.originalParagraph + """
NÁVRH:
                """;
    }

    private String originalParagraph = "";

    @Override
    public void setOriginal(String originalParagraph) {
        this.originalParagraph = originalParagraph;
    }
}
