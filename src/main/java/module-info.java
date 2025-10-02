module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.rometools.rome;
    requires javafx.web;

    requires org.apache.lucene.core;
    requires org.apache.lucene.queryparser;

    opens org.example to javafx.fxml;
    exports org.example;
}