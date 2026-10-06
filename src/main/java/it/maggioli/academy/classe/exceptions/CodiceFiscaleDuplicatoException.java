package it.maggioli.academy.classe.exceptions;

public class CodiceFiscaleDuplicatoException extends RuntimeException {
    public CodiceFiscaleDuplicatoException(String codiceFiscale) {
        super("Codice fiscale gia presente: " + codiceFiscale);
    }
}
