package io.github.rNetAi.rnetCore.init;

import io.github.rNetAi.rnetCore.config.RNetCoreProperty;
import io.github.rNetAi.rnetCore.rNetProtocol.RNetProtocol;
import io.github.rNetAi.rnetCore.scanner.RNetSpringScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public class RNetCoreInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RNetCoreInitializer.class);
    private final RNetSpringScanner rNetSpringScanner;

    private final RNetCoreProperty property;
    private final RestTemplate restTemplate;

    public RNetCoreInitializer(RNetSpringScanner rNetSpringScanner, RNetCoreProperty property) {
        this.rNetSpringScanner = rNetSpringScanner;
        this.property = property;
        this.restTemplate = createRestTemplate();
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {

        String key = property.getDeveloper().getKey();

        log.info("Check rNet developer key: {}", key);

        RNetProtocol.DEVELOPER_KEY = key;

        if (!developerKeyIsValid(key)) {
            log.error("rNet Developer Key is INVALID");
            throw new RuntimeException("rNet Developer Key is INVALID");
//            return; // ❗ do I need to kill app ?
        }

        log.info("Developer Key validated For rNet Protocol");
        rNetSpringScanner.scan();
    }

    private boolean developerKeyIsValid(String key) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(RNetProtocol.RNET_DEVELOPER_KEY_CHECK)
                    .queryParam("key", key)
                    .build()
                    .toUri();

            ResponseEntity<Void> response = restTemplate.getForEntity(uri , Void.class);

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Error validating developer key", e);
            return false;
        }
    }

    private RestTemplate createRestTemplate() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        return new RestTemplate(factory);
    }
}