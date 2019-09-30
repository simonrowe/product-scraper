package org.simonjamesrowe.ingest;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@Log4j2
public class IngestApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngestApplication.class, args);
    }

    @Bean
    public CommandLineRunner crawler() {
        return (args) -> {
            String url = args[0];
            String bearerToken = args[1];
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            headers.add("Api-Version" , "1");
  
            
            int currentCount = 0;
           
            File dir = new File("out");
            for (File file : dir.listFiles()) {
                currentCount++;
                Map<String, Object> content = new ObjectMapper().readValue(Files.readString(file.toPath()), Map.class);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(content, headers);
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, entity, String.class);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    log.info("Send product {} to ingest", file.getName());
                } else {
                    log.error("Error sending {} to ingest, {}:{}", file.getName(), responseEntity.getStatusCode(), responseEntity.getBody());
                }
                
               
            }
            log.info("Sent {} record for ingest: " , currentCount);
        };
    }


    
}
