package org.simonjamesrowe.productscraper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CrawlerApplicationTest {
  

    @Test
    public void searchPageNumber() {
        assertThat(CrawlerApplication.searchPageNumber("https://www.topshop.com/en/tsuk/category/clothing-427/dresses-442?currentPage=3")).isEqualTo(3);
        assertThat(CrawlerApplication.searchPageNumber("https://www.topshop.com/en/tsuk/category/clothing-427/dresses-442?currentPage=2")).isEqualTo(2);

    }
}
