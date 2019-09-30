package org.simonjamesrowe.productscraper.ingest;

import java.util.Map;

import lombok.Data;

@Data
public class RawData {
    
    private String tenantId;
    
    private String externalProductId;
    
    private InputData mapped;
    
    private Map<String, Object> source;
}
