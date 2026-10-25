package cz.cvut.kbss.annotace.lemmatizerllm.configuration;

@Deprecated
public class LLMConfSimpleFactory {
    public static LLMConf createLLMConf(String api, String delimiter, String maxSentenceLength, String usePostProcessing) {
        return new LLMConf(
                Api.valueOf(api),
                new String[]{},
                delimiter,
                Integer.parseInt(maxSentenceLength),
                (int) Math.floor(
                        3 * Math.E * Double.parseDouble(maxSentenceLength) * Math.log(2 * Math.E) / Math.log(Double.parseDouble(maxSentenceLength))
                ),
                (usePostProcessing.equals("true")),
                false
        );
    }

    public static LLMConf createLLMConf(String api, String delimiter, String maxSentenceLength, String usePostProcessing, String apiKeys) {
        return new LLMConf(
                Api.valueOf(api),
                apiKeys.split(","),
                delimiter,
                Integer.parseInt(maxSentenceLength),
                (int) Math.floor(
                        3 * Math.E * Double.parseDouble(maxSentenceLength) * Math.log(2 * Math.E) / Math.log(Double.parseDouble(maxSentenceLength))
                ),
                (usePostProcessing.equals("true")),
                false
        );
    }
}
