package fr.smart_waste.sapue.protocol;

/**
 * Exception thrown when protocol parsing or validation fails
 * Contains error code from Liste Code.md
 */
public class ProtocolException extends Exception {

    private final String errorCode;
    private final String errorMessage;

    public ProtocolException(String errorCode, String errorMessage) {
        super(errorCode + ": " + errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get formatted response for client
     * @return Error response string
     */
    public String getResponse() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "ProtocolException{" +
                "errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}