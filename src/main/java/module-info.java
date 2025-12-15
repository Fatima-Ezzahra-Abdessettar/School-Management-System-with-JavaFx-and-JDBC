module com.ensa.v2school.sm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires static lombok;
    requires java.management;
    requires javafx.graphics;
    requires java.desktop;

    exports com.ensa.v2school.sm;
    opens com.ensa.v2school.sm to javafx.fxml;
    opens com.ensa.v2school.sm.Controllers to javafx.fxml;
    opens com.ensa.v2school.sm.Models to javafx.base;


}
