/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity;

import java.sql.*;
public class dbcon {
    static Connection con;
    static public Connection getconnect(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            con=DriverManager.getConnection("jdbc:mysql://localhost:3306/attand?allowMutiQueries=true","root","root");


        }catch(Exception e){
            System.out.println(e);
        }return con;




    }
}
