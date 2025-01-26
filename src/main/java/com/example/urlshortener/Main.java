package com.example.urlshortener;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.UUID;

public class Main {
    public static void main(String[] args) throws IOException {
        UrlShortener shortener = new UrlShortener();

        // Создаем HTTP-сервер, слушающий порт 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Обработчик для сокращения ссылки
        server.createContext("/shorten", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Получаем тело запроса
                    String query = new String(exchange.getRequestBody().readAllBytes());
                    if (!query.contains("originalUrl") || !query.contains("ttl")) {
                        String response = "Missing required parameters: originalUrl and ttl";
                        exchange.sendResponseHeaders(400, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                        return;
                    }

                    // Разбираем параметры
                    String[] params = query.split("&");
                    String originalUrl = params[0].split("=")[1];
                    long ttl = Long.parseLong(params[1].split("=")[1]);

                    // Генерируем сокращённую ссылку
                    UUID userId = UUID.randomUUID();
                    String shortUrl = shortener.shortenUrl(originalUrl, userId, ttl);

                    // Отправляем ответ клиенту
                    String response = "Shortened URL: " + shortUrl;
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                }
            }
        });

        // Обработчик для получения оригинальной ссылки
        server.createContext("/original", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    String query = exchange.getRequestURI().getQuery();
                    if (query == null || !query.contains("shortCode")) {
                        String response = "Missing required parameter: shortCode";
                        exchange.sendResponseHeaders(400, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                        return;
                    }

                    // Получаем shortCode
                    String shortCode = query.split("=")[1];
                    try {
                        String originalUrl = shortener.getOriginalUrl(shortCode);
                        String response = "Original URL: " + originalUrl;
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } catch (IllegalArgumentException e) {
                        String response = "Error: " + e.getMessage();
                        exchange.sendResponseHeaders(400, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                }
            }
        });

        // Запускаем сервер
        server.setExecutor(null); // Используем встроенный пул потоков
        System.out.println("Server started on port 8080");
        server.start();
    }
}