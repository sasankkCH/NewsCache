package org.example;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class SearchManager {
    // We'll store the Lucene index in a folder named "lucene-index"
    private static final String INDEX_DIR = "lucene-index";
    private static SearchManager instance;

    private SearchManager() {}

    public static synchronized SearchManager getInstance() {
        if (instance == null) {
            instance = new SearchManager();
        }
        return instance;
    }

    public void indexArticle(Article article) {
        // Create the configuration object first.
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

        // Now, the try-with-resources block only contains objects that need to be closed.
        try (Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
             IndexWriter writer = new IndexWriter(dir, config)) { // We use the config object here

            // Create a Lucene Document from our Article object
            Document doc = new Document();
            // We store the link so we can identify the article later
            doc.add(new StringField("link", article.getLink(), Field.Store.YES));
            // We index the title and description so they are searchable
            doc.add(new TextField("title", article.getTitle(), Field.Store.NO));
            doc.add(new TextField("description", article.getDescription(), Field.Store.NO));

            // updateDocument will replace an existing article with the same link,
            // or add it if it's new.
            writer.updateDocument(new Term("link", article.getLink()), doc);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // We will implement this search method in the next step
    public List<Article> search(String queryString) {
        List<String> foundLinks = new ArrayList<>();
        try (Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
             DirectoryReader reader = DirectoryReader.open(dir)) {

            IndexSearcher searcher = new IndexSearcher(reader);
            // This QueryParser will search in both the "title" and "description" fields
            QueryParser parser = new QueryParser("title", new StandardAnalyzer());
            Query query = parser.parse(queryString);

            TopDocs hits = searcher.search(query, 100); // Find the top 100 results
            for (ScoreDoc sd : hits.scoreDocs) {
                Document d = searcher.doc(sd.doc);
                foundLinks.add(d.get("link"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Now, retrieve the full Article objects from the database using the found links
        DatabaseManager dbManager = DatabaseManager.getInstance();
        return foundLinks.stream()
                .map(dbManager::getArticleByLink)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}