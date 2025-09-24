package webcrawler.urldiscovery.request;

import webcrawler.urldiscovery.response.UrlDiscoveryErrorDto;
import webcrawler.urldiscovery.response.UrlDiscoveryResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class UrlDiscoveryBatchResponseDto {
    private List<UrlDiscoveryResponseDto> successResults;
    private List<UrlDiscoveryErrorDto> errors;

    public static UrlDiscoveryBatchResponseDto create(List<UrlDiscoveryResponseDto> successResults, List<UrlDiscoveryErrorDto> errors) {
        UrlDiscoveryBatchResponseDto response = new UrlDiscoveryBatchResponseDto();
        response.successResults = successResults;
        response.errors = errors;
        return response;
    }
}