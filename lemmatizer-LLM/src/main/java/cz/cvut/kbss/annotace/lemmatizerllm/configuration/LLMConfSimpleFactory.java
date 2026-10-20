package cz.cvut.kbss.annotace.lemmatizerllm.configuration;

public class LLMConfSimpleFactory {

    public static LLMConf createLLMConf(String api, String delimiter, String maxSentenceLength, String usePostProcessing) {
        return new LLMConf(
                Api.valueOf(api),
                delimiter,
                Integer.parseInt(maxSentenceLength),
                (int) Math.floor(
                        3 * Math.E * Double.parseDouble(maxSentenceLength) * Math.log(2 * Math.E) / Math.log(Double.parseDouble(maxSentenceLength))
                ),
                usePostProcessing.equals("true"),
                new String[]{}
        );
    }

    public static LLMConf createLLMConf(String api, String delimiter, String maxSentenceLength, String usePostProcessing, String apiKeys) {
        return new LLMConf(
                Api.valueOf(api),
                delimiter,
                Integer.parseInt(maxSentenceLength),
                (int) Math.floor(Double.parseDouble(maxSentenceLength) * Math.pow(1.1, Double.parseDouble(maxSentenceLength))),
                usePostProcessing.equals("true"),
                apiKeys.split(",")
        );
    }
}
