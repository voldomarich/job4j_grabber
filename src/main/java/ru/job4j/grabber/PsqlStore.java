package ru.job4j.grabber;

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

    private static Post createPost(ResultSet resultSet) throws SQLException {
        Post post = new Post();
        post.setId(resultSet.getInt(1));
        post.setTitle(resultSet.getString(2));
        post.setLink(resultSet.getString(3));
        post.setDescription(resultSet.getString(4));
        post.setCreated(resultSet.getTimestamp(5).toLocalDateTime());
        return post;
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into post (name, text, link, created) values (?, ?, ?, ?)"
                        + "on conflict (link) do nothing",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getLink());
            ps.setString(3, post.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
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
                    Post post = createPost(resultSet);
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
                    post = createPost(generatedKeys);
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
