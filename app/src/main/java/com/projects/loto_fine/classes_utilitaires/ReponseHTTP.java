package com.projects.loto_fine.classes_utilitaires;

public class ReponseHTTP {
    private boolean isError;
    private String message;

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
