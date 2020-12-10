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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
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
    private DirectoryChooser directoryChoser;
    private HashMap<String, String> listViewPaths;
    private JDomPdf model;
    private Alert alertNoFileSelected;
    private Alert alertPageOutOfRange;
    private Alert alertNoMultiSelection;
    ImageView imageView = null;
    LinkedHashSet<String> ext, del;

    @FXML
    private Pagination paging;
    @FXML 
    private ListView<String> listView;
    @FXML
    private TextField extractPageTextField;
    @FXML
    private TextField deletePageTextField;
    @FXML
    private ScrollPane scrollPane;

    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createAndConfigureFileChooser();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewPaths = new HashMap<>();
        alertNoFileSelected = new Alert(AlertType.NONE, "No file selected!", ButtonType.OK);
        alertPageOutOfRange = new Alert(AlertType.NONE, "Page number out of the range!", ButtonType.OK);
        alertNoMultiSelection = new Alert(AlertType.NONE, "Select at least two files!", ButtonType.OK);
        ext = new LinkedHashSet<>();
        del = new LinkedHashSet<>();
    }
    
    
    private void putOnPagination(Pagination paging, File file){
        try{
            model = new JDomPdf(file);
            paging.setPageFactory((Integer index) -> {
                model = new JDomPdf(file);
                paging.setPageCount(model.numPages());
                ImageView imageView = new ImageView(model.getImage(index));
                imageView.setFitHeight(scrollPane.getHeight());
                imageView.setFitWidth(scrollPane.getWidth());
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setCache(true);
                return imageView; 
            });
        }finally{
            model.closeModel();
        }
    }
    
    private void createAndConfigureFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf", "*.PDF"));
        directoryChoser = new DirectoryChooser();
        directoryChoser.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
    }
    
    private List<String> getPaths(){
        List<String> files = new ArrayList<>();        
        listView.getItems().forEach((s) -> {
            files.add(listViewPaths.get(s));
        });
        return files;
    }
        
    /**
     * Returns a selected File-path or path to element of listView it this has only one element otherwise null.
     * @return 
     */
    private String getActiveFilePath(){
        String srcFile = listView.getSelectionModel().getSelectedItem();
        if(listView.getItems().size()==1)
            srcFile = listView.getItems().get(0);
        if(listView.getSelectionModel().getSelectedItems().size()>1)
           return null;
        // System.out.println("In getActiveFilePath");
        return listViewPaths.get(srcFile);
    }
    
    
    // FXML call
    
    
    @FXML
    private void handleMergeButtonAction(ActionEvent event) throws IOException {
        // mergePDFmain(listView.getItems());
        if(listView.getSelectionModel().getSelectedItems().size()<2){
            alertNoMultiSelection.showAndWait();
            return;
        }
        
        List<String> files = getPaths();
        
        File saveFile = fileChooser.showSaveDialog(paging.getScene().getWindow());
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setDestinationFileName(saveFile.getAbsolutePath());
        try {
            for(String s: files)
                pdfMerger.addSource(new File(s));
            pdfMerger.mergeDocuments(null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(jDomXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    void handleRemoveButtonAction(ActionEvent event) {
        List<String> selection = new ArrayList<>();
        selection.addAll(listView.getSelectionModel().getSelectedItems());
        for(String s: selection){
            listViewPaths.remove(s);
            listView.getItems().remove(s);
        }
    }

    @FXML
    void handleUpButtonAction(ActionEvent event) {
        List<Integer> selection = new ArrayList<>();
        selection.addAll(listView.getSelectionModel().getSelectedIndices());
        Collections.sort(selection);
        
        if(selection.contains(0)) return;        
        for(int i: selection){
            if(i>0){
                listView.getItems().add(i-1, listView.getItems().get(i));
                listView.getItems().remove(i+1);
            }
        }
        
        listView.getSelectionModel().clearSelection();

        selection.forEach((i) -> {
            listView.getSelectionModel().select(i-1);
        });
    }
    
    @FXML
    @SuppressWarnings("element-type-mismatch")
    void handleDownButtonAction(ActionEvent event) {
        List<Integer> selection = new ArrayList<>();
        selection.addAll(listView.getSelectionModel().getSelectedIndices());
        Collections.sort(selection, Collections.reverseOrder());
        
        if(selection.isEmpty() || selection.contains(listView.getItems().size()-1)) return;        

        for(int i: selection){
            if((i>=0) && (i<listView.getItems().size()-1)){
                listView.getItems().add(i+2, listView.getItems().get(i));
                listView.getItems().remove(i);
            }
        }
        
        listView.getSelectionModel().clearSelection();

        selection.forEach((i) -> {
            listView.getSelectionModel().select(i+1);
        });
    }
    
    void handleListViewOnDragOver(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        putOnPagination(paging, files.get(0));
        listView.getItems().add(files.get(0).getName());
        listViewPaths.put(files.get(0).getName(), files.get(0).getAbsolutePath());
    }
    
    @FXML
    void handleOnDragDrpped(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        File lastFile = null;
        for(File file: files)
            if(null!=file && "pdf".equalsIgnoreCase(file.getName().substring(file.getName().length() - 3, file.getName().length()))){
                listView.getItems().add(file.getName());
                listViewPaths.put(file.getName(), file.getAbsolutePath());
                lastFile = file;
            }
        if(lastFile != null)
            putOnPagination(paging, lastFile);
    }

    @FXML
    void handleOnDragOver(DragEvent event) {
        Dragboard board = event.getDragboard();
        if(board.hasFiles())
            event.acceptTransferModes(TransferMode.ANY);
    }
    
    @FXML
    private void handleChooseButtonAction(ActionEvent event) {
        final File file = fileChooser.showOpenDialog(paging.getScene().getWindow());
        if(null==file)
            return;
        putOnPagination(paging, file);
        listView.getItems().add(file.getName());
        listViewPaths.put(file.getName(), file.getAbsolutePath());
    }
    
    @FXML
    void handlePreviewButtonAction(ActionEvent event) {
        String srcFile = getActiveFilePath();
        if(srcFile == null){
            alertNoFileSelected.showAndWait();
            return;
        }
        
        try{
            model = new JDomPdf(Paths.get(srcFile));
            
            double pageNumberRounded = Math.pow(Math.ceil(Math.sqrt(model.numPages())), 2);

            double pW = scrollPane.getWidth(), pH = scrollPane.getHeight();
            int widthPage = (int)Math.sqrt(pW*pW/pageNumberRounded);
            int heighPage = (int)Math.sqrt(pH*pH/pageNumberRounded);
            int mW = (int)pW/widthPage, mH = (int)pH/heighPage;
            
            paging.setPageFactory((Integer index) -> {
                Group root = new Group();           
                VBox pageVBox = new VBox();
                HBox pHBs;
                HBox hBox;
                String style = "-fx-border-color: red;"
                        + "-fx-border-width: 1;"
                        + "-fx-border-style: dotted;";
                String antStyle = "-fx-border-color: white;"
                        + "-fx-border-width: 1;"
                        + "-fx-border-style: dotted;";
                
                
                paging.setPageCount(1);
                paging.setStyle("-fx-arrows-visible:false;");
                paging.setStyle("-fx-page-information-visible:false;");

                for(int h = 0; h <= mH; h++){  // use <= due to rounding else <
                    pHBs = new HBox();
                    for(int w = 0; w < mW; w++){
                        if(h*mW+w >= model.numPages()) break;
                        imageView = new ImageView(model.getImage(h*mW+w));
                        imageView.setFitWidth(widthPage);
                        imageView.setPreserveRatio(true);
                        imageView.setUserData(""+(h*mW+w+1));
                        hBox = new HBox();
                        hBox.getChildren().add(imageView);
                        pHBs.getChildren().add(hBox);
                        hBox.setOnMouseClicked(e -> {
                            HBox hb = (HBox)e.getSource();
                            String s = (String)hb.getChildren().get(0).getUserData();
                            if(ext.contains(s) || del.contains(s))
                                hb.setStyle(antStyle);
                            else
                                hb.setStyle(style);
                            updateFields(s);
                        });
                    }
                    pageVBox.getChildren().add(pHBs);
                }
                root.getChildren().add(pageVBox);
                return root;
            });
        }finally{
            model.closeModel();
        }
    }
    
    @FXML
    void handleViewButtonAction(ActionEvent event) {
        paging.setStyle("-fx-arrows-visible:true;");
        
        String srcFile = getActiveFilePath();
        if(srcFile == null){
            alertNoFileSelected.showAndWait();
            return;
        }
        
        putOnPagination(paging, new File(srcFile));
    }
    
    @FXML
    void handleSpritterButtonAction(ActionEvent event) {
        String srcFile = getActiveFilePath();
        if(srcFile == null){
            alertNoFileSelected.showAndWait();
            return;
        }
        
        File selectedDirectory = directoryChoser.showDialog(paging.getScene().getWindow());
        if(selectedDirectory==null) return;
        
        PDDocument document = null;
        Splitter splitter = new Splitter();
        try {
            document = PDDocument.load(new File(srcFile));
            List<PDDocument> pages = splitter.split(document);
            Iterator<PDDocument> iterator = pages.listIterator();
            
            // directoryChoser.setTitle("Speicherort");
            String directory = Paths.get(selectedDirectory.getAbsolutePath(), new File(srcFile).getName().replaceFirst("[.][^.]+$", "")).toString();
            
            int i = 1;
            while(iterator.hasNext()) {
               PDDocument pd = iterator.next();
               pd.save(directory+(i++)+".pdf");
            }
            document.close();
        } catch (IOException |NullPointerException ex) {
            Logger.getLogger(jDomXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("Multiple PDF’s created");
    }
    
    @FXML
    private void handleExtractButtonAction(ActionEvent event) throws IOException {
        List<Integer> extractingPages = new ArrayList<>();
        String pagesGroup[] = extractPageTextField.getText().split(",");
        for(String pg:pagesGroup){
            String pagesSubgroup[] = pg.split("-");
            if(pagesSubgroup.length==1)
                extractingPages.add(Integer.parseInt(pagesSubgroup[0].trim()));
            else
                extractingPages.addAll(IntStream
                        .range(Integer.parseInt(pagesSubgroup[0].trim()), 1+Integer.parseInt(pagesSubgroup[1].trim()))
                        .boxed().collect(Collectors.toList()));
        }

        String srcFile = getActiveFilePath();
        if(srcFile == null){
            alertNoFileSelected.showAndWait();
            return;
        }
        File userSelection = fileChooser.showSaveDialog(paging.getScene().getWindow());
        if(userSelection == null) return;
        
        PDDocument mergedPDF = new PDDocument();
        try (PDDocument document = PDDocument.load(new File(srcFile))) {
            if(Collections.max(extractingPages) > document.getNumberOfPages()
                    || Collections.min(extractingPages) < 1){
                alertPageOutOfRange.showAndWait();
                return;
            }
            extractingPages.forEach((i) -> {
                    mergedPDF.addPage(document.getPage(i-1));
            });

            mergedPDF.save(userSelection);
            mergedPDF.close();
        }    
    }

    @FXML
    private void handleDeleteButtonAction(ActionEvent event) throws IOException {
        List<Integer> extractingPages = new ArrayList<>();
        String pagesGroup[] = deletePageTextField.getText().replaceAll("\\s+", "").split(",");
        for(String pg:pagesGroup){
            String pagesSubgroup[] = pg.split("-");
            if(pagesSubgroup.length==1)
                extractingPages.add(Integer.parseInt(pagesSubgroup[0].trim()));
            else
                extractingPages.addAll(IntStream
                        .range(Integer.parseInt(pagesSubgroup[0].trim()), 1+Integer.parseInt(pagesSubgroup[1].trim()))
                        .boxed().collect(Collectors.toList()));
        }

        String srcFile = getActiveFilePath();
        if(srcFile == null){
            alertNoFileSelected.showAndWait();
            return;
        }
        
        File userSelection = fileChooser.showSaveDialog(paging.getScene().getWindow());
        if(userSelection == null) return;

        try (PDDocument currentDocument = PDDocument.load(new File(srcFile))) {
            if(Collections.max(extractingPages) > currentDocument.getNumberOfPages()
                    || Collections.min(extractingPages) < 1){
                alertPageOutOfRange.showAndWait();
                return;
            }

            Collections.sort(extractingPages, Collections.reverseOrder());
            extractingPages.forEach((i) -> {
                currentDocument.removePage((i-1));
            });

            currentDocument.save(userSelection);
        }
    }
    
    @FXML
    private void validExtractPageNumbers(){
        String pattern = "^(?!([ \\d]*-){2})\\d+(?: *[-,] *\\d+)*$";
        if(!extractPageTextField.getText().matches(pattern))
            extractPageTextField.setStyle("-fx-background-color:red;");
        else
            extractPageTextField.setStyle("-fx-background-color:white;");
    }
    
    @FXML
    private void validDeletePageNumbers(){
        String pattern = "^(?!([ \\d]*-){2})\\d+(?: *[-,] *\\d+)*$";
        if(!deletePageTextField.getText().matches(pattern))
            deletePageTextField.setStyle("-fx-background-color:red;");
        else
            deletePageTextField.setStyle("-fx-background-color:white;");
    }

    private void updateFields(String p) {
        if(!extractPageTextField.getText().isEmpty())
            ext.addAll(Arrays.asList(extractPageTextField.getText().split(",")));
        if(ext.contains(p))
            ext.remove(p);
        else       
            ext.add(p);
        extractPageTextField.setText(String.join(",", ext));
        
        if(!deletePageTextField.getText().isEmpty())
            del.addAll(Arrays.asList(deletePageTextField.getText().split(",")));
        if(del.contains(p))
            del.remove(p);
        else
            del.add(p);
        deletePageTextField.setText(String.join(",", del));
    }
}
