package org.simonjamesrowe.productscraper;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.simonjamesrowe.productscraper.ingest.RawData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Log4j2
public class ProductService {
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ChromeDriverPool pool;
    
    @Autowired
    private ProductConverter productConverter;

    @Async
    public Future<Product> productDetails(String productUrl, boolean isVariant) {
        log.debug("Processing Product {}", productUrl);
        ChromeDriver driver = pool.borrow();
        Product product = new Product(); 
        try {
            driver.get(productUrl);


            product.setName(
                driver.findElementByXPath("//div[contains(@class, 'ProductDetail-details')]/div/h1").getText());
            product.setPrice(extractPrice(driver
                .findElementByXPath("//div[contains(@class, 'ProductDetail-details')]//span[contains(@class, 'Price')]")
                .getText()));
            product.setDescription(driver.findElementByXPath("//div[@class='ProductDescription-content']").getText());

            try {
                String irrelevantDescriptionParts =

                    driver.findElementByXPath("//div[@class='ProductDescription-content']//b").getText();
                product.setDescription(
                    product.getDescription().replace(irrelevantDescriptionParts, "").replaceAll("(\\n|\\r)", "")
                        .trim());
            } catch (NoSuchElementException ex) {
            }

            product.setMaterialComposition(extractMaterialComposition(product.getDescription()));
            product.setDescription(product.getDescription().replace(product.getMaterialComposition() + ". ", ""));

            List<String> extraAttributes =
                driver.findElementsByXPath("//li[@class='ProductDescriptionExtras-item']").stream()
                    .map(e -> e.getText())
                    .collect(toList());
            product.setColour(extractColor(extraAttributes));
            product.setSku(extractSku(extraAttributes));
            product.setExternalProductId(extractExternalId(productUrl));
            product.setImages(findImages(driver));
            if (!isVariant) {
                product.setVariantUris(extractVariantUris(driver).stream()
                    .filter(uri -> !product.getExternalProductId().equals(extractExternalId(uri))).collect(
                        Collectors.toUnmodifiableList()));
                if (CollectionUtils.isEmpty(product.getVariantUris())) {
                    product.setVariantUris(extractColorVariants(driver));
                }
            }
            product.setProductUrl(productUrl);
            writeProductToFile(product);
        } catch (Exception e) {
            log.error("An error has occured with product " + productUrl, e);
        }
        log.info("Extracted Product {}", product);

        pool.returnDriver(driver);
       
        return new AsyncResult<>(product);
    }

    private void writeProductToFile(Product product) throws IOException {
        RawData data = productConverter.convert(product);
        File out = new File("out");
        if (!out.exists()) out.mkdir();
        objectMapper.writeValue(new File(out, product.getExternalProductId() + ".json"), data);
    }

    private List<String> extractColorVariants(ChromeDriver driver) {
        final List<String> colorVariants = new ArrayList<>();
        final List<String> styles =
            driver.findElementsByXPath("//div[@class='ProductDetail-topGroupRightInner']//a[@class='Swatch-link']")
                .stream().map(e -> e.getAttribute("style").replace(";", "")).collect(
                Collectors.toUnmodifiableList());

        String productUrl = driver.getCurrentUrl();
        for (String style : styles) {
            driver.findElementsByXPath("//div[@class='ProductDetail-topGroupRightInner']//a[@class='Swatch-link']")
                .stream().filter(e -> e.getAttribute("style").contains(style)).collect(
                Collectors.toUnmodifiableList()).get(0).click();
            try {
                Thread.sleep(100);
            } catch (Exception ex) {
            }
            colorVariants.add(driver.getCurrentUrl());
            driver.get(productUrl);
        }

        return colorVariants;
    }

    private List<String> extractVariantUris(ChromeDriver driver) {
        return driver.findElementsByXPath("//div[@class='ProductDetail-topGroupRightInner']//a").stream()
            .map(e -> e.getAttribute("href")).filter(uri -> uri != null && uri.contains("/product"))
            .collect(Collectors.toUnmodifiableList());
    }

    protected String extractMaterialComposition(String description) {
        Matcher matcher = Pattern.compile("(\\d+%\\s\\w+(,)?+(\\s)?+)+\\.").matcher(description);
        if (matcher.find()) {
            String fragment = matcher.group();
            return fragment.substring(0, fragment.length() - 1);
        }
        return "";

    }

    private List<String> findImages(ChromeDriver driver) throws Exception {
        List<String> images = new ArrayList<>();
        String nextImage = driver.findElementByXPath("//div[@class='ProductDetail-media']//img").getAttribute("src");
        nextImage = nextImage.substring(0, nextImage.indexOf("?"));
        while (!images.contains(nextImage)) {
            images.add(nextImage);
            WebElement nextImageButton =
                driver.findElementByXPath("//button[@class='Carousel-arrow Carousel-arrow--right']");
            nextImageButton.click();
            Thread.sleep(25);
            List<WebElement> imageElements = driver.findElementsByXPath("//div[@class='ProductDetail-media']//img");
            nextImage = imageElements.get(imageElements.size() - 1).getAttribute("src");
            nextImage = nextImage.substring(0, nextImage.indexOf("?"));
        }
        return images;
    }

    private String extractExternalId(String productUrl) {
        return productUrl.substring(productUrl.lastIndexOf("/") + 1);
    }

    private String extractColor(List<String> attribtues) {
        for (String attribute : attribtues) {
            Matcher matcher = Pattern.compile("Colour:.+\\w+").matcher(attribute);
            if (matcher.find()) {
                String fragment = matcher.group();
                return attribute.substring(attribute.indexOf(":") + 1).trim();
            }
        }
        return "";
    }

    private String extractSku(List<String> attribtues) {
        for (String attribute : attribtues) {
            Matcher matcher = Pattern.compile("Product\\sCode:.+\\w+").matcher(attribute);
            if (matcher.find()) {
                String fragment = matcher.group();
                return attribute.substring(attribute.indexOf(":") + 1).trim();
            }
        }
        return "";
    }

    private String extractPrice(String text) {
        String price = "";
        Matcher matcher = Pattern.compile("\\d+\\.\\d+").matcher(text);
        while (matcher.find()) {
            price = matcher.group();
        }
        return price;

    }

}
