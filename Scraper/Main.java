import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
 
public class Main {
	public static void main(String[] args) {
        try {
            Document doc = Jsoup.connect("https://www.amazon.com/s?k=628949040248&ref=nb_sb_noss").get();
            String string = doc.toString();

            if (string.contains("asin")) {
                int i = string.indexOf("asin");
                int f = i + 18;
                System.out.println(i);
                System.out.println(string.substring(i, f));
                System.out.println("asin found");}

            else {System.out.println("asin not found");}
        }
        catch (IOException e) {
            System.out.println(e);
        }    
    }
}

    

