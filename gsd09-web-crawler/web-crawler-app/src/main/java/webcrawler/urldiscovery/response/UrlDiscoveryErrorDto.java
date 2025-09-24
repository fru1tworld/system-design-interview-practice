package webcrawler.urldiscovery.response;

import lombok.Getter;

@Getter
public class UrlDiscoveryErrorDto {
    private String url;
    private String errorMessage;

    public static UrlDiscoveryErrorDto create(String url, String errorMessage) {
        UrlDiscoveryErrorDto error = new UrlDiscoveryErrorDto();
        error.url = url;
        error.errorMessage = errorMessage;
        return error;
    }
}