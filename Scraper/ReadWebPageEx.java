import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ReadWebPageEx {

    public static void main(String[] args) throws MalformedURLException, IOException {

        BufferedReader br = null;

        try {

            URL url = new URL("https://www.nhl.com");
            br = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;

            StringBuilder sb = new StringBuilder();

            while ((line = br.readLine()) != null) {

                sb.append(line);
                sb.append(System.lineSeparator());
            }

            String text = sb.toString();

            System.out.print(text);

            if (text.contains("B0")) {
                int i = text.indexOf("B0");
                int f = i + 8;
                System.out.println(i);
                System.out.println(text.substring(i, f));
                System.out.println("data-asin found");}

            else {System.out.println("not found");}

        } finally {

            if (br != null) {
                br.close();
            }
        }
    }
}