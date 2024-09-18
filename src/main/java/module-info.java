module com.mycompany.telemetriaproject {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.telemetriaproject to javafx.fxml;
    exports com.mycompany.telemetriaproject;
    requires org.snmp4j;
    requires itextpdf;
}
