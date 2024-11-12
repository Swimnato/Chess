package commandparser;

public class InvalidSyntaxException extends Exception {
    private boolean showRawMessage;

    public InvalidSyntaxException(String message) {
        super(message);
        showRawMessage = false;
    }

    public InvalidSyntaxException(String message, boolean showRawMessage) {
        super(message);
        this.showRawMessage = showRawMessage;
    }

    public boolean isShowRawMessage() {
        return showRawMessage;
    }
}
