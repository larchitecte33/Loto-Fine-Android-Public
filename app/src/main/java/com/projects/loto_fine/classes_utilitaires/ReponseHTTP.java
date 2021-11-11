package com.projects.loto_fine.classes_utilitaires;

// Classe repr�sentant une r�ponse HTTP.
public class ReponseHTTP {
    private boolean isError; // Y a-t-il une erreur ?
    private String message; // Message contenu dans la r�ponse.

    // Accesseurs
    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
