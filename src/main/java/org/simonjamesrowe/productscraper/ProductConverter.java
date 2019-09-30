package org.simonjamesrowe.productscraper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.simonjamesrowe.productscraper.ingest.InputData;
import org.simonjamesrowe.productscraper.ingest.RawData;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class ProductConverter implements Converter<Product, RawData> {

    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Override public RawData convert(Product product) {

        RawData rawData = new RawData();
        rawData.setTenantId("2222");
        rawData.setExternalProductId(product.getExternalProductId());

        try {
            rawData.setSource(objectMapper.readValue(objectMapper.writeValueAsBytes(product), Map.class));
        } catch (IOException e) {
            log.error(e);
        }

        InputData mapped = new InputData();
        mapped.setSearchKeywords("");
        mapped.setManufacturerName("Topshop");
        mapped.setPackageWeight("");
        mapped.setIsbn("");
        mapped.setPackageWidth("");
        mapped.setBaseCurrency("GBP");
        mapped.setProductName(product.getName());
        mapped.setEan("");
        mapped.setProductTaxCode("");
        mapped.setExactSize("");
        mapped.setPackageWeightUOM("");
        mapped.setPackageLength("");
        mapped.setModel(product.getSku());
        mapped.setSku(product.getSku());
        mapped.setExactColour(product.getColour());
        mapped.setProductDescription(product.getDescription());
        mapped.setProductType("");
        mapped.setProductFeatures("");
        mapped.setBrandName("");
        mapped.setPackageDimensionsUOM("");
        mapped.setCostPrice("");
        mapped.setEpid("");
        mapped.setUpc("");
        mapped.setVoloCategoryId("");
        mapped.setMpn("");
        mapped.setGtin14("");
        mapped.setBrandType("PREMIUM");
        mapped.setPackageHeight("");
        mapped.setMaterial(product.getMaterialComposition());
        mapped.setAdultProduct(false);
        mapped.setImageUrls(product.getImages());
        mapped.setAsin("");
        mapped.setCountryOfOrigin("");
        mapped.setCategorySpecifics(null);
        mapped.setColourCategory("");
        mapped.setRetailPrice(product.getPrice());
        mapped.setListOnChannels(List.of("google-uk", "amz-gb"));
        mapped.setProductUrl(product.getProductUrl());
        mapped.setItemPackageQuantity(1);
        rawData.setMapped(mapped);

        return rawData;
    }
}
