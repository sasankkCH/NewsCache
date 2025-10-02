# NewsCache 📰

A Java-based desktop news aggregator application built with JavaFX for fetching, storing, and reading RSS feeds offline.

<img width="1593" height="933" alt="image" src="https://github.com/user-attachments/assets/d10c5eef-6208-4693-9949-b4b477b48511" />


## ✨ Features

* **Offline Reading:** Fetches and stores news articles locally in an SQLite database.
* **Multi-Feed Aggregation:** Pulls articles from multiple user-defined RSS feeds via a simple properties file.
* **Full-Text Search:** Instantly search through all downloaded articles using Apache Lucene.
* **Filtering:** Filter articles by category or view only bookmarked articles.
* **Bookmarking:** Save your favorite articles for easy access later.
* **Customizable UI:** Includes a responsive layout styled with CSS and a toggleable Dark Mode.

## 🛠️ Tech Stack

* **Language:** Java
* **Framework:** JavaFX
* **Build Tool:** Apache Maven
* **Libraries:**
    * **RSS Parsing:** Rome Tools
    * **Full-Text Search:** Apache Lucene
    * **Database:** SQLite-JDBC

## 🚀 Getting Started

To run this project locally:

1.  Clone the repository:
    ```bash
    git clone [https://github.com/your-username/NewsCache.git](https://github.com/your-username/NewsCache.git)
    ```
    *(Remember to replace "your-username" with your actual GitHub username)*

2.  Open the project in IntelliJ IDEA. Maven will automatically download the required dependencies.
3.  Modify the `src/main/resources/feeds.properties` file with your desired RSS feeds.
4.  Run the `App.java` file to start the application.

---
*This project was built with step-by-step guidance from Google's Gemini.*
