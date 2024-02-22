package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.Connection;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGES = 5;

    public static void main(String[] args) throws IOException {

        for (int i = 1; i <= PAGES; i++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, PAGES, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();

                Element dateElement = row.select(".vacancy-card__date").first();
                String date = dateElement.child(0).attributes().get("datetime");
                HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
                LocalDateTime localDate = parser.parse(date);

                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", vacancyName, link, localDate);
            });
        }
    }
}
