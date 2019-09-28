package org.simonjamesrowe.productscraper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openqa.selenium.chrome.ChromeDriver;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PooledChromeDriver {
    
    private ChromeDriver driver;
    private boolean available = true;
}
