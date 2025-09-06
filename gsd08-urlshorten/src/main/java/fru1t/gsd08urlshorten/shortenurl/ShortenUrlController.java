package fru1t.gsd08urlshorten.shortenurl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
public class ShortenUrlController {
    private final ShortenUrlService shortenUrlService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/shorten")
    public String createShortenUrl(@RequestParam("originalUrl") String url, Model model) {
        String shortenedUrl = shortenUrlService.createShortenUrl(url);
        model.addAttribute("shortenedUrl", "http://localhost:9999/" + shortenedUrl);
        return "index";
    }

    @GetMapping("/{shortenUrl}")
    public RedirectView redirect(@PathVariable("shortenUrl") String shortenUrl) {
        RedirectView redirectView = new RedirectView();
        String redirectUrl = shortenUrlService.readShortenUrl(shortenUrl);
        redirectView.setUrl(redirectUrl);
        return redirectView;
    }

    @PostMapping("api/v1/shorten-url")
    @ResponseBody
    public String createShortenUrlApi(@RequestBody() String url) {
        return shortenUrlService.createShortenUrl(url);
    }

    @GetMapping("api/v1/shorten-url")
    public RedirectView readShortenUrlApi(@RequestParam("shortenUrl") String shortenUrl) {
        RedirectView redirectView = new RedirectView();
        String redirectUrl = shortenUrlService.readShortenUrl(shortenUrl);
        redirectView.setUrl(redirectUrl);
        return redirectView;
    }
}
