package cz.cvut.kbss.annotace.lemmatizerllm.text_service.postprocessor;

public class CzechSublemmaProcessor extends SublemmaProcessor {

    private static final String[][] TABLE = new  String[][]{
            {},
            {},
            {"k", "ku"}
    };

    public CzechSublemmaProcessor() {}

    @Override
    protected String[][] getTable() {
        return TABLE;
    }
}
