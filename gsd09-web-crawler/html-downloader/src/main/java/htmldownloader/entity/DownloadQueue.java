package htmldownloader.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "download_queue")
@Getter
@Setter
public class DownloadQueue {
    @Id
    private String id;

    private String crawlingId;
    private String originalUrl;
    private String rootDomain;
    private Integer depth;
    private String bfsPath;
    private String status;
    private Long score;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DownloadQueue create(String id,
                                String crawlingId,
                                String originalUrl,
                                String bfsPath,
                                Integer depth,
                                String status){
        DownloadQueue response = new DownloadQueue();
        response.id = id;
        response.crawlingId = crawlingId;
        response.originalUrl = originalUrl;
        response.depth = depth;
        response.status = status;
        response.bfsPath = bfsPath;
        response.createdAt = LocalDateTime.now();
        response.updatedAt = LocalDateTime.now();

        return response;
    }
}