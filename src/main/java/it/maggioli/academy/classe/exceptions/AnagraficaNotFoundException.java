package it.maggioli.academy.classe.exceptions;

public class AnagraficaNotFoundException extends RuntimeException {
    public AnagraficaNotFoundException(Long id) {
        super("Anagrafica non trovata con id: " + id);
    }
}
