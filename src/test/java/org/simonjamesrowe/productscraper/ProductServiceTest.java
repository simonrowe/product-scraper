package org.simonjamesrowe.productscraper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ProductServiceTest {
    
    private ProductService productService = new ProductService();

    @Test
    public void extractMaterialComposition() {
        String description = "Red ruched midi cami dress. 95% Polyester, 5% Elatsine. Hand wash only.";
        assertThat(productService.extractMaterialComposition(description)).isEqualTo("95% Polyester, 5% Elatsine");
    }
}
