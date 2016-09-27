/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ProcessData;

/**
 *
 * @author Masud
 */
public class Token {
    public String value;
    public double TTF;//total term frequency
    public double DF;//document frequency
    public double TFIDF;
    public Token(String str){
        value = str;
        TTF =0.0;
        DF =0.0;
        TFIDF=0.0;
    }
}
