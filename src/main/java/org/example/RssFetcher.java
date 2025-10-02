package org.example;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RssFetcher {

    // Method to load the properties file from the resources folder.
    private Properties loadFeedProperties() {
        Properties properties = new Properties();
        try (InputStream input = RssFetcher.class.getClassLoader().getResourceAsStream("feeds.properties")) {
            if (input == null) {
                System.err.println("Sorry, unable to find feeds.properties");
                return properties;
            }
            // Load a properties file from class path
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }


    public Properties getFeedProperties() {
        return loadFeedProperties();
    }
    // Main method to fetch and parse all feeds listed in the properties file.
    public List<Article> fetchAllArticles() {
        List<Article> allArticles = new ArrayList<>();
        Properties feedProperties = loadFeedProperties();

        for (String category : feedProperties.stringPropertyNames()) {
            String url = feedProperties.getProperty(category);
            // This is inside the fetchAllArticles() method's for loop
            try {
                System.out.println("Fetching from: " + url);
                URL feedUrl = new URL(url);

                // --- ADD THIS CODE ---
                // Set a User-Agent to pretend we are a browser
                java.net.HttpURLConnection httpcon = (java.net.HttpURLConnection) feedUrl.openConnection();
                httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
                // --- END OF ADDED CODE ---

                SyndFeedInput input = new SyndFeedInput();
                // Now, build the feed from our custom connection
                SyndFeed feed = input.build(new XmlReader(httpcon));

                for (SyndEntry entry : feed.getEntries()) {
                    Article article = new Article();
                    article.setTitle(entry.getTitle());
                    article.setLink(entry.getLink());
                    if (entry.getDescription() != null) {
                        article.setDescription(entry.getDescription().getValue());
                    }
                    if (entry.getPublishedDate() != null) {
                        article.setPubDate(entry.getPublishedDate().toString());
                    }
                    article.setCategory(category);
                    allArticles.add(article);
                }
            } catch (Exception e) {
                System.err.println("Error fetching or parsing feed from " + url + ": " + e.getMessage());
            }
        }
        System.out.println("Fetched a total of " + allArticles.size() + " articles.");
        return allArticles;
    }
}
