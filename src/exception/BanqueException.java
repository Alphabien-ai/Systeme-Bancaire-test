package exception;

/**
 * Exception metier generale du systeme bancaire.
 */
public class BanqueException extends Exception {
    private static final long serialVersionUID = 1L;

    public BanqueException(String message) {
        super(message);
    }
}
