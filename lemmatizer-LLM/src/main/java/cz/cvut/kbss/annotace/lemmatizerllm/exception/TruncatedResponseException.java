package cz.cvut.kbss.annotace.lemmatizerllm.exception;

public class TruncatedResponseException extends RuntimeException {
    public TruncatedResponseException(String message) {
        super(message);
    }
}
