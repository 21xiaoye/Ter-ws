package org.ws.exceptions;

public class IncompleteHandshakeException extends RuntimeException{
    private static final long serialVersionUID = 7906596804233893092L;

    /**
     * attribute which size of handshake would have been preferred
     */
    private final int preferredSize;

    /**
     * constructor for a IncompleteHandshakeException
     * <p>
     *
     * @param preferredSize the preferred size
     */
    public IncompleteHandshakeException(int preferredSize) {
        this.preferredSize = preferredSize;
    }

    /**
     * constructor for a IncompleteHandshakeException
     * <p>
     * preferredSize will be 0
     */
    public IncompleteHandshakeException() {
        this.preferredSize = 0;
    }

    /**
     * Getter preferredSize
     *
     * @return the preferredSize
     */
    public int getPreferredSize() {
        return preferredSize;
    }
}
