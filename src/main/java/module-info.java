module com.example.wordyl {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.wordyl to javafx.fxml;
    exports com.example.wordyl;
}