package com.mycompany.telemetriaproject;

import com.itextpdf.text.pdf.PdfWriter;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.control.Alert;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SNMPClientController {

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;
    
    @FXML
    private Button PDFButton;

    @FXML
    private TextArea textArea;

    @FXML
    private ProgressBar progressBar;

    private Timer timer;

    // Configuración de SNMP
    private static String community = "ingsoft";
    private static String ipAddress = "192.168.1.77"; // IP del servidor Ubuntu
    private static int port = 161;
    private static int timeout = 2000;
    private static int retries = 3;

    // OIDs para CPU, Memoria y Almacenamiento
    private static String contact = ".1.3.6.1.2.1.1.4.0";
    private static String serverName = ".1.3.6.1.2.1.1.5.0";
    private static String location = ".1.3.6.1.2.1.1.6.0";
    private static String systemUptime = ".1.3.6.1.2.1.1.3.0";
    private static String cpuOID = "1.3.6.1.4.1.2021.11.10.0";
    private static String oneMinuteLoad = ".1.3.6.1.4.1.2021.10.1.3.1";
    private static String fiveMinuteLoad = ".1.3.6.1.4.1.2021.10.1.3.2";
    private static String fifteenMinuteLoad = ".1.3.6.1.4.1.2021.10.1.3.3";
    private static String memTotalOID = ".1.3.6.1.4.1.2021.4.5.0";
    private static String memUsedOID = ".1.3.6.1.4.1.2021.4.6.0";
    private static String memAvailableOID = ".1.3.6.1.4.1.2021.4.11.0";
    private static String storagePathOID = "1.3.6.1.4.1.2021.13.15.1.1.1.10";
    private static String storageDeviceOID = "1.3.6.1.4.1.2021.13.15.1.1.2.10";
    private static String storageTotalOID = "1.3.6.1.4.1.2021.13.15.1.1.3.10";
    private static String storageAvailableOID = "1.3.6.1.4.1.2021.13.15.1.1.4.10";
    private static String storageUsedOID = "1.3.6.1.4.1.2021.13.15.1.1.5.10";
    private static String nicID = ".1.3.6.1.2.1.2.2.1.2.2";
    private static String speed = ".1.3.6.1.2.1.2.2.1.5.2";
    private static String macAddress = ".1.3.6.1.2.1.2.2.1.6.2";
    private static String bytesIn = ".1.3.6.1.2.1.2.2.1.10.2";
    private static String bytesOut = ".1.3.6.1.2.1.2.2.1.16.2";

    @FXML
    private Label lblContact;

    @FXML
    private Label lblPlace;

    @FXML
    private Button ClearButton;

    @FXML
    private void startAnalysis() {
        progressBar.setVisible(true); 
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> performSNMPAnalysis());
            }
        }, 0, 8000); // Ejecutar cada 8 segundos
    }

    @FXML
    private void stopAnalysis() {
        if (timer != null) {
            timer.cancel();
            progressBar.setVisible(false); 
        }
    }
    
    @FXML
    private void ClearAnalysis(ActionEvent event) {
        textArea.clear();
    }
    
    @FXML
    private void generatePDF() {
        if (textArea.getText().isEmpty()) {
            showAlert("Error", "El área de texto está vacía", "No se puede generar el PDF porque el área de texto está vacía.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file == null) {
            showAlert("Error", "No se seleccionó el archivo", "No se pudo generar el PDF porque no se seleccionó una ubicación para guardar el archivo.");
            return;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            PdfWriter writer = PdfWriter.getInstance(document, fileOutputStream);

            document.open();

            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Reporte de Análisis SNMP",
                    com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.TIMES_BOLD, 16));
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(title);

            document.add(new com.itextpdf.text.Paragraph("\n"));

            com.itextpdf.text.Paragraph content = new com.itextpdf.text.Paragraph(textArea.getText(),
                    com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.TIMES_ROMAN, 12));
            document.add(content);

            document.close();

            showAlertSucessfull("Éxito", "PDF generado correctamente", "El archivo PDF se ha generado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo generar el PDF", "Ocurrió un error al generar el archivo PDF.");
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showAlertSucessfull(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

        
    private void performSNMPAnalysis() {
        try {   
            Address targetAddress = new UdpAddress(ipAddress + "/" + port);
            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
            transport.listen();
            // Configuración del objetivo SNMP
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(targetAddress);
            target.setVersion(SnmpConstants.version2c);
            target.setRetries(retries);
            target.setTimeout(timeout);
            // Crear objeto SNMP
            Snmp snmp = new Snmp(transport);
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            lblContact.setText(getSNMPValue(snmp, target, contact));
            lblPlace.setText(getSNMPValue(snmp, target, location));
            StringBuilder result = new StringBuilder();
            result.append("=== Análisis realizado el ").append(timestamp).append(" ===\n\n");
            result.append("Datos del administrador del sistema:\n");
            result.append("Nombre: ").append(getSNMPValue(snmp, target, contact)).append("\n");
            result.append("Nombre del servidor: ").append(getSNMPValue(snmp, target, serverName)).append("\n");
            result.append("Ubicación: ").append(getSNMPValue(snmp, target, location)).append("\n\n");

            result.append("Tiempo transcurrido desde que inició el sistema operativo:\n");
            result.append("Tiempo de actividad del sistema: ").append(getSNMPValue(snmp, target, systemUptime)).append("\n\n");

            result.append("Carga de CPU:\n");
            result.append("CPU Usage: ").append(getSNMPValue(snmp, target, cpuOID)).append("%\n");
            result.append("Carga a 1 minuto (como cadena): ").append(getSNMPValue(snmp, target, oneMinuteLoad)).append("\n");
            result.append("Carga a 5 minutos (como cadena): ").append(getSNMPValue(snmp, target, fiveMinuteLoad)).append("\n");
            result.append("Carga a 15 minutos (como cadena): ").append(getSNMPValue(snmp, target, fifteenMinuteLoad)).append("\n\n");

            result.append("Memoria del sistema:\n");
            result.append("RAM total en la máquina: ").append(getSNMPValue(snmp, target, memTotalOID)).append(" KB\n");
            result.append("RAM utilizada: ").append(getSNMPValue(snmp, target, memUsedOID)).append(" KB\n");
            result.append("RAM disponible: ").append(getSNMPValue(snmp, target, memAvailableOID)).append(" KB\n\n");

            result.append("Almacenamiento en /:\n");
            result.append("Ruta donde el disco está montado: ").append(getSNMPValue(snmp, target, storagePathOID)).append("\n");
            result.append("Ruta del dispositivo para la partición: ").append(getSNMPValue(snmp, target, storageDeviceOID)).append("\n");
            result.append("Tamaño total del disco/partición (kBytes): ").append(getSNMPValue(snmp, target, storageTotalOID)).append(" KB\n");
            result.append("Espacio disponible en el disco: ").append(getSNMPValue(snmp, target, storageAvailableOID)).append(" KB\n");
            result.append("Espacio utilizado en el disco: ").append(getSNMPValue(snmp, target, storageUsedOID)).append(" KB\n\n");

            result.append("Interfaz de red:\n");
            result.append("ID de NIC: ").append(getSNMPValue(snmp, target, nicID)).append("\n");
            result.append("Velocidad: ").append(getSNMPValue(snmp, target, speed)).append("\n");
            result.append("Dirección MAC: ").append(getSNMPValue(snmp, target, macAddress)).append("\n");
            result.append("Bytes entrantes: ").append(getSNMPValue(snmp, target, bytesIn)).append("\n");
            result.append("Bytes salientes: ").append(getSNMPValue(snmp, target, bytesOut)).append("\n");

            result.append("\n-----------------------------------------------\n");

            textArea.appendText(result.toString());
            snmp.close();

        } catch (Exception e) {
            e.printStackTrace();
            textArea.setText("Error al realizar la consulta SNMP.");
        }
    }
    
    private String getSNMPValue(Snmp snmp, CommunityTarget target, String oid) {
        try {
            // Crear PDU (Protocol Data Unit)
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);

            // Enviar la solicitud SNMP
            ResponseEvent response = snmp.send(pdu, target);

            // Procesar la respuesta
            if (response != null && response.getResponse() != null) {
                return response.getResponse().get(0).getVariable().toString();
            } else {
                textArea.setText("Error: No se recibió respuesta");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No Data";
    }
}
