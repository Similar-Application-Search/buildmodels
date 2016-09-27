/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GitHubDataProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author masud
 */
public class ExtractCategory {
    public static void main(String[] args){
       ExtractCategory extractCategory = new ExtractCategory();
       String category =  extractCategory.getCategoryFromPlayUrl("https://play.google.com/store/apps/details?id=com.netflix.mediaclient");
       System.out.println("Category is = "+category);
    }
    
  public String getCategoryFromPlayUrl(String playURL){
    String playUrl_id = getGPStoreID(playURL);
    String category="";   
    try {
            String commandLine = "node getgenre.js "+playUrl_id;
           // String commandLine = "node getgenre.js com.educ8s.triviaquiz2015";
            String[] cmd = new String[]{"/bin/bash", "-c", commandLine};
            ProcessBuilder pb = new ProcessBuilder().command(cmd);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
               // System.out.println(line);
                if(line.startsWith("There was")){//not valid url
                    //System.out.println("Line:"+line);
                    break;
                }
                if(line.trim().equals("finish")){
                    p.destroy(); 
                    break;
                }
                if(category.isEmpty()){
                    category = line;
                }
                else{
                    category = category +":"+line;
                }
            }
            
        } catch (IOException ex) {
            Logger.getLogger(GitHubData.class.getName()).log(Level.SEVERE, null, ex);
        }
    return category;
}
public String getGPStoreID(String playURL){
    String playID=null;
    playID = playURL.trim().split("id=")[1];
    return playID;
}
}
