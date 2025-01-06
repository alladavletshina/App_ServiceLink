package org.example;

import java.util.Date;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class ShortUrl {

    private String longUrl;
    private Date dateCreation;
    private int maxClicks;
    private int currentClicks = 0;
    private String shortUrl;
    private int maxLifetimeDays;

    public ShortUrl(String longUrl, int maxClicks, String shortUrlValue, Date dateCreation) {
        this.longUrl = longUrl;
        this.maxClicks = maxClicks;
        this.dateCreation = dateCreation;
        shortUrl = "https://" + shortUrlValue;
        loadMaxLifetimeDaysFromConfig();
    }

    public int getClickTLimit() {
        return maxClicks;
    }

    public void setClickTLimit(int newMaxClicks) {
        this.maxClicks = newMaxClicks;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setMaxLifetimeDays(int newMaxLifetimeDays) {
        this.maxLifetimeDays = newMaxLifetimeDays;
    }

    public int getMaxLifetimeDays() {
        return maxLifetimeDays;
    }

    public int daysRemaining() {
        Date currentDate = new Date();
        long timeDiff = currentDate.getTime() - dateCreation.getTime();
        long daysSinceCreation = timeDiff / (1000 * 60 * 60 * 24);
        return (int)(getMaxLifetimeDays() - daysSinceCreation);
    }

    public String getLongUrl() {
        return longUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void updateLeftClicks() {
        currentClicks += 1;
        System.out.println("\nКоличество кликов обновлено. Осталось: " + (getClickTLimit() - currentClicks));
    }

    public void updateClicksAfterUpdateClickLimit() {
        currentClicks = 0;
    }

    public int getLeftClicks() {
        return (getClickTLimit() - currentClicks);
    }

    public boolean isExpired() {
        Date currentDate = new Date();
        long timeDiff = currentDate.getTime() - dateCreation.getTime();
        long daysSinceCreation = timeDiff / (1000 * 60 * 60 * 24);
        return daysSinceCreation > getMaxLifetimeDays();
    }

    public boolean canBeDeleted() {
        return (isExpired() || getLeftClicks() <= 0);
    }

    private void loadMaxLifetimeDaysFromConfig() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Путь к файлу config.json относительно classpath
            String pathToConfig = "/config.json";
            File configFile = new File(this.getClass().getResource(pathToConfig).getFile());
            if (configFile.exists()) {
                Config config = objectMapper.readValue(configFile, Config.class);
                maxLifetimeDays = config.getLifetime().getDefaultValue();
            } else {
                maxLifetimeDays = 30; // Устанавливаем значение по умолчанию, если файл не найден
            }
        } catch (IOException ex) {
            maxLifetimeDays = 30; // Устанавливаем значение по умолчанию, если произошла ошибка при чтении
            System.err.println("Ошибка при чтении конфигурационного файла: " + ex.getMessage());
        }
    }

    public static class Config {
        private Lifetime lifetime;

        public Lifetime getLifetime() {
            return lifetime;
        }

        public void setLifetime(Lifetime lifetime) {
            this.lifetime = lifetime;
        }
    }

    public static class Lifetime {
        private int defaultValue;

        public int getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(int defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

}
