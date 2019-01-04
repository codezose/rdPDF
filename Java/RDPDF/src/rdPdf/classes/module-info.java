module rdPdf{
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.swing;
    requires java.logging;
    requires pdfbox.app;
    
    opens rd.pdf to javafx.fxml;
    exports rd.pdf;
}
