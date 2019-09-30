package org.simonjamesrowe.productscraper.ingest;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class InputData {
    
    private String searchKeywords;

    private String manufacturerName;
    
    private String packageWeight;

    private String isbn;
    
    private String packageWidth;

    private String baseCurrency;

    private String productName;

    private String ean;

    private String productTaxCode;

    private String exactSize;

    private String packageWeightUOM;
    
    private String packageLength;

    private String model;
    
    private String sku;

    private String exactColour;

    private String productDescription;

    private String productType;

    private String productFeatures;

    private String brandName;

    private String packageDimensionsUOM;
    
    private String costPrice;

    private String epid;

    private String upc;

    private String mpn;

    private String voloCategoryId;

    private String gtin14;

    private String brandType;

    private String packageHeight;

    private String material;

    private Boolean adultProduct;

    private List<String> imageUrls;
    
    private String asin;

    private String countryOfOrigin;
    
    private Map<String, String> categorySpecifics;

    private String colourCategory;

    private String retailPrice;

    private List<String> listOnChannels;
    
    private String productUrl;
    
    private Integer itemPackageQuantity;
    
}
