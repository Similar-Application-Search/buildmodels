/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GitHubDataProcess;

import java.net.MalformedURLException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Masud
 */
public class GitHubData {
    public static void main(String[] args){
        GitHubData gitHubData = new GitHubData();
        //gitHubData.getContentFromUrl("https://raw.githubusercontent.com/octokit/octokit.rb/master/README.md");
       // System.out.println(System.currentTimeMillis()/1000);
        //System.out.println();
        //String repourl= gitHubData.getProjectRepoUrlPart("https://api.github.com/repos/Bronsa/brochure");
        //String url_readme = gitHubData.retrieveReadmeUrlFromRepo(repourl+"/readme");
        //gitHubData.getContentFromUrl(url_readme);
        //gitHubData.collectReadmeUrl("/home/masud/NetBeansProjects/NetbeanData/SimilarProjectData/project_java.csv");
        /*if(args.length !=4){
            System.out.println("Please give proper param: project_file, linestatus, gitmetadatadest, settings");
        }
        //gitHubData.collectReadmeUrl("/home/masud/NetBeansProjects/NetbeanData/SimilarProjectData/project_java.csv","lineStatus.txt","GitMetaDataJava_Part1.txt","settings.txt");
        Date currentDate = new Date();
        System.out.println("Start collecting at "+currentDate.toString());
        gitHubData.collectReadmeUrl(args[0],args[1],args[2],args[3]);
        System.out.println("End collecting at "+currentDate.toString());
        System.out.println("Please wait at leat 1 hour before running again");
        */
        //String cat = gitHubData.getCategoryFromPlayUrl("com.educ8s.triviaquiz2015");
        //System.out.println("cat = "+cat);
        gitHubData.processCategory("/home/masud/NetBeansProjects/NetbeanData/SimilarProjectData/GPlayMetaData_v2.txt", "CategoryGooglePlayMetaData_v3.txt");
        //gitHubData.processCategory(args[0],args[1]);
    
    }
public void processCategory(String GPUrlDataPath, String outputDataPath){
   BufferedReader fileReader = null;
   BufferedWriter bw_meta = null;
   int startCount = 800;
   int endCount=900;
   double projectCount=0;
   double projectWithGooglePlayUrl = 0;
   try
   {
       File file_meta = new File(outputDataPath);
       if (!file_meta.exists()) {
          file_meta.createNewFile();
       }
       FileWriter fw_meta = new FileWriter(file_meta,true);
       bw_meta = new BufferedWriter(fw_meta); 

       File gpURL = new File(GPUrlDataPath);
       fileReader = new BufferedReader(new FileReader(gpURL));
       String line;
        while((line = fileReader.readLine()) !=null){
            projectCount++;
            if((projectCount>=startCount && projectCount<endCount)){
                    //continue;

                //projectWithGooglePlayUrl++;
                String[] line_parts = line.split("\t");
                String playURL = line_parts[4].trim();
                String category = getCategoryFromPlayUrl(playURL);
                if(category.isEmpty() == false){
                    bw_meta.write(line+"\t"+category+"\n");
                    projectWithGooglePlayUrl++;
                    System.out.println("cat = "+category + " Number of Inaccesssible url = "+ (projectCount-projectWithGooglePlayUrl)+"Completed = "+projectCount);
                }
            }
            else{
                //break;
            }
            //System.out.println("cat = "+category);
           
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
public String getGPStoreID(String playURL){
    String playID=null;
    System.out.println("url="+playURL);
    playID = playURL.trim().split("id=")[1];
    return playID;
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
                System.out.println(line);
                if(line.startsWith("There was")){
                    System.out.println("Line:"+line);
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
            System.out.println("line start");
            Logger.getLogger(GitHubData.class.getName()).log(Level.SEVERE, null, ex);
             System.out.println("line end");
        }
    return category;
}
public void collectReadmeUrl(String projectDataFilePath, String statusFile, String gitMetaData, String settingPath){
    BufferedReader fileReader = null;
    BufferedReader readStatus = null;
    BufferedReader readSettings = null;
    BufferedWriter fileWriter = null;
    BufferedWriter statusWriter = null;
    int startFromLine = 0;
    int gitApiHitCount = 0;
    
   //github api  key info
    String client_id = "7a0aaefeb182be13f210";
    String client_secret = "f7bab0eec626d4f44dcd1dc4f813b9812c53793f";
    try{
        fileReader = new BufferedReader(new FileReader(projectDataFilePath));
        //readStatus = new BufferedReader(new FileReader("lineStatus.txt"));
        //fileWriter = new BufferedWriter(new FileWriter("/home/masud/NetBeansProjects/NetbeanData/SimilarProjectData/project_java.csv",true));
        
        readStatus = new BufferedReader(new FileReader(statusFile));
        readSettings = new BufferedReader(new FileReader(settingPath));
        
        fileWriter = new BufferedWriter(new FileWriter(gitMetaData,true));
        String startLine = null;
        String clientIdLine="";
        String clientSecretLine="";
        if((clientIdLine = readSettings.readLine()) != null && (clientSecretLine = readSettings.readLine()) != null){
            client_id = clientIdLine.trim().split(":")[1].trim();
            client_secret = clientSecretLine.trim().split(":")[1].trim();
        }else{
            System.out.println("Please give valid client id and security code for GitHub API, We are using default key id now !!");
            //return;
        }
        if((startLine = readStatus.readLine()) !=null){
            startFromLine = Integer.parseInt(startLine.trim());
             System.out.println("Start at line = "+startLine);
        }else{
            System.out.println("No start line found, starting from the beginning");
        }
        String line;
        String writeLine;
        int lineCount = 0;
        while((line = fileReader.readLine()) != null){
            
            if(lineCount<startFromLine){
                lineCount++;
                continue;
            }
            lineCount++;
            String[] line_parts = line.split("\t");
            if(startFromLine==0){
                writeLine = line+"\t"+"readme_url";
                fileWriter.write(writeLine+"\n");
                startFromLine++;//next line
                continue;
            }
            startFromLine++;//next line
            if(line_parts.length>1){
                String apiGitUrl = line_parts[1].trim();
                String repoUrl = getProjectRepoUrlPart(apiGitUrl);
                gitApiHitCount++;//api hit count increase
                if(repoUrl !=null){
                    //Date currentDate = new Date();
                    //System.out.println("start gitapi "+currentDate.toString()+" Repo:"+repoUrl+"/readme");
                    String gitReadMeUrl = retrieveReadmeUrlFromRepo(repoUrl+"/readme",client_id,client_secret);
                    
                    //currentDate = new Date();
                   // System.out.println("end gitapi "+currentDate.toString());
                    
                    //System.out.println("Repo URL = "+repoUrl);
                    //System.out.println("Readme URL = "+gitReadMeUrl);
                    
                    if(gitReadMeUrl.isEmpty()){
                        writeLine = line+"\t"+"null";
                    }else{ 
                        writeLine = line+"\t"+gitReadMeUrl;
                    }
                    fileWriter.write(writeLine+"\n");
                }
            }
            if(gitApiHitCount>4900){//maximum 5000 api call per hour
               /* Date currentDate = new Date();
                //toString would print the full date time string
                System.out.println("Start sleep at "+currentDate.toString());
                TimeUnit.MINUTES.sleep(65);
                //TimeUnit.SECONDS.sleep(15);
                currentDate = new Date();
                System.out.println("End sleep at "+currentDate.toString()+"\t start executing again");
                gitApiHitCount=0;*/
                break;
            }
           if(gitApiHitCount%500 == 0){
               System.out.println("Completed GitApi call in this session so far = "+gitApiHitCount);
           }
        }
        System.out.println("Completed GitApi call in this session final = "+gitApiHitCount);
        
    }catch(Exception ex){
        ex.printStackTrace();
    }
    finally{
        try{
            if(fileReader !=null){
                fileReader.close();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        try{
            if(readStatus !=null){
                readStatus.close();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        try{
            if(fileWriter !=null){
                fileWriter.close();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
    }
    try{
       statusWriter = new BufferedWriter(new FileWriter(statusFile)); 
       statusWriter.write(startFromLine+"");//save updated value
    }catch(Exception ex){
        ex.printStackTrace();
    }
    finally{
        try{
            if(statusWriter !=null){
                statusWriter.close();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
public String getProjectRepoUrlPart(String completeGitUrl){
//    /https://api.github.com/repos/SnowblindFatal/Glomes
    String repoUrl = null;
    String[] url_parts = completeGitUrl.split("repos/");
    if(url_parts.length>1){
        repoUrl = url_parts[1].trim();
    }
    return repoUrl;
}
public void getContentFromUrl(String weburl){
        try {
            // Make a URL to the web page
            URL url = new URL(weburl);
            
            // Get the input stream through URL Connection
            URLConnection con = url.openConnection();
            InputStream is =con.getInputStream();
            
            // Once you have the Input Stream, it's just plain old Java IO stuff.
            
            // For this case, since you are interested in getting plain-text web page
            // I'll use a reader and output the text content to System.out.
            
            // For binary content, it's better to directly read the bytes from stream and write
            // to the target file.
            
            
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            
            String line = null;
            
            // read each line and write to System.out
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception ex) {
            Logger.getLogger(GitHubData.class.getName()).log(Level.SEVERE, null, ex);
        }
}
public String retrieveReadmeUrlFromRepo(String repositoryUrl, String client_id, String client_secret) {
    String url_part="";
      try{
        /* for example of curl command*/
        // curl 'https://api.github.com/repos/defunkt/jquery-pjax?client_id=1ec30d10a88b58b195d3&client_secret=6a022b4f38783acef7a777f22c7056710c1563e8'
       // String client_id = "7a0aaefeb182be13f210";
        //String client_secret = "f7bab0eec626d4f44dcd1dc4f813b9812c53793f";
        String commandLine = "curl '" + "https://api.github.com/repos/" + repositoryUrl + "?client_id=" + client_id + "&client_secret=" + client_secret + "'";
        //System.out.println(commandLine);
        String[] cmd = new String[]{"/bin/bash", "-c", commandLine};
                    ProcessBuilder pb = new ProcessBuilder().command(cmd);
                    Process p = pb.start();
                    //System.out.println("Start waiting");
                    //int exit = p.waitFor();
                     //System.out.println("end wait waiting");
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
              /*if(exit != 0){
                      System.out.println("Not normal process **********");
                            //System.exit(exit);
                    }*/

                    /*
      URL gitApiUrl = new URL("https://api.github.com/repos/" + repositoryUrl);
      //BufferedReader readUrl = new BufferedReader(new InputStreamReader(gitApiUrl.openStream()));
      URLConnection conn = gitApiUrl.openConnection();
  conn.addRequestProperty("User-Agent", "msesmart");
  BufferedReader readUrl = new BufferedReader(new InputStreamReader(conn.getInputStream())); */
                    String line, readmeUrl;
                    while((line = reader.readLine()) != null) {
                            line = line.trim();
                            //System.out.println(line);
                            if(line.startsWith("\"download_url\"")){
                                url_part = line.substring(15);
                                //System.out.println("Readme Download URL ="+url_part);
                                url_part = url_part.replaceAll("\"", "");
                                url_part = url_part.replaceAll(",", "");
                                //System.out.println("Readme Download URL Final="+url_part);
                                break;
                            }
                            
                    }
                    return url_part;
            } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return "";
            } catch(IOException e){
                    e.printStackTrace();
                    return "";
            } /*catch (InterruptedException e) {
                    e.printStackTrace();
                    return "";
            }*/
    }


}
