/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.stage.FileChooser;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 *
 * @author Dominique
 */
public class jDomXMLController implements Initializable {
    
    private FileChooser fileChooser ;
    
    @FXML private Pagination paging;
    @FXML private JDomPdf model;
    
//    @FXML private Button chooseButton;
//    @FXML private Button bindButton;
//    @FXML
//    private Button removeButton;
//
//    @FXML
//    private Button upButton;
//
//    @FXML
//    private Button downButton;

    @FXML  private ListView<String> listView;
    
    @FXML
    private void handleBindButtonAction(ActionEvent event) throws IOException {
        mergePDFmain(listView.getItems());
        //listView.getItems().addAll("<button type=\'button\'>CFile I!</button>");
    }
    
    @FXML
    void handleRemoveButtonAction(ActionEvent event) {
//        listView.getItems().removeAll(listView.getSelectionModel().getSelectedItems());
        ListView<Integer> lv= new ListView();
        lv.getItems().addAll(listView.getSelectionModel().getSelectedIndices().sorted());
        for(int i=lv.getItems().size()-1;i>=0;i--){
            listView.getItems().remove((int)lv.getItems().get(i));
//            System.out.println(lv.getItems().get(i));
        }
//        model.closeModel();
//        paging=null;
    }

    @FXML
    void handleUpButtonAction(ActionEvent event) {
        for(int i:listView.getSelectionModel().getSelectedIndices())
            mUpItem(i);
    
    }
    @FXML
    void handleDownButtonAction(ActionEvent event) {
        for(int i:listView.getSelectionModel().getSelectedIndices())
            mDownItem(i);
    }
    
//    @FXML
//    private void handleListViewOnDragDrop(DragEvent event) {
//        System.out.println("You clicked me!");
//        listView.getItems().addAll("drag drop");
//    }
    
    private void mUpItem(int i){
        if(i>0){
            listView.getItems().add(i-1, listView.getItems().get(i));
            listView.getItems().remove(i+1);
        }
    }
    
    private void mDownItem(int i){
        if((i>=0) && (i<listView.getItems().size()-1)){
            listView.getItems().add(i+2, listView.getItems().get(i));
            listView.getItems().remove(i);
        }
    }
    
    @FXML
    void handleListViewOnDragOver(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        putOnPagination(paging, files.get(0));
//        for(File file: files)
//            listView.getItems().add(file.getName());
        listView.getItems().add(files.get(0).getAbsolutePath());
    }
    
    @FXML
    private void handleChooseButtonAction(ActionEvent event) {
        final File file = fileChooser.showOpenDialog(paging.getScene().getWindow());
        if(null==file)
            return;
        putOnPagination(paging, file);
        listView.getItems().add(file.getAbsolutePath());
    }
    
    private void mergePDFmain(List<String> files) throws IOException{
        if(listView.getItems().size()<2)
            return;
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setDestinationFileName(files.get(0).substring(0, files.get(0).length() - 4)+"jDom.pdf");
        try {
            for(String s: files)
                pdfMerger.addSource(new File(s));
            pdfMerger.mergeDocuments(null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(jDomXMLController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        //System.out.println("PDF Documents merged to a single file");
    }
    
    private void putOnPagination(Pagination paging, File file){        
        
        paging.setPageFactory((Integer index) -> {
            model = new JDomPdf(Paths.get(file.getAbsolutePath()));
            paging.setPageCount(model.numPages());
            ImageView imageView = new ImageView(model.getImage(index));
            imageView.setFitHeight(760);
            imageView.setFitWidth(560);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);
            return imageView;
        });
        model.closeModel();
    }
    
    private void createAndConfigureFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("C:\\Users\\Dominique\\Desktop"));/*Paths.get(System.getProperty("user.home")).toFile()*/
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf", "*.PDF"));
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createAndConfigureFileChooser();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    
    @FXML
    void handlePreviewButtonAction(ActionEvent event) {
        if(null==listView.getSelectionModel().getSelectedItem() || ""==listView.getSelectionModel().getSelectedItem())
            return;
        putOnPagination(paging, new File(listView.getSelectionModel().getSelectedItem()));
    }
    
    @FXML
    void handleSpritterButtonAction(ActionEvent event) {
        String srcFile =listView.getSelectionModel().getSelectedItem();
        if((srcFile==null||"".equals(srcFile))&&listView.getItems().isEmpty())
            return;
        else if(!(srcFile==null||("".equals(srcFile))&&listView.getItems().isEmpty()))
            srcFile=listView.getSelectionModel().getSelectedItem();
        else
            srcFile=listView.getItems().get(0);
        //System.out.println(srcFile);
        PDDocument document = null;
        Splitter splitter = new Splitter();
        try {
            document = PDDocument.load(new File(srcFile));
            List<PDDocument> Pages = splitter.split(document);
            Iterator<PDDocument> iterator = Pages.listIterator();
            int i = 1;
            while(iterator.hasNext()) {
               PDDocument pd = iterator.next();
               pd.save(srcFile.substring(0, srcFile.length() - 4) + "_p_" + i++ + "_jDom.pdf");
            }
            document.close();
        } catch (IOException ex) {
            Logger.getLogger(jDomXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("Multiple PDF’s created");
    }
    
}
