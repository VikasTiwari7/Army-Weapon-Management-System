/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scratchnest.fxml;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Communication Lab 10
 */
public class gunvalues {
    private final StringProperty epc;
    private final StringProperty date;
    private final StringProperty name;
    
    
     
    public gunvalues(String epc, String date, String name)
    {
        this.epc = new SimpleStringProperty(epc);
        this.date = new SimpleStringProperty(date);
        this.name=new SimpleStringProperty(name);
        
 }

   

   
   
    public String getepc() {
        return epc.get();
    }
     public void setepc(String value){
        epc.set(value);
    }
     
      public StringProperty epcProperty() {
        return epc;
    }

      public String getdate() {
        return date.get();
    }
     public void setdate(String value){
        date.set(value);
    }
     
      public StringProperty dateProperty() {
        return date;
    }
      public String getname(){
          return name.get();
      }
      public void setname(String value){
          name.set(value);
          
      }
      public StringProperty nameProperty(){
          return name;
      }
      
      

     
}

