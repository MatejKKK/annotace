package cz.cvut.kbss.annotace.lemmatizerllm.termextractor.promts;

public final class LongPromptTexts extends AbstractPromptTexts {
    @Override
    public String CZECH(String terms) {
        return """
Na základě seznamu pojmů najdi v odstavcích pod ním tyto pojmy.
Může se stát, že nějaký pojem v odstavcích prostě není.
Odstavce budou číslované per line a každý právě na jednom řádku v tomto úkolu i tvé odpovědi.
Každý pojem ze seznamu má své id, takže jej v textu vrať jako ^^pojem^^[id_pojmu]
Odstavce bez nalezeného pojmu mi nevracej.
Vrácený odstavec zachovej přesně tak, jak si ho dostal s vyjímkou označení pojmu (^^ a [id_pojmu]), ale pojem v odstavci nech v půvoním tvaru.
Seznam pojmů s jejich id:
            """
                .concat(terms)
                .concat("Odstavce ke zpracování jsou: \n");
    }

    @Override
    public String ENGLISH(String terms) {
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
    public String GERMAN(String terms) {
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
    public String SLOVAK(String terms) {
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
}