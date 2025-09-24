package htmldownloader.repository;

import htmldownloader.entity.DownloadQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DownloadQueueRepository extends JpaRepository<DownloadQueue, String> {

    @Query("SELECT dq FROM DownloadQueue dq WHERE dq.status = :status ORDER BY dq.score DESC, dq.createdAt ASC")
    List<DownloadQueue> findByStatusOrderByScoreDescCreatedAtAsc(@Param("status") String status);

    @Query("SELECT dq FROM DownloadQueue dq WHERE dq.crawlingId = :crawlingId AND dq.status = :status")
    List<DownloadQueue> findByCrawlingIdAndStatus(@Param("crawlingId") String crawlingId,
                                                  @Param("status") String status);

    boolean existsByOriginalUrlAndCrawlingId(String originalUrl, String crawlingId);

    /**
     * bfsPath 중복 체크
     */
    boolean existsByBfsPath(String bfsPath);

    /**
     * 특정 크롤링 세션에서 bfsPath 중복 체크
     */
    boolean existsByCrawlingIdAndBfsPath(String crawlingId, String bfsPath);

    /**
     * 1달 이내에 특정 URL이 처리되었는지 확인
     */
    @Query("SELECT COUNT(dq) > 0 FROM DownloadQueue dq WHERE dq.originalUrl = :originalUrl AND dq.createdAt > :oneMonthAgo")
    boolean existsByOriginalUrlWithinOneMonth(@Param("originalUrl") String originalUrl,
                                            @Param("oneMonthAgo") LocalDateTime oneMonthAgo);

    /**
     * 특정 크롤링 세션의 rootDomain 조회
     */
    @Query("SELECT DISTINCT dq.rootDomain FROM DownloadQueue dq WHERE dq.crawlingId = :crawlingId AND dq.depth = 0")
    String findRootDomainByCrawlingId(@Param("crawlingId") String crawlingId);
}