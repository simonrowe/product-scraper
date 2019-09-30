package org.simonjamesrowe.productscraper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;

@SpringBootApplication
@Log4j2
@EnableAsync
@EnableScheduling
public class CrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
    }

    @Bean
    public CommandLineRunner crawler(final ProductService productService, final ChromeDriverPool pool) {
        return (args) -> {
            log.info("Running this mini application");
            //System.setProperty("webdriver.chrome.driver", "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.setHeadless(true);
            ChromeDriver driver = new ChromeDriver(chromeOptions); 

            // And now use this to visit Google
            //driver.get("https://www.topshop.com/en/tsuk/category/clothing-427/dresses-442");
            driver.get("https://www.topshop.com/en/tsuk/category/jeans-6877054");

            List<Future<Product>> productFutures = new LinkedList<>();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            var pageNumber = searchPageNumber(driver.getCurrentUrl());
            var newPageNumber = 1;
            do {
                pageNumber = newPageNumber;
                js.executeScript("window.scrollBy(0, -100)");
                Thread.sleep(150);
                js.executeScript("window.scrollBy(0,100000)");

                Thread.sleep(1500);
                newPageNumber = searchPageNumber(driver.getCurrentUrl());
                log.info("loaded page {}", newPageNumber);

            } while (newPageNumber > pageNumber);

            List<WebElement> productLinks = driver.findElementsByXPath("//a[@class='Product-nameLink']");
            AtomicInteger count = new AtomicInteger();
            productLinks.stream().map(linkElement -> linkElement.getAttribute("href")).forEach(link -> {
                count.incrementAndGet();
                productFutures.add(productService.productDetails(link, true));
            });

            log.info("Processing {} skus", count.get());
            
            List<Future<Product>> variantFutures = new LinkedList<>();
            productFutures.forEach(pf -> {
                try {
                    count.decrementAndGet();
                    Product p = pf.get();
                    if (count.get() % 10 == 0) {
                        log.info("{} products left to process", count.get());
                    }

                    if (!CollectionUtils.isEmpty(p.getVariantUris())) {
                        p.getVariantUris().forEach(uri -> {
                            //count.incrementAndGet();
                            variantFutures.add(productService.productDetails(uri, true));
                        });
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            });

            log.info("Processing variants");
            variantFutures.forEach(f -> {
                try {
                    f.get();
                } catch (Exception e) {
                    log.error(e);
                }
            });

            driver.close();
            log.info("Processing {} skus", count.get());

            
            pool.onDestroy();
            System.exit(0);
        };
    }


    protected static final Integer searchPageNumber(String url) {
        Matcher matcher = Pattern.compile("currentPage=\\d+").matcher(url);
        if (matcher.find()) {
            String fragment = matcher.group();
            return Integer.valueOf(fragment.substring(fragment.indexOf("=") + 1));
        }

        return 1;
    }
}
