module com.jchat {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.gluonhq.attach.storage;
    requires com.gluonhq.glisten.afterburner;
    requires java.sql;
    
    opens com.jchat to javafx.fxml, com.gluonhq.glisten.afterburner;
    exports com.jchat;
}