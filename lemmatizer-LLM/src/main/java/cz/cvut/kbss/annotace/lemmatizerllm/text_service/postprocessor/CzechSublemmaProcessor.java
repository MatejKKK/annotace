package cz.cvut.kbss.annotace.lemmatizerllm.text_service.postprocessor;

public class CzechSublemmaProcessor extends SublemmaProcessor {

    private final static String[] TABLE_1_LEMMA = {""};
    private final static String[] TABLE_1_SUBLEMMA = {""};

    private static String searchTable1(final String sublemma) {
        if (true)//todo
            return sublemma;
        for (int i = 0; i < TABLE_1_LEMMA.length; ++i) {
            if (TABLE_1_SUBLEMMA[i].equals(sublemma)) return TABLE_1_LEMMA[i];
        }

        return sublemma;
    }

    public CzechSublemmaProcessor() {}

    public String process(String sublemma) {
        String lemma = searchTable1(sublemma);
        if (!lemma.equals(sublemma)) return lemma;

        //todo

        return sublemma;
    }
}
