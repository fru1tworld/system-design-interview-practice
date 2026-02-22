package fru1t.gsd08urlshorten.shortenurl;

import com.sun.net.httpserver.Headers;
import fru1t.gsd08urlshorten.shortenurl.model.ShortenUrlCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
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
    @ResponseBody
    public String createShortenUrl(@Valid @RequestBody ShortenUrlCreateRequest request) {
        String shortenedUrl = shortenUrlService.createShortenUrl(request.getOriginalUrl());
        return "http://localhost:9999/" + shortenedUrl;
    }

    @GetMapping("/{shortenUrl}")
    public RedirectView redirect(@PathVariable("shortenUrl") String shortenUrl) {
        RedirectView redirectView = new RedirectView();
        String redirectUrl = shortenUrlService.readShortenUrl(shortenUrl);
        redirectView.setUrl(redirectUrl);
        redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);

        return redirectView;
    }
}
