package htmldownloader.dto;

import lombok.Getter;

@Getter
public class UrlDiscoveryDto {
    private String crawlingId;
    private String url;
    private String bfsPath;
    private Integer depth;
    private Integer maxDepth;

}
