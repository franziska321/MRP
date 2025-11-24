package at.fhtw.server;

import at.fhtw.server.handlers.LoginHandler;
import at.fhtw.server.handlers.MediaHandler;
import at.fhtw.server.handlers.ProfileHandler;
import at.fhtw.server.handlers.RegisterHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpServerApp {

    public static void startServer() throws IOException, InterruptedException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);


        //Routen
        server.createContext("/api/users/register", new RegisterHandler());
        server.createContext("/api/users/login", new LoginHandler());
        server.createContext("/api/users/profile", new ProfileHandler());
        server.createContext("/api/media", new MediaHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("Server läuft auf http://localhost:8080");
    }
}
