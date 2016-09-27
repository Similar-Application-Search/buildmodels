/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ProcessData;

import Utility.Utility;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 *
 * @author mr5ba
 */
public class EvaluationData {
    Tokenizer tokenizer ;
    Utility util;
   //public static String rootEvalFolder = "C:/Development/NetbeanProjects/data/sim_project/eval_data/";
   
   public static String rootEvalFolder = "I:/Dev/NetbeanProjects/data/similar_projects/";
   
   public static boolean ASC = true;
   public static boolean DESC = false;
public EvaluationData(){
    util = new Utility();
    try {
        tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
    } catch (IOException e) {
         e.printStackTrace();
    }
}   
public static void main(String[] args){
    EvaluationData evaluationData = new EvaluationData();
    evaluationData.runDataProcess(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]),Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    //evaluationData.runDataProcess("uniqueProjectData.txt", 50, 10,10, 60);
    
    //evaluationData.generateandSaveCategoryStats("uniqueProjectData.txt");
    //evaluationData.calculateAndSavedTokenWeight(rootEvalFolder+"ProjectsDataCollection.txt", rootEvalFolder+"saveTokenWeightPath");
    //evaluationData.UniformlyGenerateEvaluationDataWithCategory(args[0], args[1],args[2],args[3]);
    // evaluationData.generateExperimentDataWithStat(args[0], Integer.parseInt(args[1]),Integer.parseInt(args[2]),Integer.parseInt(args[3]));

 //evaluationData.generateExperimentDataWithStat("I:/Dev/NetbeanProjects/data/similar_projects/ProjectsDataCollection.txt", 90);
    //evaluationData.UniformlyGenerateEvaluationDataWithCategory(rootEvalFolder+"DocForWMD_stemmed.txt", rootEvalFolder+"projectDescriptions.txt",rootEvalFolder+"projectGitURL.txt",rootEvalFolder+"projectCategory.txt");
    //evaluationData.generateEvaluationDataWithCategory(rootEvalFolder+"DocForWMD_stemmed.txt", rootEvalFolder+"projectDescriptions.txt",rootEvalFolder+"projectGitURL.txt",rootEvalFolder+"projectCategory.txt");

 }
public void runDataProcess(String dataFile,int query_num, int topN_readme, int topN_mc, int cat_cutoff){
    splitTrainTestData(dataFile, query_num, cat_cutoff);
    calculateAndSavedTokenWeight("trainingProjectData.txt","none.txt");
    writeDataTypewise("trainingProjectData.txt", "trainDocForWMD.txt", "trainProjectDetails.txt", "trainProjectGitURL.txt", "trainProjectCategory.txt", topN_readme, topN_mc);
    writeDataTypewise("testingProjectData.txt", "testDocForWMD.txt", "testProjectDetails.txt", "testProjectGitURL.txt", "testProjectCategory.txt", topN_readme, topN_mc);
    System.out.println("Done collecting necessary data. Please proceed to next step.");
}
public void generateAndSaveTFIDFValue(){
    HashMap<String,Double> idf_map = new HashMap<>();
    HashMap<String,Double> df_map = new HashMap<>();
    HashMap<String,Double> ttf_map = new HashMap<>();
    idf_map = util.readFromFile_doubleHash("idf_map_rm.txt");
    util.printDoubleHashMap(idf_map);
    
}

public void splitTrainTestData(String pathInputProjectsData, int testQueryNumner, int category_freq_cutoff){
    HashMap <String, Integer> category_stats = readCategoryStat("category_stats.txt");
    
    BufferedReader fileReaderWMD = null;
    BufferedWriter bw_training = null;
    BufferedWriter bw_testing = null;
    HashMap<ArrayList<String>,String> documentMap = new HashMap<>(); 
    ArrayList<Double> docLenList = new ArrayList<>();
    try
     {
        fileReaderWMD = new BufferedReader(new FileReader(pathInputProjectsData));
        FileWriter fw_training = new FileWriter("trainingProjectData.txt");
        FileWriter fw_testing = new FileWriter("testingProjectData.txt");
        bw_training = new BufferedWriter(fw_training);
        bw_testing = new BufferedWriter(fw_testing);
        String lineWMD = "";
        
        String category="";
        int countquery=0;
        HashMap<String, Integer> datamap= new HashMap<>();
        
        while((lineWMD = fileReaderWMD.readLine()) !=null ){
            /*Integer nval = datamap.get(lineWMD);
            if(nval == null){
                nval=1;
            }
            else{
                nval++;
            }
            datamap.put(lineWMD, nval);*/
            String[] line_parts = lineWMD.split("\t");
            category = line_parts[3];
            Integer cat_freq = category_stats.get(category);
            if(cat_freq ==null){
                cat_freq=0;
            }
            if(cat_freq>=category_freq_cutoff && countquery<testQueryNumner){//take it as query , this time sequentially later differently
                bw_testing.write(lineWMD+"\n");
                countquery++;
            }else{
                bw_training.write(lineWMD+"\n");
            }
            //unique
            
        }
         //System.out.println("Size of data = "+datamap.size());
        util.shuffleHashMap(datamap);
        /*for(String dataline:datamap.keySet()){
            bw_training.write(dataline+"\n");
        }*/
     }
     catch (Exception e) {
         e.printStackTrace();
     }
    finally
     {
         try{
             if(fileReaderWMD!=null)
                fileReaderWMD.close();
          }catch(Exception ex){
              System.out.println("Error in closing the BufferedReader"+ex);
          }
         try{
             if(bw_training!=null)
                bw_training.close();
          }catch(Exception ex){
              System.out.println("Error in closing the BufferedWriter"+ex);
          }
         try{
             if(bw_testing!=null)
                bw_testing.close();
          }catch(Exception ex){
              System.out.println("Error in closing the BufferedWriter"+ex);
          }
     }
}
public HashMap <String, Integer> readCategoryStat(String categoryStatPath){
    BufferedReader fileReader = null;
    HashMap <String, Integer> category_stats = new HashMap<>();
    try{
        fileReader = new BufferedReader(new FileReader(categoryStatPath));
        String line="";
        String category="";
        int category_freq=0;
        while((line = fileReader.readLine()) !=null ){
            String[] line_parts = line.split("\t");
            category = line_parts[0];
            category_freq = Integer.parseInt(line_parts[1]);
            
            category_stats.put(category, category_freq);
        }
    }catch(Exception ex){
        ex.printStackTrace();
    }
    finally
    {
        try{
            if(fileReader!=null)
               fileReader.close();
         }catch(Exception ex){
             System.out.println("Error in closing the BufferedReader"+ex);
         }
    }
    return category_stats;
}
public void generateandSaveCategoryStats(String pathInputProjectsData){
    //split data based on category cut-off
    BufferedReader fileReader = null;
    HashMap <String, Integer> category_stats = new HashMap<>();
    try{
       fileReader = new BufferedReader(new FileReader(pathInputProjectsData));
       String line="";
       /*if((line = fileReader.readLine()) ==null){//skip header
            System.out.println("Not proper file with header");
        }*/
        String category="";
        while((line = fileReader.readLine()) !=null ){
            String[] line_parts = line.split("\t");
            category = line_parts[3];
            Integer nval = category_stats.get(category);
            if(nval==null){
                nval = 1;
            }else{
                nval = nval + 1;
            }
            category_stats.put(category, nval);
        }
        //now split file into train and test
        category_stats = util.sortByComparator(category_stats, DESC);
        util.writeOnFile_Integer(category_stats, "category_stats.txt");
    }catch(Exception ex){
        ex.printStackTrace();
    }
    finally
    {
        try{
            if(fileReader!=null)
               fileReader.close();
         }catch(Exception ex){
             System.out.println("Error in closing the BufferedReader"+ex);
         }
    }
}
public void generateExperimentDataWithStat(String pathInputProjectsData, int testQueryNumner, int cuttoff,int category_freq_cutoff){
    BufferedReader fileReaderWMD = null;
    HashMap<ArrayList<String>,String> documentMap = new HashMap<>();
    try
     {
        fileReaderWMD = new BufferedReader(new FileReader(pathInputProjectsData));

         String lineWMD = "";
         int countwhile=0;
         int countmap =0;
         HashMap <String, Integer> category_stats = new HashMap<>();
         if((lineWMD = fileReaderWMD.readLine()) ==null){//skip header
             System.out.println("Not proper file with header");
         }
         String category="";
         while((lineWMD = fileReaderWMD.readLine()) !=null ){
             ArrayList<String> docLine = new ArrayList<>();
             String[] line_parts = lineWMD.split("\t");
             //System.out.println("len = "+line_parts.length);

             String projectName = line_parts[0].trim();
             String gitURL = line_parts[1];

             category = line_parts[3];

             String descriptions = line_parts[4]+" "+line_parts[6];//add readme
             String descriptions_stemmed = line_parts[5]+" "+line_parts[7];//add readme

             String filteredStemmedDocForComparison = getTopNWords(descriptions_stemmed, cuttoff);
             
             
             docLine.add(projectName);
             docLine.add(descriptions);//remain all for testing
             docLine.add(filteredStemmedDocForComparison);
             docLine.add(gitURL);
             docLine.add(category);

             Integer nval = category_stats.get(category);
             if(nval==null){
                 nval = 1;
             }else{
                 nval = nval + 1;
             }
             category_stats.put(category, nval);

             documentMap.put(docLine,category);

         }
         for(String cat:category_stats.keySet()){
             System.out.println(cat+"\t"+category_stats.get(cat));
         }


         HashMap<ArrayList<String>,String>trainPart = new HashMap<>();
         HashMap<ArrayList<String>,String>testPart = new HashMap<>();
         int countquery=0;//max 
         //int testQueryNumner=50;
         System.out.println("DocMap Size = "+documentMap.size());
         System.out.println("Count while = "+countwhile);
         System.out.println("Count docmap = "+countmap);
         for(ArrayList<String> a:documentMap.keySet()){
             String project_category = documentMap.get(a);
             Integer cat_freq = category_stats.get(project_category);
             if(cat_freq>=category_freq_cutoff && countquery<testQueryNumner){
                 testPart.put(a, category);//take as query
                 countquery++;
             }
             else{
                 trainPart.put(a, category);
             }

         }
         writeDocumentOnFileWithCategoryStats(trainPart, "trainDocForWMD.txt", "trainProjectDetails.txt","trainProjectGitURL.txt","trainProjectCategory.txt");
         writeDocumentOnFileWithCategoryStats(testPart, "testDocForWMD.txt", "testProjectDetails.txt","testProjectGitURL.txt","testProjectCategory.txt");
     }
     catch (Exception e) {
         e.printStackTrace();
     }
    finally
     {
         try{
             if(fileReaderWMD!=null)
                fileReaderWMD.close();
          }catch(Exception ex){
              System.out.println("Error in closing the BufferedReader"+ex);
          }

     }
}
public String getTopNWordsBasedOnTfIdf(String document, int n){
    String[] doc_parts = document.split(" ");
    String topString="";
    for(int i=0;i<n && i<doc_parts.length;i++){
        topString = topString + " "+ doc_parts[i];
    }
    return topString.trim();
}

public void calculateAndSavedTokenWeight(String pathInputProjectsTrainingData, String saveTokenWeightPath){
    
    BufferedReader fileReader = null;
    HashMap<String, Token> tokenWeight = new HashMap<>();
    HashMap<String, Double> ttf_map = new HashMap<>();
    HashMap<String, Double> df_map = new HashMap<>();
    HashMap<String, Double> idf_map = new HashMap<>();
    
    HashMap<String, Double> ttf_map_mc_name = new HashMap<>();
    HashMap<String, Double> df_map_mc_name = new HashMap<>();
    HashMap<String, Double> idf_map_mc_name = new HashMap<>();
    
    double totaltoken= 0.0;
    double totaldocs =0.0;
    
    double totaltoken_mc_name= 0.0;
    try{
        fileReader = new BufferedReader(new FileReader(pathInputProjectsTrainingData));
        String line = "";
        
        while((line = fileReader.readLine()) !=null ){
            totaldocs++;
           String[] line_parts = line.split("\t");
           
           //calculate TTF, DF, TF-IDF for readme
           if(line_parts.length>=8){
            String readMeContent_stem = line_parts[7];
            String[] readMeToken = tokenizer.tokenize(readMeContent_stem);//used open-nlp tokenizer
            HashSet<String> tokenset = new HashSet();
            for(String rm_token:readMeToken){
                Double n = ttf_map.get(rm_token);
                n = (n == null) ? 1 : ++n;
                ttf_map.put(rm_token, n);
                tokenset.add(rm_token);
                totaltoken++;
            }
            for(String dftoken:tokenset){//hashset contain unique key as token, so unique per document
                Double n = df_map.get(dftoken);
                n = (n == null) ? 1 : ++n;
                df_map.put(dftoken, n);
            }
           }
           //calculate TTF, DF, TF-IDF for method and class name from source code
           /*#########################################################*/
           if(line_parts.length>=10){
            String mcnameContent_stem = line_parts[9];//stemmed method and class name
            String[] mcnameToken = tokenizer.tokenize(mcnameContent_stem);//used open-nlp tokenizer
            HashSet<String> tokenset_mc = new HashSet();
            for(String mc_token:mcnameToken){
                Double n = ttf_map_mc_name.get(mc_token);
                n = (n == null) ? 1 : ++n;
                ttf_map_mc_name.put(mc_token, n);
                tokenset_mc.add(mc_token);
                totaltoken_mc_name++;
            }
            for(String dftoken_mc:tokenset_mc){//hashset contain unique key as token, so unique per document
                Double n = df_map.get(dftoken_mc);
                n = (n == null) ? 1 : ++n;
                df_map_mc_name.put(dftoken_mc, n);
            }
           }
           /*#########################################################*/
        }
        //calculate idf for readme
        for(String idftoken:df_map.keySet()){
            double token_df_val = df_map.get(idftoken);
            if(token_df_val>0){//filtered token with df=0 considering all
                double idf_val = 1+Math.log10(totaldocs/token_df_val);//non-linear scaling
                idf_map.put(idftoken, idf_val);
            }
        }
        
        /*###################################*/
        //calculate idf for readme
        for(String idftoken:df_map_mc_name.keySet()){
            double token_df_val = df_map_mc_name.get(idftoken);
            if(token_df_val>0){//filtered token with df=0, considering all
                double idf_val = 1+Math.log10(totaldocs/token_df_val);//non-linear scaling
                idf_map_mc_name.put(idftoken, idf_val);
            }
        }
        /*###################################*/
        
        ttf_map = util.sortByComparatorDouble(ttf_map, DESC);
        util.writeOnFile_doubleHash(ttf_map, "ttf_map_rm.txt");
        
        df_map = util.sortByComparatorDouble(df_map, DESC);
        util.writeOnFile_doubleHash(df_map, "df_map_rm.txt");
        
        idf_map = util.sortByComparatorDouble(idf_map, ASC);
        util.writeOnFile_doubleHash(idf_map, "idf_map_rm.txt");
        
        /*###################################*/
        ttf_map_mc_name = util.sortByComparatorDouble(ttf_map_mc_name, DESC);
        util.writeOnFile_doubleHash(ttf_map_mc_name, "ttf_map_mc_name.txt");
        
        df_map_mc_name = util.sortByComparatorDouble(df_map_mc_name, DESC);
        util.writeOnFile_doubleHash(df_map_mc_name, "df_map_mc_name.txt");
        
        idf_map_mc_name = util.sortByComparatorDouble(idf_map_mc_name, ASC);
        util.writeOnFile_doubleHash(idf_map_mc_name, "idf_map_mc_name.txt");
        /*###################################*/
        /*System.out.println("Token data collection complete. \nTotal token = "+totaltoken+"\nTotal unique token = "+ttf_map.size());
        System.out.println("Total unique df token = "+df_map.size());
        System.out.println("Total unique idf token = "+idf_map.size());
        
        System.out.println("Total unique df token df_map_mc_name = "+df_map_mc_name.size());
        System.out.println("Total unique idf token idf_map_mc_name = "+idf_map_mc_name.size());
        */
    }catch(Exception ex){
        ex.printStackTrace();
    }
     
}
public String getTopNWords(String document, int n){
    String[] doc_parts = document.split(" ");
    String topString="";
    for(int i=0;i<n && i<doc_parts.length;i++){
        topString = topString + " "+ doc_parts[i];
    }
    return topString.trim();
}
private void getCategoryStats(String trainCategoryFilePath, String writeCategoryStats){
    BufferedReader fileReader = null;
    BufferedWriter bw = null;
    try{
        File file = new File(trainCategoryFilePath);
        FileWriter fw = new FileWriter(file);
        bw = new BufferedWriter(fw); 
        
        fileReader = new BufferedReader(new FileReader(trainCategoryFilePath));
        String line=null;
        while((line = fileReader.readLine()) !=null){
            String[] line_parts = line.trim().split("\t");
        }
        
        
    }catch(Exception ex){
        ex.printStackTrace();
    }
    finally
    {
        try{
            if(fileReader!=null)
               fileReader.close();
         }catch(Exception ex){
             System.out.println("Error in closing the BufferedReader"+ex);
         }
        try{
            if(bw!=null)
               bw.close();
         }catch(Exception ex){
             System.out.println("Error in closing the BufferedWriter"+ex);
         }
    }
}   
public void UniformlyGenerateEvaluationDataWithCategory(String pathInputDataForWMD, String pathProjectDataForProject, String pathProjectGitUrl, String pathProjectCategory){
       BufferedReader fileReaderWMD = null;
       BufferedReader fileReaderProject = null;
       BufferedReader fileReaderProjectURL = null;
       BufferedReader fileReaderProjectCategory = null;
       HashMap<ArrayList<String>,Integer> documentMap = new HashMap<>();
       ArrayList<Double> docLenList = new ArrayList<>();
       try
        {
            fileReaderWMD = new BufferedReader(new FileReader(pathInputDataForWMD));
            fileReaderProject = new BufferedReader(new FileReader(pathProjectDataForProject));
            fileReaderProjectURL = new BufferedReader(new FileReader(pathProjectGitUrl));
            fileReaderProjectCategory = new BufferedReader(new FileReader(pathProjectCategory));
            
            String lineWMD = "";
            String lineProject = "";
            String lineProjectURL = "";
            String lineProjectCategory = "";
            int countwhile=0;
            int countmap =0;
            while((lineWMD = fileReaderWMD.readLine()) !=null && (lineProject = fileReaderProject.readLine()) !=null && (lineProjectURL = fileReaderProjectURL.readLine()) !=null && (lineProjectCategory = fileReaderProjectCategory.readLine()) !=null){
                countwhile++;
                String[] line_wmd_parts = lineWMD.split("\t");
                String[] line_project_parts = lineProject.split("\t");
                String[] line_project_parts_url = lineProjectURL.split("\t");
                String[] line_project_parts_category = lineProjectCategory.split("\t");
                if((line_wmd_parts[0].trim().equals(line_project_parts[0].trim())) && (line_wmd_parts[0].trim().equals(line_project_parts_url[0].trim())) && (line_wmd_parts[0].trim().equals(line_project_parts_category[0].trim()))){//additional checkup for safety
                    ArrayList<String> docLine = new ArrayList<>();
                    String projectName = line_wmd_parts[0].trim();
                    String descriptions_stemmed = line_wmd_parts[1];
                    String descriptions_non_stemmed = line_project_parts[1];
                    String gitURL = line_project_parts_url[1];
                    String category = line_project_parts_category[1];
                    
                    
                    docLine.add(projectName);
                    docLine.add(descriptions_non_stemmed);
                    docLine.add(descriptions_stemmed);
                    docLine.add(gitURL);
                    docLine.add(category);
                    
                    int docLen = descriptions_stemmed.split(" ").length;
                    //if(docLen>5){
                        docLenList.add(docLen*1.0);
                        Integer nval = documentMap.get(docLine);
                        if(nval !=null){
                            //System.out.println("Duplicate \n");
                            //System.out.println(projectName + " "+descriptions_non_stemmed+" "+ descriptions_stemmed+" "+gitURL+" "+category);
                        }
                        documentMap.put(docLine,docLen);
                        
                        countmap++;
                    //}
                      
                }else{
                    System.out.println("Data miss match - Project name does not match");
                }
            }
            //Collections.shuffle(docLenList);//take test and train randomly
            //Collections.sort(docLenList);
            //documentMap = shuffleHashMap(documentMap);//shuffle hashmap, take test data randomly

            HashMap<ArrayList<String>,Integer>trainPart = new HashMap<>();
            HashMap<ArrayList<String>,Integer>testPart = new HashMap<>();
            int countquery=0;//max 
            int testQueryNumner=50;
            System.out.println("DocMap Size = "+documentMap.size());
            System.out.println("Count while = "+countwhile);
            System.out.println("Count docmap = "+countmap);
            for(ArrayList<String> a:documentMap.keySet()){
                if(countquery %15 == 0){
                    testPart.put(a, documentMap.get(a));
                }
                else{
                    trainPart.put(a, documentMap.get(a));
                }
              countquery++;  
            }
            writeSpecialDocumentOnFileWithCategory(trainPart, "trainDocForWMD.txt", "trainProjectDetails.txt","trainProjectGitURL.txt","trainProjectCategory.txt");
            writeSpecialDocumentOnFileWithCategory(testPart, "testDocForWMD.txt", "testProjectDetails.txt","testProjectGitURL.txt","testProjectCategory.txt");
           // writeSpecialDocumentOnFileWithCategory(documentMap, "DocForWMD.txt", "uniqueProjectDetails.txt", "uniqueProjectGitURL.txt", "uniqueProjectCategory.txt");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
       finally
        {
            try{
                if(fileReaderWMD!=null)
                   fileReaderWMD.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedReader"+ex);
             }
            try{
                if(fileReaderProject !=null)
                   fileReaderProject.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedReader"+ex);
             }
        }
}  
public void generateEvaluationDataWithCategory(String pathInputDataForWMD, String pathProjectDataForProject, String pathProjectGitUrl, String pathProjectCategory){
       BufferedReader fileReaderWMD = null;
       BufferedReader fileReaderProject = null;
       BufferedReader fileReaderProjectURL = null;
       BufferedReader fileReaderProjectCategory = null;
       HashMap<ArrayList<String>,Integer> documentMap = new HashMap<>();
       ArrayList<Double> docLenList = new ArrayList<>();
       try
        {
            fileReaderWMD = new BufferedReader(new FileReader(pathInputDataForWMD));
            fileReaderProject = new BufferedReader(new FileReader(pathProjectDataForProject));
            fileReaderProjectURL = new BufferedReader(new FileReader(pathProjectGitUrl));
            fileReaderProjectCategory = new BufferedReader(new FileReader(pathProjectCategory));
            
            String lineWMD = "";
            String lineProject = "";
            String lineProjectURL = "";
            String lineProjectCategory = "";
            while((lineWMD = fileReaderWMD.readLine()) !=null && (lineProject = fileReaderProject.readLine()) !=null && (lineProjectURL = fileReaderProjectURL.readLine()) !=null && (lineProjectCategory = fileReaderProjectCategory.readLine()) !=null){
                String[] line_wmd_parts = lineWMD.split("\t");
                String[] line_project_parts = lineProject.split("\t");
                String[] line_project_parts_url = lineProjectURL.split("\t");
                String[] line_project_parts_category = lineProjectCategory.split("\t");
                if((line_wmd_parts[0].trim().equals(line_project_parts[0].trim())) && (line_wmd_parts[0].trim().equals(line_project_parts_url[0].trim())) && (line_wmd_parts[0].trim().equals(line_project_parts_category[0].trim()))){//additional checkup for safety
                    ArrayList<String> docLine = new ArrayList<>();
                    String projectName = line_wmd_parts[0].trim();
                    String descriptions_stemmed = line_wmd_parts[1];
                    String descriptions_non_stemmed = line_project_parts[1];
                    String gitURL = line_project_parts_url[1];
                    String category = line_project_parts_category[1];
                    
                    
                    docLine.add(projectName);
                    docLine.add(descriptions_non_stemmed);
                    docLine.add(descriptions_stemmed);
                    docLine.add(gitURL);
                    docLine.add(category);
                    
                    int docLen = descriptions_stemmed.split(" ").length;
                    //if(docLen>5){
                        docLenList.add(docLen*1.0);
                        documentMap.put(docLine,docLen);
                    //}
                      
                }else{
                    System.out.println("Data miss match - Project name does not match");
                }
            }
            //Collections.shuffle(docLenList);//take test and train randomly
            //Collections.sort(docLenList);
            documentMap = shuffleHashMap(documentMap);//shuffle hashmap, take test data randomly

            HashMap<ArrayList<String>,Integer>trainPart = new HashMap<>();
            HashMap<ArrayList<String>,Integer>testPart = new HashMap<>();
            int countquery=0;//max 
            int testQueryNumner=50;
            for(ArrayList<String> a:documentMap.keySet()){
                if(countquery<50){
                    testPart.put(a, documentMap.get(a));
                }else{
                    trainPart.put(a, documentMap.get(a));
                }
              countquery++;  
            }
            writeSpecialDocumentOnFileWithCategory(trainPart, "trainDocForWMD.txt", "trainProjectDetails.txt","trainProjectGitURL.txt","trainProjectCategory.txt");
            writeSpecialDocumentOnFileWithCategory(testPart, "testDocForWMD.txt", "testProjectDetails.txt","testProjectGitURL.txt","testProjectCategory.txt");
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
       finally
        {
            try{
                if(fileReaderWMD!=null)
                   fileReaderWMD.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedReader"+ex);
             }
            try{
                if(fileReaderProject !=null)
                   fileReaderProject.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedReader"+ex);
             }
        }
}
public HashMap<ArrayList<String>,Integer> shuffleHashMap(HashMap<ArrayList<String>,Integer> unShuffledMap){
    HashMap<ArrayList<String>,Integer> shuffledMap = new HashMap<>();
    List<ArrayList<String>> keys = new ArrayList(unShuffledMap.keySet());
    Collections.shuffle(keys);
    for (ArrayList<String> key : keys) {
        shuffledMap.put(key, unShuffledMap.get(key));
    }
    return shuffledMap;
}
private void generateEvaluationData(String pathInputDataForWMD, String pathProjectDataForProject, String pathProjectGitUrl){
       BufferedReader fileReaderWMD = null;
       BufferedReader fileReaderProject = null;
       BufferedReader fileReaderProjectURL = null;
       HashMap<ArrayList<String>,Integer> documentMap = new HashMap<>();
       ArrayList<Double> docLenList = new ArrayList<>();
       
       try
        {
            fileReaderWMD = new BufferedReader(new FileReader(pathInputDataForWMD));
            fileReaderProject = new BufferedReader(new FileReader(pathProjectDataForProject));
            fileReaderProjectURL = new BufferedReader(new FileReader(pathProjectGitUrl));
            
            String lineWMD = "";
            String lineProject = "";
            String lineProjectURL = "";
            while((lineWMD = fileReaderWMD.readLine()) !=null && (lineProject = fileReaderProject.readLine()) !=null && (lineProjectURL = fileReaderProjectURL.readLine()) !=null){
                String[] line_wmd_parts = lineWMD.split("\t");
                String[] line_project_parts = lineProject.split("\t");
                String[] line_project_parts_url = lineProjectURL.split("\t");
                if((line_wmd_parts[0].trim().equals(line_project_parts[0].trim())) && (line_wmd_parts[0].trim().equals(line_project_parts_url[0].trim()))){//additional checkup
                    ArrayList<String> docLine = new ArrayList<>();
                    String projectName = line_wmd_parts[0].trim();
                    String descriptions_stemmed = line_wmd_parts[1];
                    String descriptions_non_stemmed = line_project_parts[1];
                    String gitURL = line_project_parts_url[1];
                    
                    
                    docLine.add(projectName);
                    docLine.add(descriptions_non_stemmed);
                    docLine.add(descriptions_stemmed);
                    docLine.add(gitURL);
                    
                    int docLen = descriptions_stemmed.split(" ").length;
                    //if(docLen>5){
                        docLenList.add(docLen*1.0);
                        documentMap.put(docLine,docLen);
                    //}
                      
                }else{
                    System.out.println("Data miss match - Project name does not match");
                }
            }
            
            Collections.sort(docLenList);
            int cut1 = (int)calculatePercentile(docLenList,25);
            System.out.println("Q1 = "+cut1);
            
            documentMap = filterByDocLength(documentMap, cut1);
            
            docLenList =new ArrayList<>();
            for(ArrayList<String>temp:documentMap.keySet()){
                Integer num = documentMap.get(temp);
                docLenList.add(num*1.0);
            }
             Collections.sort(docLenList);
            int cut2 = (int)calculatePercentile(docLenList,25);
            System.out.println("\tQ1 = "+cut2);
            int cut3 = (int)calculatePercentile(docLenList,50);
            System.out.println("Q2 = "+cut3);
            int cut4 = (int)calculatePercentile(docLenList,75);
            System.out.println("Q3 = "+cut4);
            int cut5 = (int)calculatePercentile(docLenList,100);
            System.out.println("Q3 = "+cut5);
            
            documentMap = sortArraListHashMap(documentMap, DESC);
            HashMap<String, HashMap<ArrayList<String>,Integer> > testTrainDataPart1 = splitByCutoff(documentMap, cut1, cut2, 25);
            HashMap<String, HashMap<ArrayList<String>,Integer> > testTrainDataPart2 = splitByCutoff(documentMap, cut2, cut3, 25);
            HashMap<String, HashMap<ArrayList<String>,Integer> > testTrainDataPart3 = splitByCutoff(documentMap, cut3, cut4, 25);
            HashMap<String, HashMap<ArrayList<String>,Integer> > testTrainDataPart4 = splitByCutoff(documentMap, cut4, cut5+1, 25);
            
            HashMap<ArrayList<String>,Integer>trainPart = new HashMap<>();
            HashMap<ArrayList<String>,Integer>testPart = new HashMap<>();
            
            trainPart = testTrainDataPart1.get("train");
            testPart = testTrainDataPart1.get("test");
            writeSpecialDocumentOnFile(trainPart, rootEvalFolder, "trainDocForWMD.txt", "trainProjectDetails.txt","trainProjectGitURL.txt");
            writeSpecialDocumentOnFile(testPart, rootEvalFolder, "testDocForWMD_Q1.txt", "testProjectDetails_Q1.txt","testProjectGitURL_Q1.txt");
            
            trainPart = testTrainDataPart2.get("train");
            testPart = testTrainDataPart2.get("test");
            writeSpecialDocumentOnFile(trainPart, rootEvalFolder, "trainDocForWMD.txt", "trainProjectDetails.txt","trainProjectGitURL.txt");
            writeSpecialDocumentOnFile(testPart, rootEvalFolder, "testDocForWMD_Q2.txt", "testProjectDetails_Q2.txt","testProjectGitURL_Q2.txt");
            
            trainPart = testTrainDataPart3.get("train");
            testPart = testTrainDataPart3.get("test");
            writeSpecialDocumentOnFile(trainPart, rootEvalFolder, "trainDocForWMD.txt", "trainProjectDetails.txt","trainProjectGitURL.txt");
            writeSpecialDocumentOnFile(testPart, rootEvalFolder, "testDocForWMD_Q3.txt", "testProjectDetails_Q3.txt","testProjectGitURL_Q3.txt");
            
            trainPart = testTrainDataPart4.get("train");
            testPart = testTrainDataPart4.get("test");
            writeSpecialDocumentOnFile(trainPart, rootEvalFolder, "trainDocForWMD.txt", "trainProjectDetails.txt","trainProjectGitURL.txt");
            writeSpecialDocumentOnFile(testPart, rootEvalFolder, "testDocForWMD_Q4.txt", "testProjectDetails_Q4.txt","testProjectGitURL_Q4.txt");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
       finally
        {
            try{
                if(fileReaderWMD!=null)
                   fileReaderWMD.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedReader"+ex);
             }
            try{
                if(fileReaderProject !=null)
                   fileReaderProject.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedReader"+ex);
             }
        }
   }
private static HashMap<ArrayList<String>, Integer> sortArraListHashMap(HashMap<ArrayList<String>, Integer> unsortMap, final boolean order)
{

    List<Map.Entry<ArrayList<String>, Integer>> list = new LinkedList<>(unsortMap.entrySet());

    // Sorting the list based on values
    Collections.sort(list, new Comparator<Map.Entry<ArrayList<String>, Integer>>()
    {
        public int compare(Map.Entry<ArrayList<String>, Integer> o1,
                Map.Entry<ArrayList<String>, Integer> o2)
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
    HashMap<ArrayList<String>, Integer> sortedMap = new LinkedHashMap<>();
    for (Map.Entry<ArrayList<String>, Integer> entry : list)
    {
        sortedMap.put(entry.getKey(), entry.getValue());
    }

    return sortedMap;
}
public void writeSpecialDocumentOnFile(HashMap<ArrayList<String>,Integer> docMap,String writePath, String docForWMD_name, String projectProperties_name, String projectGitURL_name)
{
   
   BufferedWriter bw_des_original = null;
   BufferedWriter bw_des_with_stem = null;
   BufferedWriter bw_project_git_url = null;
   try {
     
     FileWriter fw_des_with_stem = new FileWriter(writePath+"/"+docForWMD_name,true);
     FileWriter fw_des_original = new FileWriter(writePath+"/"+projectProperties_name,true);
     FileWriter fw_project_git_url = new FileWriter(writePath+"/"+projectGitURL_name,true);
     
     
     bw_des_original = new BufferedWriter(fw_des_original);
     bw_des_with_stem = new BufferedWriter(fw_des_with_stem);
     bw_project_git_url = new BufferedWriter(fw_project_git_url);
     
    for (ArrayList<String> listEntry: docMap.keySet()){
       
       String projectName = listEntry.get(0);
       String description_original = listEntry.get(1);
       String description_stemmed = listEntry.get(2);
       String project_git_url = listEntry.get(3);
       
       bw_des_original.write(projectName+"\t"+description_original+"\n");
       bw_des_with_stem.write(projectName+"\t"+description_stemmed+"\n");
       bw_project_git_url.write(projectName+"\t"+project_git_url+"\n");
    }
    System.out.println("Write Successfull: "+writePath);

 } catch (IOException ioe) {
      ioe.printStackTrace();
   }
   finally
   { 
      try{
         if(bw_des_original!=null)
            bw_des_original.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
      }
      try{
         if(bw_des_with_stem!=null)
            bw_des_with_stem.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
       }
      try{
         if(bw_project_git_url!=null)
            bw_project_git_url.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
       }

   }

}
public void writeSpecialDocumentOnFileWithCategory(HashMap<ArrayList<String>,Integer> docMap, String docForWMD_name, String projectProperties_name, String projectGitURL_name,String projectCategory_name)
{
   
   BufferedWriter bw_des_original = null;
   BufferedWriter bw_des_with_stem = null;
   BufferedWriter bw_project_git_url = null;
    BufferedWriter bw_project_category = null;
   try {
     
     /*FileWriter fw_des_with_stem = new FileWriter(writePath+"/"+docForWMD_name,true);
     FileWriter fw_des_original = new FileWriter(writePath+"/"+projectProperties_name,true);
     FileWriter fw_project_git_url = new FileWriter(writePath+"/"+projectGitURL_name,true);
     FileWriter fw_project_category = new FileWriter(writePath+"/"+projectCategory_name,true);
     */
     FileWriter fw_des_with_stem = new FileWriter(docForWMD_name);
     FileWriter fw_des_original = new FileWriter(projectProperties_name);
     FileWriter fw_project_git_url = new FileWriter(projectGitURL_name);
     FileWriter fw_project_category = new FileWriter(projectCategory_name);
     
     bw_des_original = new BufferedWriter(fw_des_original);
     bw_des_with_stem = new BufferedWriter(fw_des_with_stem);
     bw_project_git_url = new BufferedWriter(fw_project_git_url);
     bw_project_category = new BufferedWriter(fw_project_category);
     
    for (ArrayList<String> listEntry: docMap.keySet()){
       
       String projectName = listEntry.get(0);
       String description_original = listEntry.get(1);
       String description_stemmed = listEntry.get(2);
       String project_git_url = listEntry.get(3);
       String project_category = listEntry.get(4);
       
       bw_des_original.write(projectName+"\t"+description_original+"\n");
       bw_des_with_stem.write(projectName+"\t"+description_stemmed+"\n");
       bw_project_git_url.write(projectName+"\t"+project_git_url+"\n");
       bw_project_category.write(projectName+"\t"+project_category+"\n");
    }
    System.out.println("Write Successfull.");

 } catch (IOException ioe) {
      ioe.printStackTrace();
   }
   finally
   { 
      try{
         if(bw_des_original!=null)
            bw_des_original.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
      }
      try{
         if(bw_des_with_stem!=null)
            bw_des_with_stem.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
       }
      try{
         if(bw_project_git_url!=null)
            bw_project_git_url.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
       }
      try{
         if(bw_project_category!=null)
            bw_project_category.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
       }
   }

}
public void writeDocumentOnFileWithCategoryStats(HashMap<ArrayList<String>,String> docMap, String docForWMD_name, String projectProperties_name, String projectGitURL_name,String projectCategory_name)
{
   
   BufferedWriter bw_des_original = null;
   BufferedWriter bw_des_with_stem = null;
   BufferedWriter bw_project_git_url = null;
    BufferedWriter bw_project_category = null;
   try {
     
     /*FileWriter fw_des_with_stem = new FileWriter(writePath+"/"+docForWMD_name,true);
     FileWriter fw_des_original = new FileWriter(writePath+"/"+projectProperties_name,true);
     FileWriter fw_project_git_url = new FileWriter(writePath+"/"+projectGitURL_name,true);
     FileWriter fw_project_category = new FileWriter(writePath+"/"+projectCategory_name,true);
     */
     FileWriter fw_des_with_stem = new FileWriter(docForWMD_name);
     FileWriter fw_des_original = new FileWriter(projectProperties_name);
     FileWriter fw_project_git_url = new FileWriter(projectGitURL_name);
     FileWriter fw_project_category = new FileWriter(projectCategory_name);
     
     bw_des_original = new BufferedWriter(fw_des_original);
     bw_des_with_stem = new BufferedWriter(fw_des_with_stem);
     bw_project_git_url = new BufferedWriter(fw_project_git_url);
     bw_project_category = new BufferedWriter(fw_project_category);
     
    for (ArrayList<String> listEntry: docMap.keySet()){
       
       String projectName = listEntry.get(0);
       String description_original = listEntry.get(1);
       String description_stemmed = listEntry.get(2);
       String project_git_url = listEntry.get(3);
       String project_category = listEntry.get(4);
       
       bw_des_original.write(projectName+"\t"+description_original+"\n");
       bw_des_with_stem.write(projectName+"\t"+description_stemmed+"\n");
       bw_project_git_url.write(projectName+"\t"+project_git_url+"\n");
       bw_project_category.write(projectName+"\t"+project_category+"\n");
    }
    System.out.println("Write Successfull.");

 } catch (IOException ioe) {
      ioe.printStackTrace();
   }
   finally
   { 
      try{
         if(bw_des_original!=null)
            bw_des_original.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
      }
      try{
         if(bw_des_with_stem!=null)
            bw_des_with_stem.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
       }
      try{
         if(bw_project_git_url!=null)
            bw_project_git_url.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
       }
      try{
         if(bw_project_category!=null)
            bw_project_category.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
       }
   }

}
public void writeDataTypewise(String dataTestOrTrainFilePath, String docForWMD_name, String projectProperties_name, String projectGitURL_name,String projectCategory_name, int readme_cutoff, int mc_cutoff)
{
    BufferedReader br = null;
    BufferedWriter bw_des_original = null;
    BufferedWriter bw_doc_content_stem = null;
    BufferedWriter bw_project_git_url = null;
    BufferedWriter bw_project_category = null;
    
    HashMap<String,Double> idf_map = new HashMap<>();
    idf_map = util.readFromFile_doubleHash("idf_map_rm.txt");
    
    HashMap<String,Double> idf_map_mc = new HashMap<>();
    idf_map_mc = util.readFromFile_doubleHash("idf_map_mc_name.txt");
    
   try {
     br = new BufferedReader(new FileReader(dataTestOrTrainFilePath));
     
     FileWriter fw_doc_content_stem = new FileWriter(docForWMD_name);
     FileWriter fw_des_original = new FileWriter(projectProperties_name);
     FileWriter fw_project_git_url = new FileWriter(projectGitURL_name);
     FileWriter fw_project_category = new FileWriter(projectCategory_name);
     
     bw_des_original = new BufferedWriter(fw_des_original);
     bw_doc_content_stem = new BufferedWriter(fw_doc_content_stem);
     bw_project_git_url = new BufferedWriter(fw_project_git_url);
     bw_project_category = new BufferedWriter(fw_project_category);
     
    String line="";
    while((line = br.readLine()) !=null ){
       String[] line_parts = line.split("\t");
       String projectName = line_parts[0];
       String project_git_url = line_parts[1];
       String project_category = line_parts[3];
       String description_original = line_parts[4];
       String description_stemmed = line_parts[5];
       String readMeContent_stemmed = line_parts[7];
       String methodClassName_stemmed="";
       if(line_parts.length>=10){//if exist
           methodClassName_stemmed = line_parts[9];
       }
       
       String methodClassNameSortedByWeight = sortContentTokenByTFIDF(methodClassName_stemmed,idf_map_mc);
       String readMeSortedByWeight = sortContentTokenByTFIDF(readMeContent_stemmed,idf_map);
       
       //String document_content = description_stemmed +" " + readMeSortedByWeight;// adding readme content with description. Later add code using the same procedure here
       String document_content = description_stemmed + " "+ getTopNWords(readMeSortedByWeight, readme_cutoff) + " "+ getTopNWords(methodClassNameSortedByWeight, mc_cutoff);
       //document_content = getTopNWords(document_content, word_cutoff);//filtered out less important qord
       
       bw_des_original.write(projectName+"\t"+description_original+"\n");
       bw_doc_content_stem.write(projectName+"\t"+document_content+"\n");
       bw_project_git_url.write(projectName+"\t"+project_git_url+"\n");
       bw_project_category.write(projectName+"\t"+project_category+"\n");
    }
    //System.out.println("Write Successfull.");

 } catch (IOException ioe) {
      ioe.printStackTrace();
   }
   finally
   { 
      try{
         if(bw_des_original!=null)
            bw_des_original.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
      }
      try{
         if(bw_doc_content_stem!=null)
            bw_doc_content_stem.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
       }
      try{
         if(bw_project_git_url!=null)
            bw_project_git_url.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
       }
      try{
         if(bw_project_category!=null)
            bw_project_category.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedWriter"+ex);
       }
      try{
         if(br!=null)
            br.close();
      }catch(Exception ex){
          System.out.println("Error in closing the BufferedReader"+ex);
       }
   }

}
public String sortContentTokenByTFIDF(String unOrderContent, HashMap<String, Double> idf_map){
    HashMap<String, Double> tf_idf_map = new HashMap<>();
    HashMap<String, Double> tf_map = new HashMap<>();
    String[] content_parts = unOrderContent.split(" ");
    for(String part:content_parts){
        Double tfValue = tf_map.get(part);
        if(tfValue==null){
            tfValue=0.0;
        }
        else{
            tfValue++;
        }
        tf_map.put(part, tfValue);
    }
    for(String token:tf_map.keySet()){
        double tf_subLinNorm = 1 + Math.log10(tf_map.get(token));//sub-linear tf scaling
        Double idf_val = idf_map.get(token);
        if(idf_val==null){
            idf_val = 0.0;
        }
        double tfidf_weight = tf_subLinNorm * idf_val;
        tf_idf_map.put(token, tfidf_weight);
    }
    tf_idf_map = util.sortByComparatorDouble(tf_idf_map, DESC);
    String orderedContnt = "";
    for(String key:tf_idf_map.keySet()){
        orderedContnt = orderedContnt + " "+key;
    }
    return  orderedContnt.trim();
}
private HashMap<String, HashMap<ArrayList<String>,Integer> > splitByCutoff(HashMap<ArrayList<String>,Integer> docHashMap, int lowerCutoff, int upperCutoff, int numOfTestData){
    HashMap<String, HashMap<ArrayList<String>,Integer> > trainTestDataMap = new HashMap<>();
    HashMap<ArrayList<String>,Integer> filteredTempMap = new HashMap<>();
    for(ArrayList<String> aListEntry:docHashMap.keySet()){
        Integer singleDocLen = docHashMap.get(aListEntry);
        if(singleDocLen !=null && singleDocLen>=lowerCutoff && singleDocLen <upperCutoff){
            filteredTempMap.put(aListEntry, singleDocLen);
        }
    }
    HashMap<ArrayList<String>,Integer> trainDocMap = new HashMap<>();
    HashMap<ArrayList<String>,Integer> testDocMap = new HashMap<>();
    //split here
    ArrayList<Integer> arrayIndex = new ArrayList<>();
    for(int i=0;i<filteredTempMap.size();i++){
        arrayIndex.add(i);
    }
    ArrayList<Integer> arrayIndexTest = new ArrayList<>();
    
    Collections.shuffle(arrayIndex);
    
    if(arrayIndex.size()>numOfTestData){
        for(int j=0;j<numOfTestData;j++){
            arrayIndexTest.add(arrayIndex.get(j));
        }
        int checkCount=0;
        for(ArrayList<String> filteredListEntry:filteredTempMap.keySet()){
            if(arrayIndexTest.contains(checkCount)){
                testDocMap.put(filteredListEntry, filteredTempMap.get(filteredListEntry));
            }else{
                trainDocMap.put(filteredListEntry, filteredTempMap.get(filteredListEntry));
            }
            checkCount++;
        }
    }else{
        testDocMap = filteredTempMap;
        trainDocMap = null;
    }
    System.out.println("Total doc size = "+docHashMap.size());
    System.out.println("Total doc size within cutoff = "+filteredTempMap.size());
    System.out.println("Total doc size for testing = "+testDocMap.size());
    System.out.println("Total doc size for training = "+trainDocMap.size());
    trainTestDataMap.put("train", trainDocMap);
    trainTestDataMap.put("test", testDocMap);
    return trainTestDataMap;
}
public double calculatePercentile(ArrayList<Double>aSortedList,double percent){
    int totalLength = aSortedList.size();
    int index = (int) Math.round((totalLength*percent)/100);
    if(index>=0 && index<totalLength){
        return aSortedList.get(index);
    }
    else{
        if(totalLength>0 && index==totalLength){//for 100 percent return the max
            return aSortedList.get(index-1);
        }
        System.out.println("Ivalid percentile range");
        return aSortedList.get(0)-100;//return invalid value that is out of array range
    }
}
private HashMap<ArrayList<String>,Integer> filterByDocLength(HashMap<ArrayList<String>,Integer>docMap, int cutoff){
    HashMap<ArrayList<String>,Integer> filteredDocMap = new HashMap<>();
    for(ArrayList<String> aListEntry :docMap.keySet()){
        Integer singleDocEntryLength = docMap.get(aListEntry);
        if(singleDocEntryLength !=null && singleDocEntryLength >cutoff){
            filteredDocMap.put(aListEntry, singleDocEntryLength);
        }
    }
    return filteredDocMap;
}
}
