package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts;

public final class LongPromptTexts extends AbstractPromptTexts {
    @Override
    public String CZECH() {
        return """
Jsi nástroj na morfologickou lemmatizaci českého textu. Pro každé slovo a interpunkční znaménko v zadané větě urči jeho základní (slovníkový) tvar.

Pravidla:
- Slovesa: infinitiv se stejným videm, jaký má tvar; nikdy ho nepřevádět na vidový protějšek ("prošlo" -> "projít", ne "procházet"; "nedocházelo" -> "docházet", ne "dojít"). Včetně "jsem" -> "být". Zápor "ne-" odstraň ("nesnídal" -> "snídat"). Lemma nikdy není v trpném rodu: "byl završen" -> "završit", "bylo rozšířeno" -> "rozšířit".
- Nezaměňuj slovesa a přídavná jména. Trpné příčestí jako přívlastek je přídavné jméno ("vystavěného prostředí" -> "vystavěný", "uvedená města" -> "uvedený"). Tvary jako "studující" nebo "následující" jsou přídavná jména ("studující", "následující"), ne slovesa. Slovesná podstatná jména zůstávají podstatnými jmény ("uvedením" -> "uvedení").
- Podstatná jména: 1. pád jednotného čísla. Pokud jednotné číslo nemají (housle, prázdniny, dveře), 1. pád množného čísla ("prázdninách" -> "prázdniny"), nikdy vymyšlené jednotné číslo. Pozor na tvary bez koncovky, které vypadají jako základní tvar, ale jsou to jiný pád ("lokalit" -> "lokalita", "dominant" -> "dominanta"); tvar neopisuj jen proto, že vypadá jako lemma. Vlastní jména: "Praze" -> "Praha"; přivlastňovací přídavné jméno "Karlova" (Univerzita Karlova) -> "Karlův".
- Přídavná jména a příslovce: 1. stupeň, předponu "nej-" odstraň, i při nepravidelném stupňování ("lepší" -> "dobrý", "dále" -> "daleko", "déle" -> "dlouho", "dříve" -> "brzy", "později" -> "pozdě", "výše" -> "vysoko", "více" -> "mnoho"). Příslovce zůstává příslovcem ("nově" -> "nově", ne "nový"). Předpona "ne-" u přídavných jmen zůstává ("nemovitých" -> "nemovitý").
- Skloňovaná zájmena normalizuj na základní tvar ("tě" -> "ty", "ta" -> "ten", "ho" -> "on").
- Předložky, spojky, částice, číslovky, interpunkci a neohebná slova ponech beze změny; nevymýšlej jim jiné lemma ani jiný slovní druh ("pak" -> "pak", "trochu" -> "trochu", "nikoliv" -> "nikoliv").
- Velká písmena: základní tvar se píše malými písmeny, i když je tvar na začátku věty velký ("Cílem" -> "cíl", "Žila" -> "žít"). Velké písmeno má lemma jen tehdy, když se slovo píše vždy s velkým (vlastní jména: "Praha", "Albert", "Karlův"). Velikost písmen původního tvaru na lemma nemá vliv. Jediná výjimka: římské číslice mají lemma vždy velkými písmeny ("ii" -> "II").
- Diakritiku NEODSTRAŇUJ.
- Každý interpunkční znak je samostatný řádek, včetně závorek a uvozovek („ “ "). Spojení jako ")." nebo ")," rozděl na dva řádky. Číslo nebo římská číslice s tečkou ("19. století", "ii. fáze") jsou dva řádky: číslo a zvlášť tečka.
- Zachovej přesné pořadí slov a vytvoř samostatný řádek pro KAŽDÝ výskyt slova, i když se opakuje.
- Výstup je vždy ve formátu: původní_tvar^základní_tvar, jeden pár na řádek.
- Pokud narazíš na nový odstavec, vytvoř prázdný řádek.
- Nepiš nic navíc - žádné vysvětlení, žádné nadpisy, jen řádky.

Příklad 1:
Vstup: "Kvůli starým houslím se rodina dlouho hádala, ačkoli o ni už nikdo nestál."
Výstup:
Kvůli^kvůli
starým^starý
houslím^housle
se^se
rodina^rodina
dlouho^dlouho
hádala^hádat
,^,
ačkoli^ačkoli
o^o
ni^ona
už^už
nikdo^nikdo
nestál^stát
.^.

Příklad 2:
Vstup: "Nesnídal jsi nikdy dřív než v devět hodin, a tvoje sestry vstávaly ještě později."
Výstup:
Nesnídal^snídat
jsi^být
nikdy^nikdy
dřív^brzy
než^než
v^v
devět^devět
hodin^hodina
,^,
a^a
tvoje^tvůj
sestry^sestra
vstávaly^vstávat
ještě^ještě
později^pozdě
.^.

Příklad 3:
Vstup: "Cílem kapitoly je analyzovat „veřejný prostor“ vystavěného města. Dále se zjišťuje, jak byla zástavba v 19. století rozšířena (ii. fáze)."
Výstup:
Cílem^cíl
kapitoly^kapitola
je^být
analyzovat^analyzovat
„^„
veřejný^veřejný
prostor^prostor
“^“
vystavěného^vystavěný
města^město
.^.
Dále^daleko
se^se
zjišťuje^zjišťovat
,^,
jak^jak
byla^být
zástavba^zástavba
v^v
19^19
.^.
století^století
rozšířena^rozšířit
(^(
ii^II
.^.
fáze^fáze
)^)
.^.

Nyní zpracuj stejným způsobem následující věty:
            """;
    }

    @Override
    public String ENGLISH() {
        return """
You are a morphological lemmatization tool for a single English sentence. For every word in the given sentence, determine its base (dictionary) form.

Rules:
- For verbs, use the base/infinitive form without "to" (including forms of "be", "have", "do": "were" -> "be").
- For nouns, use the singular form. If a noun has no true singular in that sense (a plurale tantum, e.g. "clothes", "trousers", "scissors", "police"), keep it in its plural form - do not invent a singular.
- For adjectives and adverbs, use the positive degree, even for irregular/suppletive comparison (better/best -> good, worse -> bad, farther/further -> far).
- Normalize inflected pronouns to their citation/subject form (them -> they, whom -> who).
- Leave prepositions, conjunctions, articles, numbers and punctuation unchanged.
- Keep the base form's capitalization EXACTLY as in the original word - including ordinary words capitalized only because they start the sentence (e.g. "The" -> "The"). Do not lowercase them just because they are normally lowercase.
- Keep any diacritics/accents in the base form exactly as a standard dictionary entry would have them.
- Keep the exact word order and produce a separate line for EVERY occurrence of a word, even if it repeats.
- Output format: original_form^base_form, one pair per line.
- If you encounter a new paragraph, create a blank line between the other lines.
- Do not write anything else - no explanations, no headers, just the lines.

Example 1:
Input: "The children had worse teeth after eating the sweetest candies that their parents had bought them."
Output:
The^The
children^child
had^have
worse^bad
teeth^tooth
after^after
eating^eat
the^the
sweetest^sweet
candies^candy
that^that
their^their
parents^parent
had^have
bought^buy
them^they
.^.

Example 2:
Input: "Our best employees ran faster than everyone else, but nobody praised them because their clothes were dirtier than usual."
Output:
Our^Our
best^good
employees^employee
ran^run
faster^fast
than^than
everyone^everyone
else^else
,^,
but^but
nobody^nobody
praised^praise
them^they
because^because
their^their
clothes^clothes
were^be
dirtier^dirty
than^than
usual^usual
.^.

Now process the following sentences the same way:
            """;
    }

    @Override
    public String GERMAN() {
        return """
Du bist ein Werkzeug zur morphologischen Lemmatisierung eines einzelnen deutschen Satzes. Bestimme für jedes Wort im gegebenen Satz seine Grundform.

Regeln:
- Bei Verben verwende den Infinitiv (auch bei Hilfsverben wie "sein", "haben", "werden": "war" -> "sein"). Bei trennbaren Verben (Partikelverben wie "aufstehen", "aufsteigen", "wiederkommen") bekommt das Partikel-Wort ("auf", "wieder" usw.) seine eigene, unveränderte Grundform, und das eigentliche Verb bekommt seinen einfachen Infinitiv (z. B. bei "stand ... auf": "stand"-"stehen" und "auf"-"auf"), NICHT den zusammengesetzten Infinitiv "aufstehen".
- Bei Substantiven verwende den Nominativ Singular. Wenn ein Substantiv in dieser Bedeutung keinen Singular hat (Pluraletantum, z. B. "Eltern", "Leute", "Ferien"), verwende den Nominativ Plural statt einer erfundenen Singularform.
- Bei Adjektiven und Adverbien verwende den Positiv, auch bei unregelmäßiger Steigerung (besser/best- -> gut, lieber/liebst- -> gern, höher/höchst- -> hoch).
- Normalisiere flektierte Pronomen auf ihre Grundform (ihnen -> sie, ihm -> er).
- Normalisiere Artikel (der/die/das/den/dem/des ...) und Relativpronomen auf die Nominativ-Singular-Maskulin-Form "der".
- Präpositionen, Konjunktionen, Zahlen und Satzzeichen bleiben unverändert.
- Groß-/Kleinschreibung in der Grundform IMMER genauso wie im Original - auch am Satzanfang, wenn ein Wort nur wegen seiner Position groß geschrieben ist (z. B. eine Präposition, ein Artikel oder ein Verb am Satzanfang). Schreibe es nicht klein, nur weil es sonst normalerweise klein geschrieben wird.
- Entferne Umlaute und ß in der Grundform NICHT und ersetze sie nicht.
- Behalte die exakte Wortreihenfolge bei und erzeuge für JEDES Vorkommen eines Wortes eine eigene Zeile, auch bei Wiederholung.
- Ausgabeformat: Originalform^Grundform, ein Paar pro Zeile.
- Wenn Sie auf einen neuen Absatz stoßen, fügen Sie eine Leerzeile zwischen den anderen Zeilen ein.
- Schreibe nichts Zusätzliches - keine Erklärungen, keine Überschriften, nur die Zeilen.

Beispiel 1:
Eingabe: "An diesem Morgen stand die ganze Familie früh auf und stieg auf den höchsten Berg, den sie je gesehen hatten."
Ausgabe:
An^An
diesem^dieser
Morgen^Morgen
stand^stehen
die^der
ganze^ganz
Familie^Familie
früh^früh
auf^auf
und^und
stieg^steigen
auf^auf
den^der
höchsten^hoch
Berg^Berg
,^,
den^der
sie^sie
je^je
gesehen^sehen
hatten^haben
.^.

Beispiel 2:
Eingabe: "Die Eltern konnten die Prüfung ihrer Kinder kaum glauben, denn die Jungen hatten lieber gespielt als gelernt."
Ausgabe:
Die^Die
Eltern^Eltern
konnten^können
die^der
Prüfung^Prüfung
ihrer^ihr
Kinder^Kind
kaum^kaum
glauben^glauben
,^,
denn^denn
die^der
Jungen^Junge
hatten^haben
lieber^gern
gespielt^spielen
als^als
gelernt^lernen
.^.

Verarbeite nun den folgenden Satzen auf die gleiche Weise:
            """;
    }

    @Override
    public String SLOVAK() {
        return """
Si nástroj na morfologickú lematizáciu slovenského textu. Pre každé slovo v zadanej vete urč jeho základný (slovníkový) tvar.

Pravidlá:
- Pri slovesách použi infinitív (vrátane pomocných tvarov ako "byť", "mať": "som" -> "byť"). Zápor "ne-" v léme odstráň (napr. "nemohli" -> "môcť").
- Pri podstatných menách použi 1. pád jednotného čísla. Ak podstatné meno v danom význame nemá jednotné číslo (pomnožné podstatné meno, napr. "peniaze", "nožnice", "dvere"), použi 1. pád množného čísla, nikdy nie vymyslené jednotné číslo.
- Pri prídavných menách a príslovkách použi 1. stupeň, aj pri nepravidelnom stupňovaní (napr. "viac" -> "veľa", "vyššie" -> "vysoký").
- Skloňované zámená normalizuj na citátovú (základnú) formu.
- Predložky, spojky, častice, číslovky a interpunkciu ponechaj bez zmeny.
- Veľké písmeno v základnom tvare zachovaj VŽDY presne podľa originálu - aj na začiatku vety, keď je slovo veľké len vďaka pozícii (napríklad predložka alebo sloveso na začiatku vety), nie preto, že by sa inak písalo s malým písmenom.
- Diakritiku v základnom tvare NEODSTRAŇUJ.
- Zachovaj presné poradie slov a vytvor samostatný riadok pre KAŽDÝ výskyt slova, aj keď sa opakuje.
- Výstup je vždy vo formáte: pôvodný_tvar^základný_tvar, jeden pár na riadok.
- Ak na narazíš na nový odsek, vytvor prázdny riadok pre medzi ostatnými riadkami.
- Nepíš nič naviac - žiadne vysvetlenie, žiadne nadpisy, len riadky.

Príklad 1:
Vstup: "Najrýchlejší bežci dobehli do cieľa skôr, než diváci čakali."
Výstup:
Najrýchlejší^Rýchly
bežci^bežec
dobehli^dobehnúť
do^do
cieľa^cieľ
skôr^skoro
,^,
než^než
diváci^divák
čakali^čakať
.^.

Príklad 2:
Vstup: "Nemali sme včera dosť peňazí, a tvoji rodičia nám nechceli nič požičať."
Výstup:
Nemali^Mať
sme^byť
včera^včera
dosť^dosť
peňazí^peniaze
,^,
a^a
tvoji^tvoj
rodičia^rodič
nám^my
nechceli^chcieť
nič^nič
požičať^požičať
.^.

Teraz spracuj rovnakým spôsobom nasledujúce vety:
            """;
    }
    public String CZECH(String original) {
        return """
Jsi validátor a oprava výstupu z morfologické lemmatizace českého textu. Dostaneš dva vstupy: (1) PŮVODNÍ TEXT, celý odstavec, který může obsahovat jednu i více vět, a (2) VÝSLEDEK.

Tvým úkolem je návrh zkontrolovat a opravit, ne vytvořit lemmatizaci znovu od začátku. Pokud je řádek správně, přepiš ho beze změny. Pokud obsahuje chybu, oprav pouze ji.

U každého řádku ve tvaru původní_tvar^základní_tvar prověř zejména:
- Sloveso: je v infinitivu, bez záporky "ne-" a se stejným videm jako původní tvar ("prošlo" -> "projít", ne "procházet")? Lemma nikdy není v trpném rodu ("byl završen" -> "završit").
- Sloveso vs. přídavné jméno, oprav záměnu v obou směrech: trpné příčestí jako přívlastek je přídavné jméno ("vystavěného prostředí" -> "vystavěný"); tvary jako "studující" jsou přídavná jména; slovesná podstatná jména ("uvedením") zůstávají podstatnými jmény ("uvedení").
- Podstatné jméno: 1. pád jednotného čísla, u pomnožných jmen 1. pád množného čísla ("prázdninách" -> "prázdniny"). Zvlášť zkontroluj lemma opsané beze změny z tvaru, které je ve skutečnosti jiný pád ("lokalit" -> "lokalita").
- Přídavné jméno a příslovce: 1. stupeň bez "nej-", i při nepravidelném stupňování ("dále" -> "daleko", "déle" -> "dlouho", "výše" -> "vysoko"). Příslovce zůstává příslovcem ("nově" -> "nově"), "ne-" u přídavných jmen zůstává ("nemovitý").
- Zájmeno v základním tvaru ("tě" -> "ty", "ta" -> "ten"); neohebná slova beze změny a bez vymyšlených lemmat ("pak", "trochu", "nikoliv").
- Velká písmena: lemma se píše malými písmeny, i když je tvar na začátku věty velký ("Cílem" -> "cíl"). Velké písmeno má jen tehdy, když se slovo píše vždy s velkým (vlastní jména: "Praha", "Karlův"), a římské číslice mají lemma vždy velkými písmeny ("ii" -> "II"). Velikost písmen původního tvaru na lemma nemá vliv.
- Diakritika zůstává jako u standardního slovníkového lemmatu.
- Řádky: počet řádků odpovídá přesně počtu slov a interpunkčních znamének v PŮVODNÍM TEXTU, ve stejném pořadí. Každý interpunkční znak je samostatný řádek (")." jsou dva řádky), číslo nebo římská číslice s tečkou ("19.", "ii.") jsou také dva řádky. Pokud návrh dvě slova sloučil nebo nějaké vynechal či zdvojil, řádky rozděl nebo doplň.
- Konzistence: stejné slovo ve stejné funkci musí mít v celém odstavci stejné lemma.

Výstup: pouze opravené řádky ve formátu původní_tvar^základní_tvar, jeden pár na řádek, v původním pořadí, pro celý odstavec najednou. Nepiš žádné odůvodnění, žádné nadpisy, žádné oddělovače vět - jen finální opravené řádky.

Níže jsou příklady správného výsledku pro daný text (bez návrhu).

Příklad 1:
PŮVODNÍ TEXT: "V ii. etapě byla zástavba rozšířena. Vystavěné prostředí tak dále utvářelo studující (například v 19. století)."
VÝSLEDEK:
V^v
ii^II
.^.
etapě^etapa
byla^být
zástavba^zástavba
rozšířena^rozšířit
.^.
Vystavěné^vystavěný
prostředí^prostředí
tak^tak
dále^daleko
utvářelo^utvářet
studující^studující
(^(
například^například
v^v
19^19
.^.
století^století
)^)
.^.

Příklad 2:
PŮVODNÍ TEXT: "Univerzita Karlova uspořádala o prázdninách výstavu. Ředitel tě pak požádal o trochu času, nikoliv však o uvedení nemovitých památek."
VÝSLEDEK:
Univerzita^Univerzita
Karlova^Karlův
uspořádala^uspořádat
o^o
prázdninách^prázdniny
výstavu^výstava
.^.
Ředitel^ředitel
tě^ty
pak^pak
požádal^požádat
o^o
trochu^trochu
času^čas
,^,
nikoliv^nikoliv
však^však
o^o
uvedení^uvedení
nemovitých^nemovitý
památek^památka
.^.

Příklad 3:
PŮVODNÍ TEXT (prázdná řádka znamená nový odstavec):
"Praha se v 19. století rychle rozrůstala a nových lokalit stále přibývalo.

Nejvíce lokalit vzniklo v městském prostředí, nově i výše po proudu řeky."
VÝSLEDEK:
Praha^Praha
se^se
v^v
19^19
.^.
století^století
rychle^rychle
rozrůstala^rozrůstat
a^a
nových^nový
lokalit^lokalita
stále^stále
přibývalo^přibývat
.^.

Nejvíce^mnoho
lokalit^lokalita
vzniklo^vzniknout
v^v
městském^městský
prostředí^prostředí
,^,
nově^nově
i^i
výše^vysoko
po^po
proudu^proud
řeky^řeka
.^.

Příklad 4:
PŮVODNÍ TEXT: "Výzkum zaměřený na uvedená města byl završen. Nedocházelo k tomu dříve, a proto se dále nepokračovalo."
VÝSLEDEK:
Výzkum^výzkum
zaměřený^zaměřený
na^na
uvedená^uvedený
města^město
byl^být
završen^završit
.^.
Nedocházelo^docházet
k^k
tomu^ten
dříve^brzy
,^,
a^a
proto^proto
se^se
dále^daleko
nepokračovalo^pokračovat
.^.

Nyní zkontroluj a oprav následující:
PŮVODNÍ TEXT (prázdná řádka znamená nový odstavec):
""".concat(original).concat("""
NÁVRH:
                """);
    }
    public String ENGLISH(String original) {
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

Output: only the corrected lines in the format original_form^base_form, one pair per line, in the original order. Do not write any explanation, headers, or sentence separators - just the final corrected lines.

Example 1:
ORIGINAL TEXT: "The two brothers were the strongest players on the team. Their coach said the younger boys had tried harder than anyone."
CORRECTED RESULT:
The^The
two^two
brothers^brother
were^be
the^the
strongest^strong
players^player
on^on
the^the
team^team
.^.
Their^Their
coach^coach
said^say
the^the
younger^young
boys^boy
had^have
tried^try
harder^hard
than^than
anyone^anyone
.^.

Example 2:
ORIGINAL TEXT: "The children wore muddier shoes than usual, and their trousers looked worse too. Nobody noticed until the teacher saw them and laughed."
CORRECTED RESULT:
The^The
children^child
wore^wear
muddier^muddy
shoes^shoe
than^than
usual^usual
,^,
and^and
their^their
trousers^trousers
looked^look
worse^bad
too^too
.^.
Nobody^Nobody
noticed^notice
until^until
the^the
teacher^teacher
saw^see
them^they
and^and
laughed^laugh
.^.

Now check and correct the following:
ORIGINAL TEXT (blank line means new paragraph):
""" + original + """
DRAFT:
                """;
    }
    public String GERMAN(String original) {
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

Ausgabe: nur die korrigierten Zeilen im Format Originalform^Grundform, ein Paar pro Zeile, in der ursprünglichen Reihenfolge, für den ganzen Absatz auf einmal. Schreibe keine Begründung, keine Überschriften, keine Satztrenner - nur die finalen korrigierten Zeilen.

Beispiel 1:
URSPRÜNGLICHER TEXT: "Die Köchin kochte die besten Suppen in dem ganzen Dorf. Ihre Gäste kamen jede Woche wieder und lobten sie lauter als früher."
KORRIGIERTES ERGEBNIS:
Die^Die
Köchin^Köchin
kochte^kochen
die^der
besten^gut
Suppen^Suppe
in^in
dem^der
ganzen^ganz
Dorf^Dorf
.^.
Ihre^Ihre
Gäste^Gast
kamen^kommen
jede^jeder
Woche^Woche
wieder^wieder
und^und
lobten^loben
sie^sie
lauter^laut
als^als
früher^früh
.^.

Beispiel 2:
URSPRÜNGLICHER TEXT: "Der kleine Hund lief schneller als die großen Katzen."
KORRIGIERTES ERGEBNIS:
Der^Der
kleine^klein
Hund^Hund
lief^laufen
schneller^schnell
als^als
die^der
großen^groß
Katzen^Katze
.^.

Prüfe und korrigiere nun Folgendes:
URSPRÜNGLICHER TEXT (Leerzeile bedeutet neuer Absatz):
""" + original + """
ENTWURF:
               """;
    }
    public String SLOVAK(String original) {
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

Výstup: len opravené riadky vo formáte pôvodný_tvar^základný_tvar, jeden pár na riadok, v pôvodnom poradí, pre celý odsek naraz. Nepíš žiadne odôvodnenie, žiadne nadpisy, žiadne oddeľovače viet - len finálne opravené riadky.

Príklad 1:
PÔVODNÝ TEXT: "Naši najlepší predavači predali viac tovaru než minulý rok. Zákazníci si ale sťažovali, že ceny boli vyššie ako predtým."
OPRAVENÝ VÝSLEDOK:
Naši^Náš
najlepší^dobrý
predavači^predavač
predali^predať
viac^veľa
tovaru^tovar
než^než
minulý^minulý
rok^rok
.^.
Zákazníci^Zákazník
si^si
ale^ale
sťažovali^sťažovať
,^,
že^že
ceny^cena
boli^byť
vyššie^vysoký
ako^ako
predtým^predtým
.^.

Príklad 2:
PÔVODNÝ TEXT: "Bez nožníc si dievčatá nemohli ostrihať vlasy tak krátko, ako chceli."
OPRAVENÝ VÝSLEDOK:
Bez^Bez
nožníc^nožnice
si^si
dievčatá^dievča
nemohli^môcť
ostrihať^ostrihať
vlasy^vlas
tak^tak
krátko^krátko
ako^ako
chceli^chcieť
.^.

Teraz skontroluj a oprav nasledujúce:
PÔVODNÝ TEXT (prázdny riadok znamená nový odsek):
""" + original + """
NÁVRH:
                """;
    }
}