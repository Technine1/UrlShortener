package com.example.urlshortener;

import java.util.Scanner;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        // Создание экземпляра класса UrlShortener, а не Main
        UrlShortener shortener = new UrlShortener();
        Scanner scanner = new Scanner(System.in);

        UUID userId = UUID.randomUUID();
        System.out.println("Ваш уникальный идентификатор пользователя (UUID): " + userId);

        while (true) {
            try {
                System.out.println("\nВыберите действие:");
                System.out.println("1. Сократить ссылку");
                System.out.println("2. Получить оригинальную ссылку");
                System.out.println("3. Изменить лимит переходов");
                System.out.println("4. Удалить ссылку");
                System.out.println("5. Выход");

                System.out.print("Введите ваш выбор: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Очистка буфера

                switch (choice) {
                    case 1:
                        System.out.print("Введите оригинальную ссылку: ");
                        String originalUrl = scanner.nextLine();
                        System.out.print("Введите время жизни ссылки (в секундах): ");
                        long ttl = scanner.nextLong();
                        scanner.nextLine(); // Очистка буфера
                        String shortUrl = shortener.shortenUrl(originalUrl, userId, ttl);
                        System.out.println("Короткая ссылка: " + shortUrl);
                        break;

                    case 2:
                        System.out.print("Введите короткий код: ");
                        String shortCode = scanner.nextLine();
                        try {
                            // Вызов метода getOriginalUrl из класса UrlShortener
                            String retrievedUrl = shortener.getOriginalUrl(shortCode);
                            System.out.println("Оригинальная ссылка: " + retrievedUrl);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Ошибка: " + e.getMessage());
                        }
                        break;

                    case 3:
                        System.out.print("Введите короткий код: ");
                        shortCode = scanner.nextLine();
                        System.out.print("Введите новый лимит переходов: ");
                        int newLimit = scanner.nextInt();
                        scanner.nextLine(); // Очистка буфера
                        try {
                            shortener.updateClickLimit(shortCode, userId, newLimit);
                            System.out.println("Лимит переходов успешно обновлён.");
                        } catch (IllegalArgumentException e) {
                            System.out.println("Ошибка: " + e.getMessage());
                        }
                        break;

                    case 4:
                        System.out.print("Введите короткий код: ");
                        shortCode = scanner.nextLine();
                        try {
                            shortener.deleteUrl(shortCode, userId);
                            System.out.println("Ссылка успешно удалена.");
                        } catch (IllegalArgumentException e) {
                            System.out.println("Ошибка: " + e.getMessage());
                        }
                        break;

                    case 5:
                        System.out.println("Выход из программы.");
                        scanner.close();
                        return;

                    default:
                        System.out.println("Некорректный выбор. Попробуйте снова.");
                }
            } catch (Exception e) {
                System.out.println("Произошла ошибка: " + e.getMessage());
                scanner.nextLine(); // Очистка буфера для предотвращения зацикливания
            }
        }
    }
}