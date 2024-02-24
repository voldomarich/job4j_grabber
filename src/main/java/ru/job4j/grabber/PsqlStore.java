package ru.job4j.grabber;

import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private final Connection connection;

    public PsqlStore(Properties config) throws SQLException {
        try {
            Class.forName(config.getProperty("driver-class-name"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        connection = DriverManager.getConnection(
                config.getProperty("url"),
                config.getProperty("username"),
                config.getProperty("password")
        );
    }

    public static void main(String[] args) {
        Properties config = new Properties();
        try (InputStream input = PsqlStore.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            config.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (PsqlStore store = new PsqlStore(config)) {
            HabrCareerParse parser = new HabrCareerParse(new HabrCareerDateTimeParser());
            String sourceLink = "https://career.habr.com";
            String fullLink =
                    String.format("%s/vacancies/java_developer?page=1", sourceLink);
            List<Post> list = parser.list(fullLink);
            store.save(list.get(1));
            store.save(list.get(2));
            store.getAll();
            store.findById(2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into post (name, text, link, created) values (?, ?, ?, ?)"
                        + "on conflict on constraint post_link_key"
                        + "do"
                        + "update set name = (?) , text = (?), created  = (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getLink());
            ps.setString(3, post.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.setString(5, post.getTitle());
            ps.setString(6, post.getDescription());
            ps.setTimestamp(7, Timestamp.valueOf(post.getCreated()));
            ps.execute();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> list = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            String select = "select * from post;";
            try (ResultSet resultSet = statement.executeQuery(select)) {
                while (resultSet.next()) {
                    Post post = new Post();
                    post.setId(resultSet.getInt(1));
                    post.setTitle(resultSet.getString(2));
                    post.setLink(resultSet.getString(3));
                    post.setDescription(resultSet.getString(4));
                    post.setCreated(resultSet.getTimestamp(5).toLocalDateTime());
                    list.add(post);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Post findById(int id) {
        Post post = new Post();
        try (PreparedStatement ps = connection.prepareStatement(
                "select * from post where id = ?")) {
            ps.setInt(1, id);
            try (ResultSet generatedKeys = ps.executeQuery()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                    post.setTitle(generatedKeys.getString(2));
                    post.setDescription(generatedKeys.getString(3));
                    post.setLink(generatedKeys.getString(4));
                    post.setCreated(generatedKeys.getTimestamp(5).toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}
