package org.example;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;


public class UrlShortMaker {

    private Date dateCreation;
    private String shortUrlValue;

    private final Map<String, Map<String, ShortUrl>> history = new HashMap<>();

    public String genеrateShortUrl(String uuid, int maxClicks, String longUrl) {
        dateCreation = new Date();
        shortUrlValue = generateUniqueHash(longUrl + maxClicks + dateCreation + uuid);
        ShortUrl shortUrl = new ShortUrl(longUrl, maxClicks, shortUrlValue, dateCreation);
        saveShortUrl(uuid, shortUrl);
        return shortUrl.getShortUrl();
    }

    public ShortUrl getLongUrl(String uuid, String shortUrl) {
        Map<String,ShortUrl> map;
        ShortUrl longUrl = null;

        try {
            map = history.get(uuid);
            longUrl = map.get(shortUrl);

        } catch (Exception e) {
            System.out.println("Короткая ссылка не существует!");
        }
        return longUrl;
    }

    public String generateUniqueHash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(value.getBytes());
            String hash = Base64.getUrlEncoder().encodeToString(hashInBytes);

            return hash.substring(0, 5);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

    private void saveShortUrl(String uuid, ShortUrl shortUrl) {
        Map<String, ShortUrl> map = history.get(uuid);
        if (map != null) {
            map.put(shortUrl.getShortUrl(), shortUrl);
            history.put(uuid, map);
        } else {
            Map<String, ShortUrl> mapNew = new HashMap<>();
            mapNew.put(shortUrl.getShortUrl(), shortUrl);
            history.put(uuid, mapNew);
        }
    }

    public void deleteShortUrl(String uuid, String shortUrl) {
        Map<String, ShortUrl> map = history.get(uuid);
        if (map == null) {
            System.out.println("История для данного UUID не найдена.");
            return;
        }

        if (!map.containsKey(shortUrl)) {
            System.out.println("Короткая ссылка не найдена в истории.");
            return;
        }

        map.remove(shortUrl);
        if (map.isEmpty()) {
            history.remove(uuid);
        }

        System.out.println("Короткая ссылка успешно удалена.");
    }

    public void editMaxClicks(String uuid, String shortUrl, int newMaxClicks) {
        Map<String, ShortUrl> map = history.get(uuid);
        if (map == null) {
            System.out.println("История для данного UUID не найдена.");
            return;
        }

        ShortUrl shortUrlObj = map.get(shortUrl);
        if (shortUrlObj == null) {
            System.out.println("Короткая ссылка не найдена в истории.");
            return;
        }

        shortUrlObj.setClickTLimit(newMaxClicks);
        shortUrlObj.updateClicksAfterUpdateClickLimit();
        System.out.println("Лимит переходов обновлен до " + newMaxClicks + ".");
    }

    public void changeMaxLifetimeDays(String uuid, String shortUrl, int newMaxLifetimeDays) {
        Map<String, ShortUrl> map = history.get(uuid);
        if (map == null) {
            System.out.println("История для данного UUID не найдена.");
            return;
        }

        ShortUrl shortUrlObj = map.get(shortUrl);
        if (shortUrlObj == null) {
            System.out.println("Короткая ссылка не найдена в истории.");
            return;
        }

        shortUrlObj.setMaxLifetimeDays(newMaxLifetimeDays);
        System.out.println("Максимальное количество дней жизни ссылки обновлено до " + newMaxLifetimeDays + ".");
    }

    public void printHistory() {
        for (Map.Entry<String, Map<String, ShortUrl>> entry : history.entrySet()) {
            System.out.println("UUID: " + entry.getKey());

            for (Map.Entry<String, ShortUrl> urlEntry : entry.getValue().entrySet()) {
                ShortUrl shortUrl = urlEntry.getValue();
                System.out.printf("\tКороткая ссылка URL: %s\n\tДлинный URL: %s\n\tОставшееся кол-во переходов: %d\n\tДата создания ссылки: %s\n",
                        shortUrl.getShortUrl(),
                        shortUrl.getLongUrl(),
                        shortUrl.getLeftClicks(),
                        shortUrl.getDateCreation()
                );
            }
        }
    }

    public void printPersonalHistory(String uuid) {
        Map<String, ShortUrl> map = history.get(uuid);
        if (map == null) {
            System.out.println("История для данного UUID не найдена.");
            return;
        }

        System.out.println("История для UUID: " + uuid);
        for (Map.Entry<String, ShortUrl> urlEntry : map.entrySet()) {
            ShortUrl shortUrl = urlEntry.getValue();
            int remainingDays = shortUrl.daysRemaining();
            System.out.printf("\tКороткая ссылка URL: %s\n\tДлинный URL: %s\n\tОставшееся кол-во переходов: %d\n\tДата создания ссылки: %s\n\tОставшиеся дни: %d\n",
                    shortUrl.getShortUrl(),
                    shortUrl.getLongUrl(),
                    shortUrl.getLeftClicks(),
                    shortUrl.getDateCreation(),
                    remainingDays
            );
        }
    }

    public boolean isUuidPresent(String uuid) {
        return history.containsKey(uuid);
    }

    public void deleteExpiredAndExhaustedShortUrls() {
        Iterator<Map.Entry<String, Map<String, ShortUrl>>> outerIterator = history.entrySet().iterator();

        while (outerIterator.hasNext()) {
            Map.Entry<String, Map<String, ShortUrl>> outerEntry = outerIterator.next();
            String uuid = outerEntry.getKey();
            Map<String, ShortUrl> urls = outerEntry.getValue();

            Iterator<Map.Entry<String, ShortUrl>> innerIterator = urls.entrySet().iterator();

            while (innerIterator.hasNext()) {
                Map.Entry<String, ShortUrl> innerEntry = innerIterator.next();
                ShortUrl shortUrl = innerEntry.getValue();

                if (shortUrl.canBeDeleted()) {
                    innerIterator.remove(); // Удаление ссылки из вложенной карты
                    System.out.println("Ссылка удалена: " + shortUrl.getShortUrl());
                }
            }

            if (urls.isEmpty()) { // Если все ссылки были удалены, удаляем всю запись для данного UUID
                outerIterator.remove();
            }
        }
    }

}

