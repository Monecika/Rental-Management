package nl.saxion.paracomp.domain.messages;

public class ErrorMessage extends Message {
    public String error;

    public ErrorMessage() {
        super(MessageType.ERROR);
    }

    public ErrorMessage(String error) {
        this();
        this.error = error;
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "error='" + error + '\'' +
                '}';
    }
}