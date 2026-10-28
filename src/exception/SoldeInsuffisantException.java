package exception;

/**
 * Levee lorsqu'un retrait depasse le solde disponible.
 * Indique si le titulaire est VIP (il peut alors utiliser le retrait au guichet).
 */
public class SoldeInsuffisantException extends BanqueException {
    private static final long serialVersionUID = 1L;
    private final boolean clientVIP;

    public SoldeInsuffisantException(String message, boolean clientVIP) {
        super(message);
        this.clientVIP = clientVIP;
    }

    public boolean isClientVIP() {
        return clientVIP;
    }
}
