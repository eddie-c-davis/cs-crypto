package cs.crypto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Created by edavis on 11/25/16.
 */

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {
    public static void main(String[] args) {
        System.out.println("Running 'Peapod' web app...");

        // Remove key server instance from RedisCache.
        RedisCache.instance().remove(KeyServer.cacheKey());

        ApplicationContext context = SpringApplication.run(Application.class, args);
    }
}
