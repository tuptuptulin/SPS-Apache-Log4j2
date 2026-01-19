package com.vulnerable;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class VulnerableApp {

    private static final Logger log = LogManager.getLogger(VulnerableApp.class);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new MainHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/search", new SearchHandler());
        server.createContext("/api/user", new ApiHandler());
        server.start();

        System.out.println("Vulnerable app running on :8080 (Log4j 2.14.1)");
        log.info("Server started");
    }

    static class MainHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            log.info("Request from User-Agent: " + ex.getRequestHeaders().getFirst("User-Agent"));

            String html = "<html><body><h1>Vulnerable Application</h1>" +
                "<p>Log4j 2.14.1</p>" +
                "<form action='/search'><input name='q'><button>Search</button></form>" +
                "<form action='/login' method='post'><input name='username' placeholder='user'>" +
                "<input name='password' type='password'><button>Login</button></form>" +
                "</body></html>";

            ex.getResponseHeaders().set("Content-Type", "text/html");
            ex.sendResponseHeaders(200, html.length());
            ex.getResponseBody().write(html.getBytes());
            ex.getResponseBody().close();
        }
    }

    static class LoginHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if ("POST".equals(ex.getRequestMethod())) {
                BufferedReader r = new BufferedReader(new InputStreamReader(ex.getRequestBody()));
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) body.append(line);

                Map<String, String> params = parseForm(body.toString());
                String user = params.getOrDefault("username", "");

                log.info("Login attempt: " + user);

                String resp = "Login received for: " + user;
                ex.sendResponseHeaders(200, resp.length());
                ex.getResponseBody().write(resp.getBytes());
                ex.getResponseBody().close();
            }
        }
    }

    static class SearchHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String query = ex.getRequestURI().getQuery();
            String q = "";
            if (query != null && query.startsWith("q=")) {
                try { q = URLDecoder.decode(query.substring(2), "UTF-8"); } catch (Exception ignored) {}
            }

            log.info("Search: " + q);

            String resp = "Results for: " + q;
            ex.sendResponseHeaders(200, resp.length());
            ex.getResponseBody().write(resp.getBytes());
            ex.getResponseBody().close();
        }
    }

    static class ApiHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            log.info("API - User-Agent: " + ex.getRequestHeaders().getFirst("User-Agent"));
            log.info("API - Token: " + ex.getRequestHeaders().getFirst("X-Api-Token"));

            String resp = "{\"status\":\"ok\"}";
            ex.getResponseHeaders().set("Content-Type", "application/json");
            ex.sendResponseHeaders(200, resp.length());
            ex.getResponseBody().write(resp.getBytes());
            ex.getResponseBody().close();
        }
    }

    private static Map<String, String> parseForm(String data) {
        Map<String, String> map = new HashMap<>();
        for (String pair : data.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try { map.put(kv[0], URLDecoder.decode(kv[1], "UTF-8")); } catch (Exception ignored) {}
            }
        }
        return map;
    }
}