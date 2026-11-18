package fr.esgi.banque.exception; // ou le package approprié
public class CompteIntrouvableException extends RuntimeException {
    public CompteIntrouvableException(String message) { super(message); }
}