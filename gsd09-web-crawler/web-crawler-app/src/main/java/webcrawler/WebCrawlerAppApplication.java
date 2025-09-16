package webcrawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"webcrawler", "url.discovery"})
public class WebCrawlerAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebCrawlerAppApplication.class, args);
    }
}