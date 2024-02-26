package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
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
}
