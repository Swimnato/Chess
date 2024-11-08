public class InvalidSyntaxException extends Exception {
    private boolean showRawMessage;

    InvalidSyntaxException(String message) {
        super(message);
        showRawMessage = false;
    }

    InvalidSyntaxException(String message, boolean showRawMessage) {
        super(message);
        this.showRawMessage = showRawMessage;
    }

    boolean isShowRawMessage() {
        return showRawMessage;
    }
}
