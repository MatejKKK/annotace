package cz.cvut.kbss.annotace.lemmatizerllm.lemmatizer.promts;

@Deprecated
public final class ShortPromptTexts extends AbstractPromptTexts {

    @Override
    public String CZECH() {
        return "Dej mi z každého slova v textu jeho základní tvar (u sloves infititiv) tak, že každý řádek bude ve tvaru [původní_tvar_slova]-[zakldni_tvar_slova_bez_diakritiky] v původním pořadí. Řádky tvoř pokaždé nové, i když se dané slovo opakuje. Převeď vše na jednotné číslo v prvním pádu (přídavná jména a příslovce dej do prvního stupně), pokud to jde. Text je následující:\n";
    }

    @Override
    public String ENGLISH() {
        return "Give me the basic form (infinitive for verbs) of each word in the text, so that each line is in the form [original_word_form]-[basic_word_form_without_diacritics] in the original order. Create new lines each time, even if the given word is repeated. Get rid of conjugation and behave as text is in simple time. The text is as follows:\n";
    }

    @Override
    public String GERMAN() {
        return "Geben Sie mir die Grundform (Infinitiv bei Verben) jedes Wortes im Text an, sodass jede Zeile die Form [ursprüngliche Wortform]-[Grundform ohne diakritische Zeichen] in der ursprünglichen Reihenfolge aufweist. Erstellen Sie jedes Mal eine neue Zeile, auch wenn das jeweilige Wort wiederholt wird. Der Text lautet wie folgt:\n";
    }

    @Override
    public String SLOVAK() {
        return "Daj mi z každého slova v texte jeho základný tvar (u slovies infititív) tak, že každý riadok bude v tvare [pôvodná_tvar_slova]-[zakldni_tvar_slova_bez_diakritiky] v pôvodnom poradí. Riadky tvor zakaždým nové, aj keď sa dané slovo opakuje. Preveď všetko na jednotné číslo v prvém pádu (prídavná jména a príslovce daj do prvého stupně), pokud to jde. Text je nasledujúci:\n";
    }
}
