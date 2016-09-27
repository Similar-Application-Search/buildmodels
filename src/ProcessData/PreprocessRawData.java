/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ProcessData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

/**
 *
 * @author mr5ba
 */
public class PreprocessRawData {
    private static final  String rootDataPath = "I:/Dev/NetbeanProjects/data/similar_projects/";
    HashSet<String> stopwords_set;
    Tokenizer tokenizer ;
    public PreprocessRawData(){
        try {
         tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
        } catch (IOException e) {
              e.printStackTrace();
        }
    }
    public static void main(String[] args){
        PreprocessRawData preprocessRawData = new PreprocessRawData();
        EvaluationData evaluationData = new EvaluationData();
        if(args.length != 2) {
            System.out.println("Give two arguments !!!");
            System.exit(0);
        }
        preprocessRawData.createAllProjectsData(args[0], args[1]);
        //preprocessRawData.createDocForWMDWithCategory(args[0], args[1], args[2], args[3],args[4], Integer.parseInt(args[5]));
        //evaluationData.UniformlyGenerateEvaluationDataWithCategory(args[0], args[1],args[2],args[3]);
        //preprocessRawData.createDocForModel(preprocessRawData.rootDataPath+"projects/",preprocessRawData.rootDataPath+"inputDocForW2V_java.txt",preprocessRawData.rootDataPath+"inputDocForWMD_java.txt",preprocessRawData.rootDataPath+"projectDetails.txt");
        //preprocessRawData.createDocForModel(args[0],args[1],args[2],args[3]);
       // preprocessRawData.createDocForWord2Vec(args[0], args[1], args[2], args[3], args[4],args[5],args[6]);
   
}
    private void getProjectsUrls(String fileToParse){
        BufferedReader fileReader = null;
         BufferedWriter bw = null;
        final String DELIMITER = "\t";
        int linecount=0;
        double docCount=0;
        double totalDocLen = 0;
        double avgDocLen=0;
        int totalLine = 0;
        int tokenGTFourDocCount=0;
        int noDescriptionCOunt=0;
        int javaProjectCount=0;
        try
        {
            String line = "";
            
            //Create the file reader
            fileReader = new BufferedReader(new FileReader(fileToParse));
            FileWriter fw = new FileWriter(rootDataPath+"/"+"project_java.csv");
            bw = new BufferedWriter(fw);
            
            while ((line = fileReader.readLine()) != null) 
            {
                if(totalLine==0){
                    bw.write(line+"\n");
                }
                totalLine++;
                String[] tokens = line.split(DELIMITER);
                if(tokens.length>4){
                    String projectName = tokens[2];
                    if(tokens[3].isEmpty() || tokens[3].equals("null")){
                        noDescriptionCOunt++;
                    } 
                    //String projectDescription  = processContent(tokens[3],true);//with stopword removal
                    String language = tokens[5].trim();
                    //System.out.println(language);
                    if(language.toLowerCase().equals("java")){
                        bw.write(line+"\n");
                        javaProjectCount++;
                    }
                    tokenGTFourDocCount++;
                }
                
                linecount++;
                if(linecount%1000000==0){
                    System.out.println("Process "+linecount+" Entry");
                    System.out.println("Total projects = "+totalLine);
        System.out.println("Total line doc greater than four token = "+tokenGTFourDocCount);
        System.out.println("Selected doc for model = "+docCount);
        System.out.println("Total java projects = "+javaProjectCount);
                }
            }
            //add more doc here for word2vec
            //wiki
            //news, other resources
            
            
        } 
        catch (Exception e) {
            e.printStackTrace();
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
        System.out.println("Total projects = "+totalLine);
        System.out.println("Total line doc greater than four token = "+tokenGTFourDocCount);
        System.out.println("Selected doc for model = "+docCount);
        System.out.println("Total java projects = "+javaProjectCount);
    }
private void createDocForW2VWMD(String fileToParse, String inputDocForW2V, String inputDocForWMD){
         //Input file which needs to be parsed
        //String fileToParse = "SampleCSVFile.csv";
        LoadStopwords(rootDataPath+"english.stop");
        BufferedReader fileReader = null;
        BufferedWriter bw_w2v = null;
        BufferedWriter bw_wmd = null;

        //Delimiter used in CSV file
        final String DELIMITER = "\t";
        int linecount=0;
        double docCount=0;
        double totalDocLen = 0;
        double avgDocLen=0;
        int totalLine = 0;
        int tokenGTFourDocCount=0;
        int noDescriptionCOunt=0;
        
        try
        {
            String line = "";
            //Create the file reader
            fileReader = new BufferedReader(new FileReader(fileToParse));
            File file_w2v = new File(inputDocForW2V);
            if (!file_w2v.exists()) {
               file_w2v.createNewFile();
            }
            FileWriter fw_w2v = new FileWriter(file_w2v);
            bw_w2v = new BufferedWriter(fw_w2v);
            
            File file_wmd = new File(inputDocForWMD);
            if (!file_wmd.exists()) {
               file_wmd.createNewFile();
            }
            FileWriter fw_wmd = new FileWriter(file_wmd);
            bw_wmd = new BufferedWriter(fw_wmd); 
            //Read the file line by line
          
            
            while ((line = fileReader.readLine()) != null) 
            {
                totalLine++;
                String[] tokens = line.split(DELIMITER);
                if(tokens.length>4){
                    String projectName = tokens[2];
                    if(tokens[3].isEmpty() || tokens[3].equals("null")){
                        noDescriptionCOunt++;
                    }
                    String projectDescription  = processContent(tokens[3],true);//with stopword removal
                    String language = tokens[4];
                    if(language.toLowerCase().equals("java")){
                        if(projectDescription.isEmpty() == false && projectDescription.trim().equals("null")==false){//skip project with no valid description
                            bw_w2v.write(projectDescription+"\n");
                            bw_wmd.write(projectName+"\t"+projectDescription+"\n");
                            int singleDocLen = projectDescription.split(" ").length;
                            //bw_w2v.write(singleDocLen+"\n");
                            totalDocLen +=singleDocLen;
                            docCount++;
                        }
                    }
                    tokenGTFourDocCount++;
                }
                
                linecount++;
                if(linecount%1000000==0){
                    System.out.println("Process "+linecount+" Entry");
                }
            }
            //add more doc here for word2vec
            //wiki
            //news, other resources
            
            if(docCount>0){
                avgDocLen=totalDocLen/docCount;
            }
            System.out.println("AVG Doc Len = "+avgDocLen);
        } 
        catch (Exception e) {
            e.printStackTrace();
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
                if(bw_w2v!=null)
                   bw_w2v.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedWriter"+ex);
             }
            try{
                if(bw_wmd!=null)
                   bw_wmd.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedWriter"+ex);
             }
        }
        System.out.println("Total line doc = "+totalLine);
        System.out.println("Total line doc greater than four token = "+tokenGTFourDocCount);
        System.out.println("Selected doc for model = "+docCount);
        System.out.println("Project with no description = "+noDescriptionCOunt);
    }
private void createDocForWord2Vec(String java_projects_path,String projDesc_path,String wikiFilePath, String inputDocForW2V, String include_java_projects, String include_projDesc, String includeWiki){
         //Input file which needs to be parsed
        //String fileToParse = "SampleCSVFile.csv";
        LoadStopwords("./data/english.stop");
        BufferedReader fileReader_java_projects = null;
        BufferedReader fileReader_wiki = null;
        BufferedReader fileReader_projDesc = null;
        
        BufferedWriter bw_w2v = null;
        
        //Delimiter used in CSV file
        final String DELIMITER = "\t";
        int linecount=0;
        double docCount=0;
        double totalDocLen = 0;
        double avgDocLen=0;
        int totalLine = 0;
        int tokenGTFourDocCount=0;
        int noDescriptionCOunt=0;
        
        try
        {
            String line = "";
            File file_w2v = new File(inputDocForW2V);
            if (!file_w2v.exists()) {
               file_w2v.createNewFile();
            }
            FileWriter fw_w2v = new FileWriter(file_w2v);
            bw_w2v = new BufferedWriter(fw_w2v);
            
            if(include_java_projects.equals("true")){
                fileReader_java_projects = new BufferedReader(new FileReader(java_projects_path));
                linecount=0;
                while ((line = fileReader_java_projects.readLine()) != null) 
                {
                    bw_w2v.write(line+"\n");//line context already processed

                    linecount++;
                    if(linecount%1000000==0){
                        System.out.println("Process java_projects "+linecount+" Entry");
                    }
                }
            }
            if(include_projDesc.equals("true")){
                fileReader_projDesc = new BufferedReader(new FileReader(projDesc_path));
            
                linecount=0;
                while ((line = fileReader_projDesc.readLine()) != null) 
                {
                    bw_w2v.write(line+"\n");//line context already processed
                    linecount++;
                    if(linecount%1000000==0){
                        System.out.println("Process projDesc "+linecount+" Entry");
                    }
                }
            }
            
            //add more doc here for word2vec
            //wiki
            //news, other resources
            if(includeWiki.equals("true")){
                System.out.println("Adding wikipedia article to Word2Vec input");
                fileReader_wiki = new BufferedReader(new FileReader(wikiFilePath));
                int wikiEmptyCount=0;
                linecount=0;
                while ((line = fileReader_wiki.readLine()) != null) 
                {
                    String line_processed = processContent(line,true);
                    if(line_processed.isEmpty()==false){
                         bw_w2v.write(line_processed+"\n");
                    }
                    else{
                        wikiEmptyCount++;
                    }
                         
                   
                    linecount++;
                    if(linecount%1000000==0){
                        System.out.println("Process Wiki Entry "+linecount);
                    }
                }
                System.out.println("Wiki documents with no description = "+wikiEmptyCount);
                            
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        } 
        finally
        {
            
            try{
                if(fileReader_java_projects!=null)
                   fileReader_java_projects.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedReader"+ex);
             }
            try{
                if(fileReader_projDesc!=null)
                   fileReader_projDesc.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedReader"+ex);
             }
            try{
                if(fileReader_wiki!=null)
                   fileReader_wiki.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedReader"+ex);
             }
            try{
                if(bw_w2v!=null)
                   bw_w2v.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedWriter"+ex);
             }
            
        }
}
public String getGooglePlayUrlFromFileContent(String readmeFilePath){
    String googlePlayUrl = null;
    String tokenTest = null;
    BufferedReader fileReader = null;
    try
        {
            File file = new File(readmeFilePath);
            if (!file.exists()) {
               return googlePlayUrl;//returnr null
            }
            fileReader = new BufferedReader(new FileReader(readmeFilePath));
            String line;
            while((line = fileReader.readLine()) !=null){
                line = line.replaceAll("\\(", " ");
                line = line.replaceAll("\\)", " ");
                line = line.replaceAll("\\[", " ");
                line = line.replaceAll("\\]", " ");
                line = line.replaceAll("\\{", " ");
                line = line.replaceAll("\\}", " ");
                line = line.replaceAll("\\\\", " ");
                line = line.replaceAll("\\\\'", " ");
                line = line.replaceAll("\\<", " ");
                line = line.replaceAll("\\>", " ");
               
                String[] tokens = line.split(" ");
                for(String token:tokens){
                    token =token.trim().toLowerCase();
                    if(token.startsWith("https://play.google.com/store/apps/details?id")||token.startsWith("http://play.google.com/store/apps/details?id")||token.contains("play.google.com/store/apps/details?id")){
                        token = token.replaceAll("\\(", "");
                        token = token.replaceAll("\\)", "");
                        token = token.replaceAll("\"", "");
                        tokenTest=token;
                        googlePlayUrl = token.substring(token.indexOf("http"));//remove everything before https://......
                        break;
                    }
                }
                if(googlePlayUrl !=null){
                    break;
                }
            }
        }catch(Exception ex){
            //System.out.println("Token="+tokenTest+" Play URL = "+googlePlayUrl);
            ex.printStackTrace();
        }
    finally{
        try{
            if(fileReader!=null){
                fileReader.close();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    return googlePlayUrl;
}
private void createDocForModel(String projectFolder, String inputDocForW2V, String inputDocForWMD, String inputDocForWMDNonStemNorm){
         //Input file which needs to be parsed
        //String fileToParse = "SampleCSVFile.csv";
        LoadStopwords("english.stop");
        BufferedReader fileReader = null;
        BufferedWriter bw_w2v = null;
        BufferedWriter bw_wmd = null;
        BufferedWriter bw_wmd_non_stem_norm = null;

        //Delimiter used in CSV file
        final String DELIMITER = "\t";
        int linecount=0;
        double projectCount=0;
        double projectWithDescriptionCount = 0;
        double avgDocLen=0;
        int totalLine = 0;
        int tokenGTFourDocCount=0;
        int noDescriptionCOunt=0;
        
        try
        {
            String line = "";
            //Create the file reader
            
            File file_w2v = new File(inputDocForW2V);
            if (!file_w2v.exists()) {
               file_w2v.createNewFile();
            }
            FileWriter fw_w2v = new FileWriter(file_w2v);
            bw_w2v = new BufferedWriter(fw_w2v);
            
            File file_wmd = new File(inputDocForWMD);
            if (!file_wmd.exists()) {
               file_wmd.createNewFile();
            }
            FileWriter fw_wmd = new FileWriter(file_wmd);
            bw_wmd = new BufferedWriter(fw_wmd); 
            
            File file_wmd_non_stem_norm = new File(inputDocForWMDNonStemNorm);
            if (!file_wmd_non_stem_norm.exists()) {
               file_wmd_non_stem_norm.createNewFile();
            }
            FileWriter fw_wmd_non_stem_norm = new FileWriter(file_wmd_non_stem_norm);
            bw_wmd_non_stem_norm = new BufferedWriter(fw_wmd_non_stem_norm); 
            //Read the file line by line
          
            File dir = new File(projectFolder);
            for(File f : dir.listFiles()){
                String fileToParse=f.getAbsolutePath()+"/description.prepro";
                File f_description ;
                f_description = new File(fileToParse);
                if(f.isDirectory() && f_description.exists()){
                   // System.out.println("F= "+f.getAbsolutePath());
                    //System.out.println("Project name = "+getProjectNameFromPath(f.getName()));
                    
                    String projectDescription = getFileContents(fileToParse);
                    //System.out.println("************\n\t "+textDes);
                    String projectName = getProjectNameFromPath(f.getName());
                    String projectDescription_stemmed_normalized  = processContent(projectDescription,true);
                    projectCount++;
                    if(projectDescription_stemmed_normalized.isEmpty() == false && projectDescription_stemmed_normalized.trim().equals("null")==false){//skip project with no valid description
                            bw_w2v.write(projectDescription_stemmed_normalized+"\n");
                            bw_wmd.write(projectName+"\t"+projectDescription_stemmed_normalized+"\n");
                            bw_wmd_non_stem_norm.write(projectName+"\t"+projectDescription+"\n");
                            projectWithDescriptionCount++ ;
                        }
                }
            }
          
        } 
        catch (Exception e) {
            e.printStackTrace();
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
                if(bw_w2v!=null)
                   bw_w2v.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedWriter"+ex);
             }
            try{
                if(bw_wmd!=null)
                   bw_wmd.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedWriter"+ex);
             }
            try{
                if(bw_wmd_non_stem_norm!=null)
                   bw_wmd_non_stem_norm.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedWriter"+ex);
             }
        }
        System.out.println("Total  doc = "+projectCount);
        System.out.println("Total doc after stem = "+projectWithDescriptionCount);
        
    }
private void createDocForWMD(String projectFolder, String inputDocForWMD, String inputDocForWMDNonStemNorm, String projectGitUrl){
    LoadStopwords("english.stop");
    BufferedReader fileReader = null;
    BufferedWriter bw_wmd = null;
    BufferedWriter bw_wmd_non_stem_norm = null;
    BufferedWriter bw_wmd_url = null;

    double projectCount=0;
    double projectWithDescriptionCount = 0;
    try
    {
        File file_wmd = new File(inputDocForWMD);
        if (!file_wmd.exists()) {
           file_wmd.createNewFile();
        }
        FileWriter fw_wmd = new FileWriter(file_wmd);
        bw_wmd = new BufferedWriter(fw_wmd); 

        File file_wmd_non_stem_norm = new File(inputDocForWMDNonStemNorm);
        if (!file_wmd_non_stem_norm.exists()) {
           file_wmd_non_stem_norm.createNewFile();
        }
        FileWriter fw_wmd_non_stem_norm = new FileWriter(file_wmd_non_stem_norm);
        bw_wmd_non_stem_norm = new BufferedWriter(fw_wmd_non_stem_norm);

        File file_wmd_url = new File(projectGitUrl);
        if (!file_wmd.exists()) {
           file_wmd.createNewFile();
        }
        FileWriter fw_wmd_url = new FileWriter(file_wmd_url);
        bw_wmd_url = new BufferedWriter(fw_wmd_url); 
        //Read the file line by line

        File dir = new File(projectFolder);
        for(File f : dir.listFiles()){
            String fileToParse=f.getAbsolutePath()+"/description.prepro";
            String configPathToParseGitUrl=f.getAbsolutePath()+"/.git/config";
            File f_description ;
            f_description = new File(fileToParse);
            if(f.isDirectory() && f_description.exists()){
               // System.out.println("F= "+f.getAbsolutePath());
                //System.out.println("Project name = "+getProjectNameFromPath(f.getName()));

                String projectDescription = getFileContents(fileToParse);
                String project_git_url = getGitUrlFromConfig(configPathToParseGitUrl);
                //System.out.println("************\n\t "+textDes);
                String projectName = getProjectNameFromPath(f.getName());
                String projectDescription_stemmed_normalized  = processContent(projectDescription,true);
                projectCount++;
                if(projectDescription_stemmed_normalized.isEmpty() == false && projectDescription_stemmed_normalized.trim().equals("null")==false){//skip project with no valid description

                        bw_wmd.write(projectName+"\t"+projectDescription_stemmed_normalized+"\n");
                        bw_wmd_non_stem_norm.write(projectName+"\t"+projectDescription+"\n");
                        bw_wmd_url.write(projectName+"\t"+project_git_url+"\n");
                        //add category info here
                        projectWithDescriptionCount++ ;
                    }
            }
        }

    } 
    catch (Exception e) {
        e.printStackTrace();
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
            if(bw_wmd!=null)
               bw_wmd.close();
         }catch(Exception ex){
             System.out.println("Error in closing the BufferedWriter"+ex);
         }
        try{
            if(bw_wmd_url!=null)
               bw_wmd_url.close();
         }catch(Exception ex){
             System.out.println("Error in closing the BufferedWriter"+ex);
         }
        try{
            if(bw_wmd_non_stem_norm!=null)
               bw_wmd_non_stem_norm.close();
         }catch(Exception ex){
             System.out.println("Error in closing the BufferedWriter"+ex);
         }
    }
    System.out.println("Total  doc = "+projectCount);
    System.out.println("Total doc after stem = "+projectWithDescriptionCount);

}

private void createAllProjectsData(String projectMetaDataPath, String projectAllDataFile){
    LoadStopwords("english.stop");
    BufferedReader fileReader = null;
    BufferedWriter bw_project = null;
    
    double projectCount=0;
    double projectWithDescriptionCount = 0;
    try
    {
        //Read the file line by line
        fileReader = new BufferedReader(new FileReader(projectMetaDataPath));
        
        File file_project = new File(projectAllDataFile);
        FileWriter fw_wmd = new FileWriter(file_project);
        bw_project = new BufferedWriter(fw_wmd); 
        bw_project.write("ProjectName"+"\t"+"GitHubURL"+"\t"+"GooglePlayURL"+"\t"+"GooglePlayCategory"+"\t"+"ProjectGitHubDescription"+"\t"+"ProjectGitHubDescription_stem"+"\t"+"ProjectGitHubReadMe"+"\t"+"ProjectGitHubReadMe_stem"+"\t"+"MethodClassName"+"\t"+"MethodClassName_stem"+"\t"+"MethodClassArgumentName"+"\t"+"MethodClassArgumentName_stem"+"\n");
        String line=null;
        while((line = fileReader.readLine()) !=null){
            String[] line_parts = line.trim().split("\t");
            String projectName = line_parts[0];
            String projectPath = line_parts[1];
            
            String projectGPlayURL = line_parts[4].trim();
            String projectCategory = line_parts[5].replace("\n", "").trim();
            
            String fileToParse_description=projectPath+"/description.prepro";
            String fileToParse_ReadMe=projectPath+"/re.prepro";
            String configPathToParseGitUrl=projectPath+"/.git/config";
            String GooglePlay_ProjectPath = projectPath.replace("java_projects", "GooglePlayPtojects");//redirect to google play project path created by Baishakhi
            String fileToParse_MethodClass = GooglePlay_ProjectPath+"/method_name_class.txt";
            String fileToParse_MethodClassArgument = GooglePlay_ProjectPath+"/method_class.txt";
            
            String projectDescription = getFileContents(fileToParse_description);
            String readMeContent = getFileContents(fileToParse_ReadMe);
            String methodClassSourceCode = getFileContents(fileToParse_MethodClass);
            String methodClassArgumentSourceCode = getFileContents(fileToParse_MethodClassArgument);
            
            projectDescription = projectName+" "+projectDescription;//add project name in description
            
            String project_git_url = getGitUrlFromConfig(configPathToParseGitUrl);
            String projectDescription_stem  = processContent(projectDescription,true);//stemmed normalized and remove stopwords
            String readMeContent_stem = processContent(readMeContent, true);
            String methodClassSourceCode_stem = processContent(methodClassSourceCode, true);
            String methodClassArgumentSourceCode_stem = processContent(methodClassArgumentSourceCode, true);
            bw_project.write(projectName+"\t"+project_git_url+"\t"+projectGPlayURL+"\t"+projectCategory+"\t"+projectDescription+"\t"+projectDescription_stem+"\t"+readMeContent+"\t"+readMeContent_stem+"\t"+methodClassSourceCode+"\t"+methodClassSourceCode_stem+"\t"+methodClassArgumentSourceCode+"\t"+methodClassArgumentSourceCode_stem+"\n");
        
            projectCount++;
        }
    } 
    catch (Exception e) {
        e.printStackTrace();
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
            if(bw_project!=null)
               bw_project.close();
         }catch(Exception ex){
             System.out.println("Error in closing the BufferedWriter"+ex);
         }
        
    }
    System.out.println("Total  project checked = "+projectCount);
    System.out.println("Total project doc after stem = "+projectWithDescriptionCount);

}

private void createDocForWMDWithCategory(String inputDocForWMDPathName, String inputDocForWMDNonStemProjectDesPathName, String projectGitUrlPathName, String projectGPlayCategoryFilePath, String projectCategoryInput, int docLenCutoff){
    LoadStopwords("english.stop");
    BufferedReader fileReader = null;
    BufferedWriter bw_wmd = null;
    BufferedWriter bw_wmd_non_stem_norm = null;
    BufferedWriter bw_wmd_url = null;
    BufferedWriter bw_wmd_category = null;

    double projectCount=0;
    double projectWithDescriptionCount = 0;
    try
    {
        File file_wmd = new File(inputDocForWMDPathName);
        
        FileWriter fw_wmd = new FileWriter(file_wmd);
        bw_wmd = new BufferedWriter(fw_wmd); 

        File file_wmd_non_stem_norm = new File(inputDocForWMDNonStemProjectDesPathName);
        
        FileWriter fw_wmd_non_stem_norm = new FileWriter(file_wmd_non_stem_norm);
        bw_wmd_non_stem_norm = new BufferedWriter(fw_wmd_non_stem_norm);

        File file_wmd_url = new File(projectGitUrlPathName);
      
        FileWriter fw_wmd_url = new FileWriter(file_wmd_url);
        bw_wmd_url = new BufferedWriter(fw_wmd_url);
        //for category
        File file_wmd_category = new File(projectGPlayCategoryFilePath);
        
        FileWriter fw_wmd_category = new FileWriter(file_wmd_category);
        bw_wmd_category = new BufferedWriter(fw_wmd_category);
        
        //Read the file line by line
        fileReader = new BufferedReader(new FileReader(projectCategoryInput));
        String line=null;
        while((line = fileReader.readLine()) !=null){
            String[] line_parts = line.trim().split("\t");
            String projectName = line_parts[0];
            String projectPath = line_parts[1];
            String projectCategory = line_parts[5];
            
            String fileToParse=projectPath+"/description.prepro";
            String fileReadMeToParse=projectPath+"/re.prepro_stem";
            String configPathToParseGitUrl=projectPath+"/.git/config";
            
            String projectDescription = getFileContents(fileToParse);
            String readMeContent = getFileContents(fileReadMeToParse);
            
            projectDescription = projectName+" "+projectDescription;//add project name in description
            //add description
            projectDescription = projectDescription+" "+readMeContent;//remove this line if you want to use only description of the project
            String projectDocument = getTopNWords(projectDescription,docLenCutoff);
            String project_git_url = getGitUrlFromConfig(configPathToParseGitUrl);
            //add readme here
            String projectDescription_stemmed_normalized  = processContent(projectDocument,true);
            //already added projectDescription_stemmed_normalized= projectName+" "+projectDescription_stemmed_normalized; //add project name in the description
            projectCount++;
            if(projectDescription_stemmed_normalized.isEmpty() == false && projectDescription_stemmed_normalized.trim().equals("null")==false){//skip project with no valid description
                    bw_wmd.write(projectName+"\t"+projectDescription_stemmed_normalized+"\n");//add readme here if needed
                    bw_wmd_non_stem_norm.write(projectName+"\t"+projectDocument+"\n");
                    bw_wmd_url.write(projectName+"\t"+project_git_url+"\n");
                    bw_wmd_category.write(projectName+"\t"+projectCategory+"\n");

                    projectWithDescriptionCount++ ;
                }

        }

    } 
    catch (Exception e) {
        e.printStackTrace();
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
            if(bw_wmd!=null)
               bw_wmd.close();
         }catch(Exception ex){
             System.out.println("Error in closing the BufferedWriter"+ex);
         }
        try{
            if(bw_wmd_url!=null)
               bw_wmd_url.close();
         }catch(Exception ex){
             System.out.println("Error in closing the BufferedWriter"+ex);
         }
        try{
            if(bw_wmd_non_stem_norm!=null)
               bw_wmd_non_stem_norm.close();
         }catch(Exception ex){
             System.out.println("Error in closing the BufferedWriter"+ex);
         }
        try{
            if(bw_wmd_category!=null)
               bw_wmd_category.close();
         }catch(Exception ex){
             System.out.println("Error in closing the BufferedWriter"+ex);
         }
    }
    System.out.println("Total  project checked = "+projectCount);
    System.out.println("Total project doc after stem = "+projectWithDescriptionCount);

}
public String getTopNWords(String document, int n){
    String[] doc_parts = document.split(" ");
    String topString="";
    for(int i=0;i<n && i<doc_parts.length;i++){
        topString = topString + " "+ doc_parts[i];
    }
    return topString.trim();
}
private void generateGooglePlayData(String projectFolder, String projectMetadataPath){
    //Input file which needs to be parsed
   //String fileToParse = "SampleCSVFile.csv";
   //LoadStopwords("english.stop");
   BufferedReader fileReader = null;
   BufferedWriter bw_meta = null;
   
   double projectCount=0;
   double projectWithGooglePlayUrl = 0;
   try
   {
       File file_meta = new File(projectMetadataPath);
       if (!file_meta.exists()) {
          file_meta.createNewFile();
       }
       FileWriter fw_meta = new FileWriter(file_meta);
       bw_meta = new BufferedWriter(fw_meta); 

       File dir = new File(projectFolder);
       for(File f : dir.listFiles()){
           String desFileToParse=f.getAbsolutePath()+"/description.prepro";
           String configPathToParseGitUrl=f.getAbsolutePath()+"/.git/config";
           String readMePath=f.getAbsolutePath()+"/README.md";
           File f_description ;
           f_description = new File(desFileToParse);
           if(f.isDirectory() && f_description.exists()){
               String projectDescription = getFileContents(desFileToParse);
               String project_git_url = getGitUrlFromConfig(configPathToParseGitUrl);
               
               String googlePlayUrl = getGooglePlayUrlFromFileContent(desFileToParse);//try from description first, it's short
               if(googlePlayUrl == null){
                   googlePlayUrl = getGooglePlayUrlFromFileContent(readMePath);//try search in readme file
               }
               
               //System.out.println("************\n\t "+textDes);
               String projectName = getProjectNameFromPath(f.getName());
               projectCount++;
              if(googlePlayUrl !=null){
                  //System.out.println("Path="+f.getAbsolutePath());
                  bw_meta.write(projectName+"\t"+f.getAbsolutePath()+"\t"+projectDescription+"\t"+project_git_url+"\t"+googlePlayUrl+"\n");
                  projectWithGooglePlayUrl++;        
              }
              if(projectCount%1000 == 0){
                  System.out.println("Project scanned so far = "+projectCount);
                  System.out.println("Google play url found = "+projectWithGooglePlayUrl);

              }
           }
       }

   } 
   catch (Exception e) {
       e.printStackTrace();
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
           if(bw_meta!=null)
              bw_meta.close();
        }catch(Exception ex){
            System.out.println("Error in closing the BufferedWriter"+ex);
        }
       
   }
   System.out.println("Total project scanned = "+projectCount);
   System.out.println("Google play url found = "+projectWithGooglePlayUrl);

}
public String getGitUrlFromConfig(String configPath){
    String gitURL=null;
    BufferedReader fileReader = null;
    File config_file = new File(configPath);
    if (!config_file.exists()) {
        System.out.println("No config"+config_file.getAbsolutePath());
        return null;
    }
    try{
        fileReader = new BufferedReader(new FileReader(config_file));
        String line;
        while((line = fileReader.readLine()) !=null){
            line = line.trim();
            if(line.startsWith("url")){
                String[] url_part = line.split("=");
                gitURL = url_part[1].trim();
                break;
            }
        }
    }catch (Exception e) {
            e.printStackTrace();
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
    return gitURL;
}
private String getProjectNameFromPath(String projectPathWithAuthorName){
    String [] name_pats = projectPathWithAuthorName.split("_");
    String projectName = "";
    if(name_pats.length>1){
        for(int i=1;i<name_pats.length;i++){
            projectName = projectName +name_pats[i]+" ";
        }
    }
    return projectName.trim();
}
public String getFileContents(String fileName) {
    //use it for small size file
    File f = new File(fileName);
    if(f.exists() ==false) { 
        return "";
    }
try {
    StringBuilder sb = new StringBuilder();
    BufferedReader in = new BufferedReader(new FileReader(fileName));
    String str;
    while ((str = in.readLine()) != null){
            //str = tokenizerNormalization(str);
            sb.append(str);
    }
    in.close();
    return sb.toString();
    }catch(FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
    }catch(IOException e) {
            e.printStackTrace();
            return null;
    }
}
private String processContent(String contents, boolean removeStopWord ){
    if(removeStopWord==true && stopwords_set  == null){
        System.out.println("Please load stopword in stopwords_set first !!!");
        return null;
    }
    String processedContent = "";
    if(contents.isEmpty()){
        return processedContent;
    }
    else{
        String[] tokens = contents.split(" ");
        for(String token:tokens){
            token = processTokenStemNormal(token);
            if(token.isEmpty()==false){
                if( removeStopWord && stopwords_set.contains(token)==false){
                    processedContent = processedContent +" "+ token;
                }
                else if(removeStopWord ==false){
                    processedContent = processedContent +" "+ token;
                }
            }
           
        }
    }
    return processedContent.trim();
}
private HashMap<String,Token> processContentToHashMap(HashMap<String,Token> tokens_map,String contents, boolean removeStopWord ){
    ArrayList<String> checkListDF = new ArrayList<>();
    if(removeStopWord==true && stopwords_set  == null){
        System.out.println("Please load stopword in stopwords_set first !!!");
    }
    if(contents.isEmpty() ==false){
       // String[] content_parts = contents.split(" ");
        String[] content_parts = tokenizer.tokenize(contents);//used open-nlp tokenizer
        for(String word:content_parts){
            word = processTokenStemNormal(word);
            if(word.isEmpty()==false){
                Token word_token = tokens_map.get(word);
                if(word_token==null){
                    word_token = new Token(word);
                }
                if( removeStopWord && stopwords_set.contains(word)==false){
                    //add word to map
                    double token_ttf = word_token.TTF;
                    word_token.TTF = token_ttf + 1;
                    if(checkListDF.contains(word)==false){ //not counted for df yet
                        double token_df = word_token.DF;
                        word_token.DF = token_df +1;
                        checkListDF.add(word);
                    }
                    tokens_map.put(word, word_token);
                }
                else if(removeStopWord ==false){
                    double token_ttf = word_token.TTF;
                    word_token.TTF = token_ttf + 1;
                    if(checkListDF.contains(word)==false){ //not counted for df yet
                        double token_df = word_token.DF;
                        word_token.DF = token_df +1;
                        checkListDF.add(word);
                    }
                    tokens_map.put(word, word_token);
                }
            }
        }
    }
    return tokens_map;
}
private String processTokenStemNormal(String token){
    token =SnowballStemming(Normalization(token)); 
    return token;
}
public void LoadStopwords(String filename) {
        stopwords_set = new HashSet<>();
    try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                    //it is very important that you perform the same processing operation to the loaded stopwords
                    //otherwise it won't be matched in the text content
                    line = processTokenStemNormal(line);
                    if (!line.isEmpty())
                            stopwords_set.add(line);
            }
            reader.close();
            System.out.format("Loading %d stopwords from %s\n", stopwords_set.size(), filename);
    } catch(IOException e){
            System.err.format("[Error]Failed to open file %s!!", filename);
    }
}
public String SnowballStemming(String token) {
    SnowballStemmer stemmer = new englishStemmer();
    stemmer.setCurrent(token);
    if (stemmer.stem())
        return stemmer.getCurrent();
    else
        return token;
}
public String Normalization(String token) {
    // remove all non-word characters
    token = token.replaceAll("\\W+", ""); 

    token = token.replaceAll("[^\\p{L}\\p{Nd}]+", ""); //for non-words
    token = token.replaceAll("[-,­.;:!?…()\\\\\\\\{}\\\\[\\\\]<>«»—'“”\\\\\\\"‘’/]", ""); //for Punctuation
    // convert to lower case
    token = token.toLowerCase(); 
    token = token.replaceAll("\\\\d+(\\\\.\\\\d+)?", "NUM"); //replace integer and double
    token = token.replaceAll("\\d", "NUM"); //replace integer and double
    return token;
}
public void writeTokenMapToFile(HashMap<String,Token> tokens, String filePath){
    FileWriter fw=null;
    try {
        fw = new FileWriter(filePath);
        for(String token:tokens.keySet()){
            fw.write(token+"\t"+tokens.get(token).TTF+"\t"+tokens.get(token).DF+"\n");
        } 
    }catch(Exception e){
        e.printStackTrace();
    }
   finally{
        
        if (fw != null)
            try{     
                fw.close();
            } catch (Exception e) {
             e.printStackTrace();
            }   
   }
}
public void featureSelection(String projectFolder, boolean removeStopWords){
    boolean includeDescription = false;
    boolean includeReadme = true;
    if(removeStopWords==true){
        LoadStopwords("english.stop");
    }
    HashMap<String, Token> desc_tokens = new HashMap<>();
    HashMap<String, Token> readme_tokens = new HashMap<>();
    try
    {
        File dir = new File(projectFolder);
        for(File f : dir.listFiles()){

            if(includeDescription){
                String fileToParse=f.getAbsolutePath()+"/description.prepro";
                File f_description ;
                f_description = new File(fileToParse);
                if(f.isDirectory() && f_description.exists()){//if description exist
                    String projectDescription = getFileContents(fileToParse);
                    desc_tokens = processContentToHashMap(desc_tokens, projectDescription,removeStopWords);//add to hashmap with necessary processing
                } 
            }
            if(includeReadme){
                String fileToParse=f.getAbsolutePath()+"/README.md";
                File f_readme ;
                f_readme = new File(fileToParse);
                if(f.isDirectory() && f_readme.exists()){//if description exist
                    String readMeContent = getFileContents(fileToParse);
                    readme_tokens = processContentToHashMap(readme_tokens, readMeContent,removeStopWords);//add to hashmap with necessary processing
                }
            }
            
         }
    } 
    catch (Exception e) {
        e.printStackTrace();
    }
    
    //writeTokenMapToFile(desc_tokens, "description_token.txt");
    writeTokenMapToFile(readme_tokens, "readme_token.txt");

    System.out.println("Total  unigram = "+readme_tokens.size());
    //System.out.println("Total doc after stem = "+projectWithDescriptionCount);

}
public String SingleLinetokenizeNormalizeStemm(String input_str){
    String processedLine="";
    String[] str_parts = tokenizer.tokenize(input_str);
    for(String str:str_parts){
        str = processTokenStemNormal(str);
        if(str.isEmpty() ==false){
            processedLine = processedLine + " "+str;
        }
    }
    return processedLine.trim();
}
public void tokenizeNormalizeStemmTextData(String textFilePath){
    BufferedReader input =null;
    BufferedWriter output = null;
    int processLineCount = 0;
    try{
        input = new BufferedReader(new FileReader(textFilePath));
        //String outputfilepath = textFilePath.replaceFirst(".", "_normstem.");
        String outputfilepath ="wiki_english_normstem.txt";
        output = new BufferedWriter(new FileWriter(outputfilepath));
        String str;
        while ((str = input.readLine()) != null){
            String processed_str = SingleLinetokenizeNormalizeStemm(str);
            if(processed_str.isEmpty() ==false){
               output.write(processed_str+"\n"); 
            }
            if(processLineCount%100000 ==0){
                System.out.println("Line completed = "+processLineCount);
            }
            processLineCount++;
        }
    
    }
    catch (Exception e) {
            e.printStackTrace();
        } 
        finally
        {
            try{
                if(input!=null)
                   input.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedReader"+ex);
             }
            try{
                if(output!=null)
                   output.close();
             }catch(Exception ex){
                 System.out.println("Error in closing the BufferedWriter"+ex);
             }
        }
    }
}
