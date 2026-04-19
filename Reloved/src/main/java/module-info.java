module com.reloved {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.gluonhq.attach.storage;
    requires com.gluonhq.attach.util;
    requires com.gluonhq.charm.glisten;
    requires java.sql;

    opens com.reloved to javafx.fxml, com.gluonhq.charm.glisten, gluonhq.ignite;
    exports com.reloved;
}
