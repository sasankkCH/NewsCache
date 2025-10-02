package org.example;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class DatabaseManager {

    // The database file will be created in our project's root directory.
    private static final String DB_URL = "jdbc:sqlite:NewsCache.db";

    // This is a Singleton pattern. It ensures we only ever have one instance
    // of the DatabaseManager, preventing potential database connection issues.
    private static DatabaseManager instance;

    // The constructor is private to enforce the Singleton pattern.
    private DatabaseManager() {
        initializeDatabase();
    }

    // This is the public method to get the single instance of this class.
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Establishes a connection to the database.
    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return conn;
    }

    // Creates the 'articles' table if it doesn't already exist.
    private void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS articles (" +
                "  link TEXT PRIMARY KEY," +
                "  title TEXT NOT NULL," +
                "  description TEXT," +
                "  content TEXT," +
                "  pubDate TEXT," +
                "  category TEXT," +
                "  isBookmarked INTEGER DEFAULT 0" + // Using INTEGER 0 for false, 1 for true
                ");";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating database table: " + e.getMessage());
        }
    }

    public void setBookmarkStatus(String link, boolean isBookmarked) {
        String sql = "UPDATE articles SET isBookmarked = ? WHERE link = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Store the boolean as an integer (1 for true, 0 for false)
            pstmt.setInt(1, isBookmarked ? 1 : 0);
            pstmt.setString(2, link);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating bookmark for link [" + link + "]: " + e.getMessage());
        }
    }

    public List<Article> getBookmarkedArticles() {
        String sql = "SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY pubDate DESC";
        List<Article> articles = new ArrayList<>();
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Article article = new Article();
                // (This is the same logic as your getAllArticles method)
                article.setLink(rs.getString("link"));
                article.setTitle(rs.getString("title"));
                article.setDescription(rs.getString("description"));
                article.setPubDate(rs.getString("pubDate"));
                article.setCategory(rs.getString("category"));
                article.setBookmarked(rs.getInt("isBookmarked") == 1);
                articles.add(article);
            }
        } catch (SQLException e) {
            System.err.println("Error getting bookmarked articles: " + e.getMessage());
        }
        return articles;
    }

    public Article getArticleByLink(String link) {
        String sql = "SELECT * FROM articles WHERE link = ?";
        Article article = null;

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, link);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                article = new Article();
                article.setLink(rs.getString("link"));
                article.setTitle(rs.getString("title"));
                article.setDescription(rs.getString("description"));
                article.setPubDate(rs.getString("pubDate"));
                article.setCategory(rs.getString("category"));
                article.setBookmarked(rs.getInt("isBookmarked") == 1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting article by link [" + link + "]: " + e.getMessage());
        }
        return article;
    }

    public List<Article> getArticlesByCategory(String category) {
        String sql = "SELECT * FROM articles WHERE category = ? ORDER BY pubDate DESC";
        List<Article> articles = new ArrayList<>();

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the value for the placeholder (?)
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();

            // Loop through the result set and create Article objects
            while (rs.next()) {
                Article article = new Article();
                article.setLink(rs.getString("link"));
                article.setTitle(rs.getString("title"));
                article.setDescription(rs.getString("description"));
                // ... (copy the rest of the fields from your getAllArticles method) ...
                article.setPubDate(rs.getString("pubDate"));
                article.setCategory(rs.getString("category"));
                article.setBookmarked(rs.getInt("isBookmarked") == 1);
                articles.add(article);
            }
        } catch (SQLException e) {
            System.err.println("Error getting articles by category [" + category + "]: " + e.getMessage());
        }
        return articles;
    }

    public void addArticle(Article article) {
        // The SQL statement uses placeholders (?) for security and efficiency.
        String sql = "INSERT OR IGNORE INTO articles(link, title, description, content, pubDate, category) VALUES(?,?,?,?,?,?)";

        // 'try-with-resources' automatically closes the connection and statement.
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Bind the Article object's data to the SQL statement's placeholders.
            pstmt.setString(1, article.getLink());
            pstmt.setString(2, article.getTitle());
            pstmt.setString(3, article.getDescription());
            pstmt.setString(4, article.getContent());
            pstmt.setString(5, article.getPubDate());
            pstmt.setString(6, article.getCategory());

            // Run the SQL statement to insert the data.
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding article [" + article.getTitle() + "]: " + e.getMessage());
        }
    }

    public List<Article> getAllArticles() {
        String sql = "SELECT * FROM articles ORDER BY pubDate DESC"; // Get newest articles first
        List<Article> articles = new ArrayList<>();

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Loop through the result set row by row
            while (rs.next()) {
                Article article = new Article();
                article.setLink(rs.getString("link"));
                article.setTitle(rs.getString("title"));
                article.setDescription(rs.getString("description"));
                article.setContent(rs.getString("content"));
                article.setPubDate(rs.getString("pubDate"));
                article.setCategory(rs.getString("category"));
                // Convert the stored integer (0 or 1) back to a boolean
                article.setBookmarked(rs.getInt("isBookmarked") == 1);

                articles.add(article);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all articles: " + e.getMessage());
        }
        return articles;
    }
}