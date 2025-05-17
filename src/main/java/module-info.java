module com.example.toplutasimaprojesi {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.json;
    requires jdk.jsobject;
    requires java.desktop;

    opens com.example.toplutasimaprojesi to javafx.fxml;
    exports com.example.toplutasimaprojesi;
}
