/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdom;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 *
 * @author Dominique
 */
public class JDomPdf{
    private static final Logger logger = Logger.getLogger(JDomPdf.class.getName());
    
    
    private PDDocument document;
    private PDFRenderer renderer;
    
    JDomPdf(Path path){
        try {
            document = PDDocument.load(path.toFile());
            renderer = new PDFRenderer(document);
        } catch (IOException ex) {
            Logger.getLogger(JDomPdf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    JDomPdf(File f){
        try {
            document = PDDocument.load(f);
            renderer = new PDFRenderer(document);
        } catch (IOException ex) {
            Logger.getLogger(JDomPdf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeModel(){
        try {
            if( document != null )
                document.close();
        } catch (IOException ex) {
            Logger.getLogger(JDomPdf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int numPages() {
        return document.getPages().getCount();
    }   

    public Image getImage(int pageNumber) {
        BufferedImage pageImage;
        try {
            pageImage = renderer.renderImage(pageNumber);
        } catch (IOException ex) {
            throw new UncheckedIOException("PDFRenderer throws IOException", ex);
        }
        return SwingFXUtils.toFXImage(pageImage, null);
    }
    
    PDDocument getDocument(){
        return document;
    }
}
