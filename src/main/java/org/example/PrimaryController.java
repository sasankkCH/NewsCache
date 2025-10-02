package org.example;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.Properties;
import javafx.application.Platform;

// Notice we are importing the Article class we created
import org.example.SearchManager;

import java.util.List;

public class PrimaryController {

    // These @FXML variables link to the components in primary.fxml.
    // The variable names MUST EXACTLY MATCH the fx:id values from the FXML file.
    @FXML
    private Button fetchButton;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private ListView<String> categoryListView;

    @FXML
    private TableView<Article> articleTableView;

    // Note the types: The TableColumn is for an Article object, and will display a String.
    @FXML
    private TableColumn<Article, String> titleColumn;

    @FXML
    private TableColumn<Article, String> dateColumn;

    @FXML
    private WebView articleWebView;

    /**
     * This method is automatically called by JavaFX after the FXML file has been loaded.
     * It's the perfect place to initialize our UI with data.
     */

    private void filterArticlesByCategory(String category) {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        List<Article> articles;
        if (category == null || category.equals("All Articles")) {
            articles = dbManager.getAllArticles();
        } else if (category.equals("Bookmarked")) { // <-- New case
            articles = dbManager.getBookmarkedArticles();
        } else {
            articles = dbManager.getArticlesByCategory(category);
        }
        articleTableView.getItems().setAll(articles);
        System.out.println("Filtered view for category: " + category);
    }
    @FXML
    private void initialize() {
        System.out.println("PrimaryController is initializing...");

        // --- Step 1: Configure the Table Columns ---
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("pubDate"));

        // --- Responsive columns ---
        titleColumn.prefWidthProperty().bind(articleTableView.widthProperty().multiply(0.70));
        dateColumn.prefWidthProperty().bind(articleTableView.widthProperty().multiply(0.30));

        // --- Step 2: Add Listener for Article Selection ---
        articleTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newArticle) -> displayArticleContent(newArticle)
        );

        // --- Step 3: DYNAMICALLY Populate the Category List ---
        categoryListView.getItems().add("All Articles");
        categoryListView.getItems().add("Bookmarked"); // --- NEW BOOKMARK LOGIC ---

        RssFetcher fetcher = new RssFetcher();
        Properties feedProperties = fetcher.getFeedProperties();
        for (String categoryName : feedProperties.stringPropertyNames()) {
            categoryListView.getItems().add(categoryName);
        }

        // --- Step 4: Add a Listener to the Category List ---
        categoryListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> filterArticlesByCategory(newValue)
        );

        // --- Step 5: Select the first item by default ---
        categoryListView.getSelectionModel().selectFirst();

        // --- (Temporary) Indexing of existing articles ---
        System.out.println("Indexing existing articles in the background...");
        new Thread(() -> {
            SearchManager searchManager = SearchManager.getInstance();
            DatabaseManager dbManager = DatabaseManager.getInstance();
            List<Article> allArticles = dbManager.getAllArticles();
            for (Article article : allArticles) {
                searchManager.indexArticle(article);
            }
            System.out.println("Finished indexing " + allArticles.size() + " existing articles.");
        }).start();

        System.out.println("Initialization complete.");
    }
    @FXML
    private ToggleButton bookmarkToggleButton;
    @FXML
    private void handleBookmarkToggle() {
        Article selectedArticle = articleTableView.getSelectionModel().getSelectedItem();
        if (selectedArticle != null) {
            boolean isNowBookmarked = bookmarkToggleButton.isSelected();
            selectedArticle.setBookmarked(isNowBookmarked);
            DatabaseManager.getInstance().setBookmarkStatus(selectedArticle.getLink(), isNowBookmarked);
            System.out.println("Article '" + selectedArticle.getTitle() + "' bookmark status set to: " + isNowBookmarked);
        }
    }
    // Add this new helper method to PrimaryController.java
    private void displayArticleContent(Article article) {
        if (article != null) {
            String imageCss = "<style>img { max-width: 95%; height: auto; }</style>";
            String darkModeCss = "";

            // Check if the dark mode toggle is currently selected
            if (darkModeToggle.isSelected()) {
                darkModeCss = "<style>body { background-color: #2b2b2b; color: #e0e0e0; }</style>";
            }

            // Prepend all CSS to the article's description HTML
            String htmlContent = imageCss + darkModeCss + article.getDescription();
            articleWebView.getEngine().loadContent(htmlContent);

            // Also update the bookmark button's state
            bookmarkToggleButton.setSelected(article.isBookmarked());
            bookmarkToggleButton.setDisable(false);
        } else {
            // No article selected, clear the view and disable the bookmark button
            articleWebView.getEngine().loadContent("");
            bookmarkToggleButton.setSelected(false);
            bookmarkToggleButton.setDisable(true);
        }
    }
    @FXML
    private ToggleButton darkModeToggle;
    @FXML
    private void handleDarkModeToggle() {
        // Get the main scene from any component, like the toggle button itself
        Scene scene = darkModeToggle.getScene();

        // Always clear the old stylesheets before adding a new one
        scene.getStylesheets().clear();

        if (darkModeToggle.isSelected()) {
            // Apply the dark theme stylesheet
            scene.getStylesheets().add(App.class.getResource("dark-theme.css").toExternalForm());
            System.out.println("Switched to Dark Mode");
        } else {
            // Apply the light theme stylesheet (our original style.css)
            scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
            System.out.println("Switched to Light Mode");
        }
        Article selectedArticle = articleTableView.getSelectionModel().getSelectedItem();
        displayArticleContent(selectedArticle);
    }
    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (query == null || query.trim().isEmpty()) {
            // If the search bar is empty, just show the currently selected category
            filterArticlesByCategory(categoryListView.getSelectionModel().getSelectedItem());
            return;
        }

        System.out.println("Searching for: " + query);
        SearchManager searchManager = SearchManager.getInstance();
        List<Article> searchResults = searchManager.search(query);
        articleTableView.getItems().setAll(searchResults);
        System.out.println("Found " + searchResults.size() + " results.");
    }

    @FXML
    private void handleFetchFeeds() {
        System.out.println("Fetch button clicked!");

        // Disable the button to prevent the user from clicking it again while fetching
        fetchButton.setDisable(true);
        fetchButton.setText("Fetching...");

        // Create and start a new background thread for the network operations
        new Thread(() -> {

            // --- This code runs on a background thread ---
            RssFetcher fetcher = new RssFetcher();
            List<Article> fetchedArticles = fetcher.fetchAllArticles();

            DatabaseManager dbManager = DatabaseManager.getInstance();
            SearchManager searchManager = SearchManager.getInstance();

            for (Article article : fetchedArticles) {
                dbManager.addArticle(article);
                searchManager.indexArticle(article);
            }

            // --- When the background task is done, update the UI on the JavaFX Application Thread ---
            Platform.runLater(() -> {
                System.out.println("Fetching complete. Refreshing view.");

                // Re-enable the button and restore its text
                fetchButton.setText("Fetch All Feeds");
                fetchButton.setDisable(false);

                // Refresh the currently viewed category to show any new articles
                String selectedCategory = categoryListView.getSelectionModel().getSelectedItem();
                filterArticlesByCategory(selectedCategory);
            });

        }).start(); // This starts the new thread
    }

    /**
     * A helper method to load all articles from the database and refresh the table.
     */
    private void loadAllArticles() {
        System.out.println("Loading all articles from the database...");
        DatabaseManager dbManager = DatabaseManager.getInstance();
        List<Article> articles = dbManager.getAllArticles();

        // The TableView is backed by an ObservableList. setAll clears the list and adds all new items.
        articleTableView.getItems().setAll(articles);
        System.out.println("Article table refreshed with " + articles.size() + " articles.");
    }
}
