package htmldownloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"htmldownloader", "event"})
public class HtmlDownloaderApplication {
    public static void main(String[] args) {
        SpringApplication.run(HtmlDownloaderApplication.class, args);
    }
}