package org.simonjamesrowe.productscraper;

import java.util.List;
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

@SpringBootApplication
@Log4j2
@EnableAsync
@EnableScheduling
public class CrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner crawler(final ProductService productService) {
        return (args) -> {
            log.info("Running this mini application");
            //System.setProperty("webdriver.chrome.driver", "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.setHeadless(true);
            ChromeDriver driver = new ChromeDriver(chromeOptions);
          
            // And now use this to visit Google
            driver.get("https://www.topshop.com/en/tsuk/category/clothing-427/dresses-442");

            JavascriptExecutor js = (JavascriptExecutor) driver;
            var pageNumber = searchPageNumber(driver.getCurrentUrl());
            var newPageNumber = 1;
            do {
                pageNumber = newPageNumber;
                js.executeScript("window.scrollBy(0, -100)");
                Thread.sleep(150);
                js.executeScript("window.scrollBy(0,100000)");

                Thread.sleep(850);
                newPageNumber = searchPageNumber(driver.getCurrentUrl());
                log.info("loaded page {}", newPageNumber);
                
            } while (newPageNumber > pageNumber);
   
            List<WebElement> productLinks = driver.findElementsByXPath("//a[@class='Product-nameLink']");
            AtomicInteger count = new AtomicInteger();
            productLinks.stream().map(linkElement -> linkElement.getAttribute("href")).forEach(link -> {
                count.incrementAndGet();
                productService.productDetails(link);
            });
          
            log.info("Processing {} skus", count.get());

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
