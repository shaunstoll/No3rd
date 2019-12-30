import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class PriceJauntScraper {
	public static void main(String[] args) {
		if (args.length != 1) {
			final String msg = "Enter barcode file as second argument."; 
			System.err.println(msg);
			throw new IllegalArgumentException(msg);
		}
		String file = args[0];
		Set<String> asinSet = getAsins(file);

		Map<String, String> asinToPrice = getPrice(asinSet);
		Set<String> priceSet = new HashSet<>();

		for (String asin : asinSet) {
			if (asinToPrice.get(asin) != null) {
				System.out.println(asin + " => " + asinToPrice.get(asin));
			}
		}
	}

	public static Set<String> getAsins(final String str) {
		Set<String> asinSet = new HashSet<>();
		final File input = new File(str);

		try {
			Scanner scanner = new Scanner(input);

		 	while (scanner.hasNextLine()) {
	            asinSet.add(scanner.nextLine());
	        }
	        scanner.close();
	    } 
	    catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }
	    return asinSet;
	}

	public static Map<String, String> getPrice(final Set<String> asins) {
	 	Map<String, String> asinToPrice = new HashMap<>();

	 	for (String asin : asins) {

			try {
            	Document doc = Jsoup.connect("https://www.amazon.com/dp/" + asin).get();
           		String stringHtml = doc.toString();
           		
	            if (stringHtml.contains("priceblock_ourprice")) {
	                int i = stringHtml.indexOf("priceblock_ourprice\" class=") + 85;
	                int f = i + 7;
	                asinToPrice.put(asin, stringHtml.substring(i, f));
           		}
           	}

           	catch (IOException e) {System.out.println(e);} 
        }

        return asinToPrice;
	}
}