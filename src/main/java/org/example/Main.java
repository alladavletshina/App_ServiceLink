package org.example;
import java.awt.*;
import java.net.URI;
import java.util.Scanner;
import java.net.URISyntaxException;
import java.io.IOException;

public class Main {

    private final Scanner scanner;
    private String userId;

    public Main() {
        scanner = new Scanner(System.in);
    }

    private void startApplication(String userId, UrlShortMaker urlShortMaker) {
        while (true) {
            showMenu();
            int choice = getUserChoice();
            String shortUrl;
            this.userId = userId;
            urlShortMaker.deleteExpiredAndExhaustedShortUrls();

            switch (choice) {
                case 1:
                    System.out.println("Введите длинный URL для преобразования в короткую ссылку");
                    String longUrl = scanner.nextLine();

                    System.out.println("Установите лимит на количество переходов по ссылке");
                    int maxClicks = getUserChoice();

                    shortUrl = urlShortMaker.genеrateShortUrl(userId,maxClicks, longUrl);
                    System.out.println("Короткая ссылка: " + shortUrl);
                    break;

                case 2:
                    System.out.print("Введите короткую ссылку: ");
                    shortUrl = scanner.nextLine();
                    ShortUrl makerLongUrl = urlShortMaker.getLongUrl(userId, shortUrl);

                    if (makerLongUrl != null && makerLongUrl.getLeftClicks() > 0 && !makerLongUrl.isExpired()) {
                        String url = makerLongUrl.getLongUrl();
                        System.out.println("Исходный URL: " + url);
                        try {
                            Desktop.getDesktop().browse(new URI(url));
                            makerLongUrl.updateLeftClicks();
                        } catch (IOException | URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (makerLongUrl != null && makerLongUrl.getLeftClicks() <= 0 && !makerLongUrl.isExpired()) {
                        System.out.println("К сожалению, количество доступных переходов по этой ссылке закончилось. Пожалуйста, создайте новую короткую ссылку.");
                    } else {
                        System.out.println("Короткая ссылка недействительна или истек срок ее действия.");
                    }
                    System.out.println();
                    break;

                case 3:
                    System.out.print("Введите короткую ссылку: ");
                    shortUrl = scanner.nextLine();
                    urlShortMaker.deleteShortUrl(userId, shortUrl);
                    break;
                case 4:
                    urlShortMaker.printPersonalHistory(userId);
                    break;
                case 5:
                    System.out.print("Введите короткую ссылку: ");
                    shortUrl = scanner.nextLine();

                    ShortUrl shortUrlObj = urlShortMaker.getLongUrl(userId, shortUrl);
                    if (shortUrlObj == null) {
                        System.out.println("Короткая ссылка не найдена в истории.");
                        break;
                    }

                    System.out.println("Что вы хотите отредактировать:");
                    System.out.println("1. Лимит на количество переходов");
                    System.out.println("2. Срок действия ссылки (в днях)");
                    System.out.println("0. Вернуться в меню");

                    int choiceChanges = getUserChoice();
                    if (choiceChanges == 1) {
                        System.out.println("Обновите лимит на количество переходов по ссылке:");
                        int newMaxClicks = getUserChoice();
                        urlShortMaker.editMaxClicks(userId, shortUrl, newMaxClicks);
                    } else if (choiceChanges == 2) {
                        System.out.println("Обновить количество дней существования ссылки:");
                        int newMaxLifetimeDays = getUserChoice();
                        urlShortMaker.changeMaxLifetimeDays(userId, shortUrl, newMaxLifetimeDays);
                    } else if (choiceChanges == 0) {
                        System.out.println("Возвращаемся в меню.");
                        break;
                    } else {
                        System.out.println("Неверный выбор. Попробуйте снова.");
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Неправильный выбор. Попробуйте снова.");
            }
        }
    }

    private void exitApp() {
        System.out.println("\nДо свидание! Будем рады видеть Вас снова!");
        scanner.close();
        System.exit(0);
    }

    private void showMenu() {
        System.out.println("\nВыберите нужное действие:");
        System.out.println("1. Создать новую короткую ссылку");
        System.out.println("2. Перейти по короткой ссылке");
        System.out.println("3. Удалить свою ссылку");
        System.out.println("4. Посмотреть историю сохраненных ссылок");
        System.out.println("5. Изменить короткую ссылку");
        System.out.println("0. Вернуться в главное меню");
        System.out.print("Ваш выбор: ");
    }

    private int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Ошибка ввода. Пожалуйста, введите число.");
            return -1;
        }
    }

    private void greetingShowMenu() {
        System.out.println("\nГлавное меню:");
        System.out.println("1. У меня есть уникальный идентификационный номер");
        System.out.println("2. Нет уникального идентификационного номера");
        System.out.println("0. Выход из приложения");
        System.out.print("Ваш выбор: ");
    }

    private void greetingApplication(UrlShortMaker urlShortMaker) {
        Main main = new Main();
        while (true) {
            greetingShowMenu();
            int choice = getUserChoice();
            String userId;

            switch (choice) {
                case 1:
                    System.out.println("Введите уникальный идентификационный номер:");
                    userId = scanner.nextLine();

                    if (urlShortMaker.isUuidPresent(userId)) {
                        startApplication(userId, urlShortMaker);
                    } else {
                        System.out.println("История для данного UUID не найдена.");
                    }

                    break;
                case 2:
                    User user = new User();
                    userId = user.getUuid();
                    System.out.println("\nВам присвоен уникальный идентификационный номер: " + userId);
                    startApplication(userId, urlShortMaker);
                    break;
                case 0:
                    exitApp();
                    return;
                default:
                    System.out.println("Неправильный выбор. Попробуйте снова.");
            }
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        System.out.println("Добро пожаловать в сервис сокращения ссылок!");
        UrlShortMaker urlShortMaker = new UrlShortMaker();
        main.greetingApplication(new UrlShortMaker());
    }
}
