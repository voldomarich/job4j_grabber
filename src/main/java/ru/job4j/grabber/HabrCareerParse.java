package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.Connection;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGES = 1;

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document page = connection.get();
        Elements rows = page.select(".collapsible-description__content");

        List<String> list = new ArrayList<>();
        rows.forEach(row -> {
                    Scanner scanner = new Scanner(row.firstElementSibling().text());
                    Pattern delimiter = Pattern.compile("</?\\w+>");
                    scanner.useDelimiter(delimiter);
                    scanner.forEachRemaining(list::add);
                });

        StringBuilder stringBuilder = new StringBuilder();
        list.forEach(s -> {
                    stringBuilder.append(s);
                    stringBuilder.append(System.lineSeparator());
                }
        );
        return stringBuilder.toString();
    }

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

                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));

                Element dateElement = row.select(".vacancy-card__date").first();
                String date = dateElement.child(0).attributes().get("datetime");
                HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
                LocalDateTime localDate = parser.parse(date);

                String description;
                try {
                    description = retrieveDescription(fullLink);
                    System.out.println(description);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.printf("%s %s %s %s%n", vacancyName, link, localDate, description);
        })
    ;}
    }
}
