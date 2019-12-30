import com.jaunt.*;
import com.jaunt.component.*;

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class JauntScraper {

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

    Map<String, String> asinToPrice = getPrice(asinSet);

    for (String asin : asinSet) {
      System.out.println(asin + " => " + asinToPrice.get(asin)); 
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
    UserAgent userAgent = new UserAgent();

    for (String barcode : barcodes) {
      try {userAgent.visit("https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=" + barcode);}
      catch(JauntException e){System.err.println(e);}

      String stringHtml = userAgent.doc.innerHTML().toString();

      while (stringHtml.contains("To discuss automated access to Amazon data please contact api-services-support@amazon.com.")) {
        try {userAgent.visit("https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=" + barcode);}
        catch(JauntException e){System.err.println(e);}
        stringHtml = userAgent.doc.innerHTML().toString();
      }

        if (stringHtml.contains("data-asin")) {
          int i = stringHtml.indexOf("data-asin") + 11;
          int f = i + 10;
          //System.out.println(i);
          barcodeToAsin.put(barcode, stringHtml.substring(i, f));
          //System.out.println(barcode + " has " + stringHtml.substring(i, f));
        }

        else {
          if (!stringHtml.contains("To discuss automated access to Amazon data please contact api-services-support@amazon.com.")) {

            barcodeToAsin.put(barcode, "no ASIN for this barcode");
          }
        }
    }

    return barcodeToAsin;
  }

  public static Map<String, String> getPrice(final Set<String> asins) {
    Map<String, String> asinToPrice = new HashMap<>();
    UserAgent userAgent = new UserAgent();

    for (String asin : asins) {
      try {userAgent.visit("https://www.amazon.com/dp/" + asin);}
      catch (JauntException e) {System.err.println(e);}

      String stringHtml = userAgent.doc.innerHTML().toString();
              
      if (stringHtml.contains("priceblock_ourprice")) {
        int i = stringHtml.lastIndexOf("priceblock_ourprice\" class=\"a-size-medium a-color-price priceBlockBuyingPriceString") + 85;
        int f = i + 7;
        asinToPrice.put(asin, stringHtml.substring(i, f));
      }
    }

    return asinToPrice;
  }
}






















