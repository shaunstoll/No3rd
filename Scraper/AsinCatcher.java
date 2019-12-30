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

public class AsinCatcher {

	public static void main(String[] args) {
		if (args.length != 1) {
			final String msg = "Enter barcode file as second argument."; 
			System.err.println(msg);
			throw new IllegalArgumentException(msg);
		}
		String file = args[0];
		Set<String> barcodeSet = getBarcodes(file);

		// for (String s : barcodeSet) {System.out.println(s);}
		// System.out.println();

		Map<String, String> barcodeToAsin = getAsin(barcodeSet);
		Set<String> asinSet = new HashSet<>();

		for (String barcode : barcodeSet) {
			if (barcodeToAsin.get(barcode) != null) {
				System.out.println(barcode + " => " + barcodeToAsin.get(barcode));

				if (barcodeToAsin.get(barcode) != "no ASIN for this barcode") {asinSet.add(barcodeToAsin.get(barcode));}
			}
		}
	}

	public static Set<String> getBarcodes(final String str) {
		Set<String> barcodeSet = new HashSet<>();
		final File input = new File(str);

		try {
			Scanner scanner = new Scanner(input);

		 	while (scanner.hasNextLine()) {
	            barcodeSet.add(scanner.nextLine());
	        }
	        scanner.close();
	    } 
	    catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }
	    return barcodeSet;
	}
	
	public static Map<String, String> getAsin(final Set<String> barcodes) {
	 	Map<String, String> barcodeToAsin = new HashMap<>();
		for (String barcode : barcodes) {

			try {
            	Document doc = Jsoup.connect("https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=" + barcode).get();
           		String stringHtml = doc.toString();

	            if (stringHtml.contains("data-asin")) {
	                int i = stringHtml.indexOf("data-asin") + 11;
	                int f = i + 10;
	                //System.out.println(i);
	                barcodeToAsin.put(barcode, stringHtml.substring(i, f));
	                //System.out.println(barcode + " has " + stringHtml.substring(i, f));
           		}

           		if (!stringHtml.contains("To discuss automated access to Amazon data please contact api-services-support@amazon.com.")) {
           			barcodeToAsin.put(barcode, "no ASIN for this barcode");
           		}
            }

            catch (IOException e) {System.out.println(e);} 
		}

		return barcodeToAsin;
	}
}





















