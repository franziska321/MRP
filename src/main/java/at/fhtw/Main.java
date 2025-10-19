package at.fhtw;

import at.fhtw.presentation.MediaPresentation;
import at.fhtw.server.HttpServerApp;

public class Main {
    public static void main(String[] args) {
    try {
        HttpServerApp.startServer();
    } catch (Exception e) {
        System.err.println("Fehler beim Starten des Servers:" + e.getMessage());
    }



    }
}
