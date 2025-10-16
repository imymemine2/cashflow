module com.example {
    requires javafx.controls;
    requires javafx.fxml;

    // SQLiteアクセスに必要なjava.sqlモジュールを要求
    requires java.sql; 

    opens com.example to javafx.fxml;
    exports com.example;
}
