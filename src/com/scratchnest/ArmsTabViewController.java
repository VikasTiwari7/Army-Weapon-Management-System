/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scratchnest;

import com.fazecast.jSerialComm.SerialPort;
import com.jfoenix.controls.JFXButton;
import com.scratchnest.fxml.gunvalues;
import com.thingmagic.ReadExceptionListener;
import com.thingmagic.ReadListener;
import com.thingmagic.Reader;
import com.thingmagic.ReaderException;
import com.thingmagic.SimpleReadPlan;
import com.thingmagic.TagProtocol;
import com.thingmagic.TagReadData;
import connectivity.dbcon;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * FXML Controller class
 *
 * @author jeev8
 */
public class ArmsTabViewController implements Initializable {
     @FXML
    private TableView<gunvalues> details;

    @FXML
    private TableColumn<gunvalues, String> tepc;

   private ObservableList<gunvalues> data;
    
    @FXML
    private JFXButton findbutton;

    
    @FXML
    private Label readerConnectionStatusLabel;
    
    @FXML
    private Label readerConnectionStatusLabel1;
    
    @FXML 
    private Label countrow;
    
    protected static Reader rfidReader = null;
    
    volatile static StringProperty tag = new SimpleStringProperty();
    
    protected static SerialPort rfidReaderSerialPort = null;
    
    protected static AudioInputStream audioInputStream;
    
    protected  static Clip clip; 
    @FXML
    private ToggleGroup monitoringServiceStartStopToggleGroup1;
    @FXML
    private TableColumn<gunvalues, String> tdate,tname;
    


    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
       
        startRfidReaderService();
        tag.setValue("NOT READING");
        readerConnectionStatusLabel1.textProperty().bind(tag);
        
        try{
            Class.forName("com.mysql.jdbc.Driver");
        }catch(ClassNotFoundException e){
            System.out.println("MySQL JDBC Driver not found !!");
            return;
        }
        System.out.println("MySQL JDBC Driver Registered!");
        Connection connection = null;
        try{
            connection = DriverManager
                .getConnection("jdbc:mysql://localhost:3306/JDBCDemo", "root", "password");
            System.out.println("SQL Connection to database established!");
 
        }catch (SQLException e){
            System.out.println("Connection Failed! Check output console");
        }
        
    }
    
   
    @FXML
    public void stopServiceFromUiButton(ActionEvent event){
      
        
        
        try{
            rfidReader.stopReading();
            rfidReader.destroy();
            rfidReader = null;
            readerConnectionStatusLabel.textProperty().unbind();
            readerConnectionStatusLabel.setText("Disconnected");
        }catch(Exception e){
            System.out.println("Stop Service Exception: " + e);
        }
    }
    
    @FXML
    public void startServiceFromUiButton(ActionEvent event){
        //String tag1= readerConnectionStatusLabel1.getText();
       
        try{
             //Connection con1 = dbcon.getconnect();
             //PreparedStatement ps = con1.prepareStatement("insert into record1 values(?)");
             // ps.setString(1, tag1);
            //int i = ps.executeUpdate();
            //System.out.println("rows effected=+i");
            
            if( (rfidReader == null) || (!rfidReader.hasContinuousReadStarted)){
                startRfidReaderService();
            }
        }catch(Exception e){
            System.out.println("Exception: " + e);
        }
             
    }
    
    private void startRfidReaderService(){
        show();
        count();
        
        rfidReaderConnectService connectService = new rfidReaderConnectService();
        
        rfidReaderReadingService readingService = new rfidReaderReadingService();
        
        connectService.restart();
        
        connectService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                //readerConnectionStatusLabel.textProperty().unbind();
                int[] antennaList = {2,3};
                try {
                    rfidReader.paramSet("/reader/region/id", Reader.Region.IN);
                    SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, true);
                    rfidReader.paramSet("/reader/read/plan", plan);
                    rfidReader.paramSet("/reader/gpio/outputList", antennaList);
                    readerConnectionStatusLabel.textProperty().unbind();
                    readerConnectionStatusLabel.textProperty().bind(readingService.messageProperty());
                    readingService.restart();
                } catch (Exception ex) {
                    System.out.println("Reader Setting Exception: " + ex);
                }
            }
        });
        
        readerConnectionStatusLabel.textProperty().bind(connectService.messageProperty());
    }
    
    private static void SimpleAudioPlayer(String filePath) throws UnsupportedAudioFileException, IOException, LineUnavailableException { 
        
        audioInputStream =  AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);
    } 

    
    private static class rfidReaderConnectService extends Service<Void> {
        
        String finalMessage = null;

        @Override
        protected Task createTask() {
  
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String connectedSerialPort = rfidSerialPortDetection();
                    if(connectedSerialPort.equals("Reader Not Connected")){
                        finalMessage = "Disconnected";
                        //return connectedSerialPort;
                    }else{
                        try{
                            rfidReader = Reader.create("tmr:///" + connectedSerialPort);
                            rfidReader.connect();
                            System.out.println(rfidReader.paramGet("/reader/version/model").toString());
                            finalMessage = "Connected";
                        }catch(Exception e){
                            finalMessage = "Not able to connect";
                        }
                        //return finalMessage;
                    }
                    updateMessage(finalMessage);

                    return null;
                }
            };
        }
    }
    
    private class rfidReaderReadingService extends Service<Void> {

        @Override
        protected Task createTask() {
  
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try{
                        ReadListener tagPrintListener = new ReadListener(){
                            
                            @Override
                            public void tagRead(Reader r, TagReadData tr){
                                System.out.println("Background read: " + tr.toString());
                                
                                
                                Platform.runLater(new Runnable() {
                                    
                                    @Override
                                    public void run() {
                                        tag.setValue(tr.toString());
                                        //tag.setValue(tr.toepcString);
                                       
                                        try{
                                            Connection con1 = dbcon.getconnect();
                                            java.sql.Timestamp date=new java.sql.Timestamp(new java.util.Date().getTime());
                                            //PreparedStatement ps1 = con1.prepareStatement(name);
                                            PreparedStatement ps = con1.prepareStatement("insert into record1 values(?,?)");
                                            ps.setString(1, tr.epcString());
                                            ps.setTimestamp(2, date);
                                           
                                            
                                            int i = ps.executeUpdate();
                                            System.out.println("rows effected=+i");
                                            show();
                                            count();
                                        }catch(Exception e)
                                        {}
                                        
                                    }
                                    
                                });
                                try{
                                    SimpleAudioPlayer("src/com/scratchnest/res/audio/singleAlert.wav");
                                    clip.start();
                                }catch(Exception e){
                                    System.out.println("Audio Exception: " + e);
                                }
                                //updateMessage("Not Reading");

                            }
                        };
                        rfidReader.addReadListener(tagPrintListener);
                        ReadExceptionListener exceptionListener = new ReadExceptionListener(){
                            @Override
                            public void tagReadException(com.thingmagic.Reader r, ReaderException re){
                                updateMessage("Disconnected");

                            }
                        };
                        rfidReader.addReadExceptionListener(exceptionListener);
                        rfidReader.startReading();
                        updateMessage("Connected");
                    }catch(Exception e){
                        System.out.println("Reader Exception: " + e);
                        updateMessage("Disconnected");
                    }
                    return null;
                }
            };
        }
    }
    
    private static String rfidSerialPortDetection(){
        
        SerialPort[] serialPortList = SerialPort.getCommPorts();
        String finalSelectedPort = null; //final reader com port
        //int readerConnectedCount = 0; //number of reader connected
        for(int i = 0; i < serialPortList.length; i++){
            if(serialPortList[i].toString().substring(0, 3).equals("M6E")){
                finalSelectedPort = serialPortList[i].getSystemPortName();
                rfidReaderSerialPort = serialPortList[i];
                return finalSelectedPort;
            }
        }
        finalSelectedPort = "Reader Not Connected";
        return finalSelectedPort;
    }
     public void show() {
       
            
            
             try {
            Connection conn = connectivity.dbcon.getconnect();
            data = FXCollections.observableArrayList();    
            
            ResultSet rss = (ResultSet) conn.createStatement().executeQuery("SELECT * from record2 NATURAL JOIN record1 GROUP BY DateandTime");
            while (rss.next()) {


                data.add(new gunvalues(rss.getString(1),rss.getString(2),rss.getString(3))); 

            }
             }
         catch (Exception e) {
            System.out.println(e);
        }
        tepc.setCellValueFactory(new PropertyValueFactory<>("epc"));
        tdate.setCellValueFactory(new PropertyValueFactory<>("date"));
        tname.setCellValueFactory(new PropertyValueFactory<>("name"));
      
        details.setItems(data);


        }
     public void count() {
        // String numberRow = null;
    
       try{
            ResultSet rs;
            Connection con1 = dbcon.getconnect();
            PreparedStatement ps = con1.prepareStatement("Select count(Epc) from record1");
            rs=ps.executeQuery();
           if(rs.next()){
           String sum=rs.getString("count(Epc)");
            countrow.setText(sum);
              //while(rs.next()){
                //numberRow = rs.getString("count(Epc)");
               // countrow.setText(numberRow);
                     }
            }catch (Exception ex){
                System.out.println(ex.getMessage());            
    
   
  
                }
               
                    
    }
}
   



