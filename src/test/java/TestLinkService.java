import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;
import org.example.UrlShortMaker;
import org.example.ShortUrl;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import java.time.Duration;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

public class TestLinkService {

    private UrlShortMaker urlShortMaker;

    @BeforeEach
    public void setup() {
        urlShortMaker = new UrlShortMaker();
    }

    //Этот метод отвечает за получение оригинального длинного URL по заданному короткому URL
    @Test
    public void testGetLongUrlWithValidShortUrl() {
        String uuid = "12345";
        String longUrl = "https://www.baeldung.com/java-9-http-client";
        int maxClicks = 10;

        String shortUrl = urlShortMaker.genеrateShortUrl(uuid, maxClicks, longUrl);

        ShortUrl storedShortUrl = urlShortMaker.getLongUrl(uuid, shortUrl);

        assertEquals(longUrl, storedShortUrl.getLongUrl());
        System.out.println("\nЭтот метод отвечает за получение оригинального длинного URL по заданному короткому URL");
        System.out.println(storedShortUrl.getLongUrl());
    }

    //Проверяет, что метод getLongUrl возвращает "Короткая ссылка не существует!", если короткий URL не найден в истории
    @Test
    public void testGetLongUrlWithInvalidShortUrl() {
        String uuid = "12345";
        String invalidShortUrl = "invalid";

        ShortUrl storedShortUrl = urlShortMaker.getLongUrl(uuid, invalidShortUrl);

        System.out.println("\nПроверяет, что метод getLongUrl возвращает \"Короткая ссылка не существует!\", если короткий URL не найден в истории");
        assertNull(storedShortUrl);
    }

    //Проверяет, что генерация нового короткого URL с одним и тем же uuid, но разными longUrl дает разные результаты.
    @Test
    public void testGenenerateShortUrlWithExistingUuidAndDifferentLongUrl() {
        String uuid = "12345";
        String firstLongUrl = "https://www.baeldung.com/java-9-http-client";
        String secondLongUrl = "https://google.com";
        int maxClicks = 10;

        String firstShortUrl = urlShortMaker.genеrateShortUrl(uuid, maxClicks, firstLongUrl);
        String secondShortUrl = urlShortMaker.genеrateShortUrl(uuid, maxClicks, secondLongUrl);

        assertNotEquals(firstShortUrl, secondShortUrl);
        System.out.println("\nПроверяет, что генерация нового короткого URL с одним и тем же uuid, но разными longUrl дает разные результаты.");
        System.out.println(firstShortUrl);
        System.out.println(secondShortUrl);
    }

    //Проверяет, что генерация нового короткого URL с одним и тем же uuid и longUrl дает разные результаты.
    @Test
    public void testGenenerateShortUrlWithExistingUuidAndLongUrl() throws InterruptedException {
        String uuid = "12345";
        String longUrl = "https://www.baeldung.com/java-9-http-client";
        int maxClicks = 10;

        String firstShortUrl = urlShortMaker.genеrateShortUrl(uuid, maxClicks, longUrl);

        Duration delay = Duration.ofSeconds(1); // Задержка в 1 секунду
        TimeUnit.MILLISECONDS.sleep(delay.toMillis());

        String secondShortUrl = urlShortMaker.genеrateShortUrl(uuid, maxClicks, longUrl);

        assertNotEquals(firstShortUrl, secondShortUrl);
        System.out.println("\nПроверяет, что генерация нового короткого URL с одним и тем же uuid и longUrl дает разные результаты.");
        System.out.println(firstShortUrl);
        System.out.println(secondShortUrl);
    }

    //проверяет функциональность метода deleteShortUrl, который отвечает за удаление записи о короткой ссылке из истории
    @Test
    public void testDeleteShortUrl() {
        String uuid = "12345";
        String longUrl = "http://www.example.com";
        int maxClicks = 10;

        String shortUrl = urlShortMaker.genеrateShortUrl(uuid, maxClicks, longUrl);

        ShortUrl storedShortUrl = urlShortMaker.getLongUrl(uuid, shortUrl);
        assertNotNull(storedShortUrl);

        urlShortMaker.deleteShortUrl(uuid, shortUrl);

        ShortUrl deletedShortUrl = urlShortMaker.getLongUrl(uuid, shortUrl);
        assertNull(deletedShortUrl);
    }

    //проверяет способность изменять лимит на количество переходов по короткой ссылке
    @Test
    public void testEditMaxClicks() {
        String uuid = "12345";
        String longUrl = "http://www.example.com";
        int initialMaxClicks = 10;
        int updatedMaxClicks = 20;

        String shortUrl = urlShortMaker.genеrateShortUrl(uuid, initialMaxClicks, longUrl);

        ShortUrl storedShortUrl = urlShortMaker.getLongUrl(uuid, shortUrl);
        assertNotNull(storedShortUrl);

        urlShortMaker.editMaxClicks(uuid, shortUrl, updatedMaxClicks);

        ShortUrl updatedShortUrl = urlShortMaker.getLongUrl(uuid, shortUrl);
        assertEquals(updatedMaxClicks, updatedShortUrl.getClickTLimit());
    }

    // Простетировано, что не осталось количество переходов по ссылке
    @Test
    public void testCase2Handling() {
        String uuid = "12345";
        String longUrl = "http://www.example.com";
        int maxClicks = 5;
        int initialLeftClicks = 0;

        String shortUrl = urlShortMaker.genеrateShortUrl(uuid, maxClicks, longUrl);

        ShortUrl storedShortUrl = urlShortMaker.getLongUrl(uuid, shortUrl);
        assertNotNull(storedShortUrl);

        storedShortUrl.setClickTLimit(initialLeftClicks);

        String inputShortUrl = shortUrl;

        ShortUrl makerLongUrl = urlShortMaker.getLongUrl(uuid, inputShortUrl);
        assertNotNull(makerLongUrl);

        if (makerLongUrl.getLeftClicks() > 0 && !makerLongUrl.isExpired()) {
            try {
                Desktop.getDesktop().browse(new URI(makerLongUrl.getLongUrl()));
                makerLongUrl.updateLeftClicks();
                assertEquals(makerLongUrl.getLeftClicks(), initialLeftClicks - 1);
            } catch (IOException | URISyntaxException e) {
                fail("Не удалось открыть URL.");
            }
        } else if (makerLongUrl.getLeftClicks() <= 0 || makerLongUrl.isExpired()) {
            assertTrue(makerLongUrl.getLeftClicks() <= 0 || makerLongUrl.isExpired());
            System.out.println("К сожалению, количество доступных переходов по этой ссылке закончилось. Пожалуйста, создайте новую короткую ссылку.");
        } else {
            assertFalse(true);
        }
    }

    // Простетировано, что успешно открывает браузер
    @Test
    public void testCase2Handling_1() {
        String uuid = "12345";
        String longUrl = "http://www.example.com";
        int maxClicks = 5;
        int initialLeftClicks = 2;

        String shortUrl = urlShortMaker.genеrateShortUrl(uuid, maxClicks, longUrl);

        ShortUrl storedShortUrl = urlShortMaker.getLongUrl(uuid, shortUrl);
        assertNotNull(storedShortUrl);

        storedShortUrl.setClickTLimit(initialLeftClicks);

        String inputShortUrl = shortUrl;

        ShortUrl makerLongUrl = urlShortMaker.getLongUrl(uuid, inputShortUrl);
        assertNotNull(makerLongUrl);

        if (makerLongUrl.getLeftClicks() > 0 && !makerLongUrl.isExpired()) {
            try {
                Desktop.getDesktop().browse(new URI(makerLongUrl.getLongUrl()));
                makerLongUrl.updateLeftClicks();
                assertEquals(makerLongUrl.getLeftClicks(), initialLeftClicks - 1);
            } catch (IOException | URISyntaxException e) {
                fail("Не удалось открыть URL.");
            }
        } else if (makerLongUrl.getLeftClicks() <= 0 || makerLongUrl.isExpired()) {
            assertTrue(makerLongUrl.getLeftClicks() <= 0 || makerLongUrl.isExpired());
            System.out.println("К сожалению, количество доступных переходов по этой ссылке закончилось. Пожалуйста, создайте новую короткую ссылку.");
        } else {
            assertFalse(true);
        }
    }
}
