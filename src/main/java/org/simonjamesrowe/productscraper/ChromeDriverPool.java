package org.simonjamesrowe.productscraper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;

import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ChromeDriverPool {

    private List<PooledChromeDriver> pool = new ArrayList<>();
    private boolean initialized = false;

    public void init() {
        for (int i = 0; i < 10; i++) {
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.setHeadless(true);
            ChromeDriver driver = new ChromeDriver(chromeOptions);
            pool.add(new PooledChromeDriver(driver, true));
        }
        initialized = true;
    }

    public synchronized ChromeDriver borrow() {
        if (!initialized) {
            init();
        }
        while (pool.stream().noneMatch(PooledChromeDriver::isAvailable)) {
            try {
                Thread.sleep(1000);
                log.info("No drivers available - waiting for one");
            } catch (InterruptedException e) {
            }
        }
        PooledChromeDriver pooledChromeDriver = pool.stream().filter(PooledChromeDriver::isAvailable).findFirst().get();
        pooledChromeDriver.setAvailable(false);
        return pooledChromeDriver.getDriver();
    }

    public synchronized void returnDriver(ChromeDriver driver) {
        pool.stream().filter(pd -> pd.getDriver() == driver).findFirst().get().setAvailable(true);
    }

    @PreDestroy
    public void onDestroy() {
        pool.forEach(pc -> pc.getDriver().close());
    }
}
