/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Evaluation;

import Utility.Utility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author masud
 */
public class Evaluation {
    Utility util;
    

public static void main(String args[]) {
  Evaluation eval = new Evaluation();
  //eval.generateEvaluationResult();
   eval.runEvaluationCommand(Integer.parseInt(args[0]),Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    
    
}
public Evaluation(){
    util = new Utility();
}
public void generateEvaluationResult(){
    /*for(int cat_freq=20;cat_freq<61;cat_freq = cat_freq+20){
        for(int readme_freq=0;readme_freq<11;readme_freq = readme_freq+10){
            for(int code_freq=0;code_freq<11;code_freq = code_freq+10){
                runEvaluationCommand(50, readme_freq, code_freq, cat_freq);
            }
        }
    }*/
    runEvaluationCommand(50, 0, 0, 60);
    System.out.println("Evaluation Completed.");
}
public void runEvaluationCommand(int queryNum, int readmeCutoff, int mcCutoff, int categoryCutoff){
    String cutoff = queryNum +" "+readmeCutoff+" "+mcCutoff+" "+categoryCutoff;
    String printout = util.executeCommnad("/if24/mr5ba/Masud/RunServer/SimilarProject/wmddataprocess/project_data_jar.sh "+cutoff);
    printout = util.executeCommnad("/if24/mr5ba/Masud/RunServer/SimilarProject/current_wmd/BuildVectorPK/trainBuildWMDVectorPK.sh");
    printout = util.executeCommnad("/if24/mr5ba/Masud/RunServer/SimilarProject/current_wmd/BuildVectorPK/testBuildWMDVectorPK.sh");
    printout = util.executeCommnad("/if24/mr5ba/Masud/RunServer/SimilarProject/current_wmd/search.sh");
    System.out.println(queryNum +"\t"+readmeCutoff+"\t"+mcCutoff+"\t"+categoryCutoff+"\t"+printout);
    
}
public static void runBashScript() {
        try {
            Process proc = Runtime.getRuntime().exec("/home/masud/NetBeansProjects/NetbeanData/SimilarProjectData/pkbuilder/testBuildWMDVectorPK.sh"); //Whatever you want to execute
            BufferedReader read = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));
            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            while (read.ready()) {
                System.out.println(read.readLine());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
