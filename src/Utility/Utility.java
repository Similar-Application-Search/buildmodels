/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Masud
 */
public class Utility {
public String executeCommnad(String shellFilePath){
    String command_output="";
    String command_output_error="";
    try {
      String line;
      Process p = Runtime.getRuntime().exec(shellFilePath);
      BufferedReader bri = new BufferedReader
        (new InputStreamReader(p.getInputStream()));
      BufferedReader bre = new BufferedReader
        (new InputStreamReader(p.getErrorStream()));
      
      while ((line = bri.readLine()) != null) {
          command_output = command_output+" "+line ;// output from this file is just a single line assumption
        //System.out.println(line);
      }
      bri.close();
      
      while ((line = bre.readLine()) != null) {
          command_output_error = command_output_error +"\n"+line;
        //System.out.println(line);
      }
      p.waitFor();
      //command_output = command_output;
      //System.out.println("Error: "+command_output_error);
    }
    catch (Exception err) {
      err.printStackTrace();
    }
    return command_output.trim();
}
public HashMap<String, Integer> sortByComparator(HashMap<String, Integer> unsortMap, final boolean order){
    List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());
    // Sorting the list based on values
    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>()
    {
        public int compare(Map.Entry<String, Integer> o1,Map.Entry<String, Integer> o2)
        {
            if (order)
            {
                return o1.getValue().compareTo(o2.getValue());
            }
            else
            {
                return o2.getValue().compareTo(o1.getValue());

            }
        }
    });

    // Maintaining insertion order with the help of LinkedList
    HashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
    for (Map.Entry<String, Integer> entry : list)
    {
        sortedMap.put(entry.getKey(), entry.getValue());
    }

    return sortedMap;
}
public HashMap<String, Double> sortByComparatorDouble(HashMap<String, Double> unsortMap, final boolean order)
{

    List<Map.Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());

    // Sorting the list based on values
    Collections.sort(list, new Comparator<Map.Entry<String, Double>>()
    {
        @Override
        public int compare(Map.Entry<String, Double> o1,
                Map.Entry<String, Double> o2)
        {
            if (order)
            {
                return o1.getValue().compareTo(o2.getValue());
            }
            else
            {
                return o2.getValue().compareTo(o1.getValue());

            }
        }
    });

    // Maintaining insertion order with the help of LinkedList
    HashMap<String, Double> sortedMap = new LinkedHashMap<>();
    for (Map.Entry<String, Double> entry : list)
    {
        sortedMap.put(entry.getKey(), entry.getValue());
    }

    return sortedMap;
}
public void writeOnFile_Integer(HashMap<String,Integer>wmap,String fileName)
{
   BufferedWriter bw = null;
   try {
    File file = new File(fileName);
    if (!file.exists()) {
       file.createNewFile();
    }
    FileWriter fw = new FileWriter(file);
    bw = new BufferedWriter(fw);
    for (String name: wmap.keySet()){
       String key =name;
       String value = wmap.get(name).toString();
       bw.write(key+"\t"+value+"\n");
    }
    //System.out.println("Write Successfull: "+fileName);

 } catch (IOException ioe) {
      ioe.printStackTrace();
   }
   finally
   { 
      try{
         if(bw!=null)
            bw.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
       }
   }
}
public void writeOnFile_doubleHash(HashMap<String,Double>wmap,String fileName)
{
   BufferedWriter bw = null;
   try {
    File file = new File(fileName);
     if (!file.exists()) {
        file.createNewFile();
     }
     FileWriter fw = new FileWriter(file);
     bw = new BufferedWriter(fw);
    for (String name: wmap.keySet()){
       String key =name;
       String value = wmap.get(name).toString();
       bw.write(key+"\t"+value+"\n");
    }
    //System.out.println("Write Successfull: "+fileName);
 } catch (IOException ioe) {
      ioe.printStackTrace();
   }
   finally
   { 
      try{
         if(bw!=null)
            bw.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
       }

   }

}
public HashMap<String,Double> readFromFile_doubleHash(String inputHashMapFile)
{
   HashMap<String,Double>readmap = new HashMap<>();
   BufferedReader br = null;
   try {
     br = new BufferedReader(new FileReader(inputHashMapFile));
    String line="";
    while((line = br.readLine()) !=null ){
        String[] line_parts = line.split("\t");
        readmap.put(line_parts[0], Double.parseDouble(line_parts[1]));
    }
 } catch (IOException ioe) {
      ioe.printStackTrace();
   }
   finally
   { 
      try{
         if(br!=null)
            br.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedReader"+ex);
       }

   }
   return readmap;
}
public void printDoubleHashMap(HashMap<String,Double> pmap){
    for(String key:pmap.keySet()){
        System.out.println(key+"\t"+pmap.get(key));
    }
}
public HashMap<String,Integer> shuffleHashMap(HashMap<String,Integer> unShuffledMap){
    HashMap<String,Integer> shuffledMap = new HashMap<>();
    List<String> keys = new ArrayList(unShuffledMap.keySet());
    Collections.shuffle(keys);
    for (String key : keys) {
        shuffledMap.put(key, unShuffledMap.get(key));
    }
    return shuffledMap;
}
// sample code for demonstrating how to recursively load files in a directory 
private void LoadDirectory(String folder, String suffix) {
    File dir = new File(folder);
    for (File f : dir.listFiles()) {
            if (f.isFile() && f.getName().endsWith(suffix)){
                    //do something with the file with extension "suffix"
            }
            else if (f.isDirectory()){
                 LoadDirectory(f.getAbsolutePath(), suffix);//recurse to get nested folder
            }

    }
}

}
