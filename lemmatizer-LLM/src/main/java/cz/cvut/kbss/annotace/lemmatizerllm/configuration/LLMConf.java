package cz.cvut.kbss.annotace.lemmatizerllm.configuration;

public record LLMConf (
        Api api,
        String delimiter,
        int maxSentenceLength,
        int maxParagraphLength,
        boolean usePostProcessing,
        String[] apiKeys
) {
    public LLMConf(String api, String delimiter, String maxSentenceLength, String usePostProcessing) {
        this(
                Api.valueOf(api),
                delimiter,
                Integer.parseInt(maxSentenceLength),
                (int) Math.floor(
                        3 * Math.E * Double.parseDouble(maxSentenceLength) * Math.log(2 * Math.E) / Math.log(Double.parseDouble(maxSentenceLength))
                ),
                usePostProcessing.equals("true"),
                new  String[]{}
        );
    }

    public LLMConf(String api, String delimiter, String maxSentenceLength, String usePostProcessing, String apiKeys) {
        this(
                Api.valueOf(api),
                delimiter,
                Integer.parseInt(maxSentenceLength),
                (int) Math.floor(Double.parseDouble(maxSentenceLength) * Math.pow(1.1, Double.parseDouble(maxSentenceLength))),
                usePostProcessing.equals("true"),
                apiKeys.split(",")
        );
    }
}