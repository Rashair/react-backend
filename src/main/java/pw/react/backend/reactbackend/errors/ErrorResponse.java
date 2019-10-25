package pw.react.backend.reactbackend.errors;

public class ErrorResponse {
    private String message = "";
    private int code;
    private String additionalInfo = "";

    public ErrorResponse(String message, int code, String additionalInfo) {
        this.message = message;
        this.code = code;
        this.additionalInfo = additionalInfo;
    }

    public ErrorResponse(String message, int code) {
        this(message, code, "");
    }

    public ErrorResponse(int code) {
        this("", code, "");
    }


    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }
}
