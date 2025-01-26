package com.example.urlshortener;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class UrlShortener {
    private final Map<String, UrlData> urlStore = new ConcurrentHashMap<>(); // Хранилище ссылок
    private final String baseDomain = "http://short.ly/"; // Базовый домен для коротких ссылок
    private final Properties config = new Properties();

    public UrlShortener() {
        loadConfig();
        scheduleLinkCleanup();
    }

    // Метод для сокращения ссылки
    public String shortenUrl(String originalUrl, UUID userId, long customTtlSeconds) {
        String shortCode = generateShortCode();
        long ttl = calculateEffectiveTtl(customTtlSeconds);
        UrlData urlData = new UrlData(originalUrl, userId, ttl);
        urlStore.put(shortCode, urlData);
        return baseDomain + shortCode;
    }

    // Метод для получения оригинальной ссылки
    public String getOriginalUrl(String shortCode) {
        UrlData urlData = urlStore.get(shortCode);
        if (urlData == null || urlData.isExpired()) {
            throw new IllegalArgumentException("Short code not found or expired");
        }
        urlData.incrementClickCount();
        if (urlData.getClickCount() > urlData.getClickLimit()) {
            urlStore.remove(shortCode);
            throw new IllegalArgumentException("Link click limit exceeded");
        }
        return urlData.getOriginalUrl();
    }

    // Метод для изменения лимита переходов
    public void updateClickLimit(String shortCode, UUID userId, int newLimit) {
        UrlData urlData = urlStore.get(shortCode);
        if (urlData == null || !urlData.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Short code not found or unauthorized access");
        }
        urlData.setClickLimit(newLimit);
    }

    // Метод для удаления ссылки
    public void deleteUrl(String shortCode, UUID userId) {
        UrlData urlData = urlStore.get(shortCode);
        if (urlData == null || !urlData.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Short code not found or unauthorized access");
        }
        urlStore.remove(shortCode);
    }

    // Генерация уникального кода для сокращенной ссылки
    private String generateShortCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // Загрузка конфигурации из файла
    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                config.load(input);
                System.out.println("Конфигурация успешно загружена.");
            }
        } catch (IOException e) {
            System.err.println("Не удалось загрузить файл конфигурации: " + e.getMessage());
        }
    }

    // Расчет времени жизни ссылки
    private long calculateEffectiveTtl(long customTtlSeconds) {
        long defaultTtl = Long.parseLong(config.getProperty("default.ttl.seconds", "86400"));
        return Math.min(customTtlSeconds, defaultTtl);
    }

    // Планировщик для автоматического удаления устаревших ссылок
    private void scheduleLinkCleanup() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            urlStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 1, 1, TimeUnit.HOURS);
    }

    // Вложенный класс для хранения данных о ссылке
    private static class UrlData {
        private final String originalUrl;
        private final UUID userId;
        private final long expiryTime;
        private int clickLimit;
        private int clickCount;

        public UrlData(String originalUrl, UUID userId, long ttlSeconds) {
            this.originalUrl = originalUrl;
            this.userId = userId;
            this.expiryTime = System.currentTimeMillis() + ttlSeconds * 1000;
            this.clickLimit = Integer.MAX_VALUE;
            this.clickCount = 0;
        }

        public String getOriginalUrl() {
            return originalUrl;
        }

        public UUID getUserId() {
            return userId;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }

        public void incrementClickCount() {
            clickCount++;
        }

        public int getClickCount() {
            return clickCount;
        }

        public int getClickLimit() {
            return clickLimit;
        }

        public void setClickLimit(int clickLimit) {
            this.clickLimit = clickLimit;
        }
    }
}