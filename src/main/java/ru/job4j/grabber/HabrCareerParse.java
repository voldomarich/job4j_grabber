package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGES = 5;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private static String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        Document document = null;
        try {
            document = connection.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert document != null;
        return document.select(".vacancy-description__text").text();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> result = new ArrayList<>();
        Connection connection = Jsoup.connect(link);
        System.out.printf("PAGE: %s%n", link);
        System.out.println();
        Document document;
        try {
            document = connection.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {

            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();

            String url = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));

            String description = retrieveDescription(url);

            Element dateElement = row.select(".vacancy-card__date").first();
            String date = dateElement.child(0).attributes().get("datetime");
            LocalDateTime localDate = dateTimeParser.parse(date);

            Post post = new Post();
            post.setTitle(vacancyName);
            post.setLink(url);
            post.setDescription(description);
            post.setCreated(localDate);
            result.add(post);
        });
        return result;
    }

    public static void main(String[] args) {
        for (int i = 1; i <= PAGES; i++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, i, SUFFIX);
            HabrCareerParse parser = new HabrCareerParse(new HabrCareerDateTimeParser());
            List<Post> postList = parser.list(fullLink);
            postList.forEach(System.out::println);
            System.out.println();
        }
    }
}
