package org.simonjamesrowe.productscraper;

import java.util.List;

import lombok.Data;

@Data
public class Product {
    private String externalProductId;
    private String name;
    private List<String> images;
    private String price;
    private String description;
    private String colour;
    private String sku;
    private String materialComposition;
    private List<String> variantUris;
    private String productUrl;
}
