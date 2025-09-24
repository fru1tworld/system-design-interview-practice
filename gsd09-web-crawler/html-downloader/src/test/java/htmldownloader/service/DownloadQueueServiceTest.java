package htmldownloader.service;

import event.event.payload.UrlDiscoveryCreatePayload;
import htmldownloader.entity.DownloadQueue;
import htmldownloader.producer.UrlDiscoveryProducer;
import htmldownloader.repository.DownloadQueueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DownloadQueueServiceTest {

    @Mock
    private DownloadQueueRepository downloadQueueRepository;

    @Mock
    private HtmlDownloadService htmlDownloadService;

    @Mock
    private UrlDiscoveryProducer urlDiscoveryProducer;

    private DownloadQueueService downloadQueueService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        downloadQueueService = new DownloadQueueService(
                downloadQueueRepository,
                htmlDownloadService,
                urlDiscoveryProducer
        );
    }

    @Test
    void shouldSaveDownloadQueueSuccessfully() {
        // Given
        String eventId = UUID.randomUUID().toString();
        UrlDiscoveryCreatePayload payload = UrlDiscoveryCreatePayload.create(
                0,
                2,
                "https://example.com",
                "https://example.com"
        );

        String htmlContent = "<html><body><a href=\"https://example.com/page1\">Link 1</a></body></html>";
        DownloadQueue mockQueue = new DownloadQueue();

        when(downloadQueueRepository.save(any(DownloadQueue.class))).thenReturn(mockQueue);
        when(downloadQueueRepository.findById(anyString())).thenReturn(Optional.of(mockQueue));
        when(htmlDownloadService.downloadHtml(anyString())).thenReturn(htmlContent);

        // When
        downloadQueueService.consumeUrlDiscovery(eventId, payload);

        // Then
        verify(downloadQueueRepository, times(3)).save(any(DownloadQueue.class)); // Initial save + 2 status updates
        verify(htmlDownloadService).downloadHtml("https://example.com");
        verify(urlDiscoveryProducer).produceUrlDiscoveryEvents(
                eq(0),
                eq(2),
                eq("https://example.com"),
                any()
        );
    }

    @Test
    void shouldHandleFailureGracefully() {
        // Given
        String eventId = UUID.randomUUID().toString();
        UrlDiscoveryCreatePayload payload = UrlDiscoveryCreatePayload.create(
                0,
                2,
                "https://invalid-url.example",
                "https://invalid-url.example"
        );

        DownloadQueue mockQueue = new DownloadQueue();

        when(downloadQueueRepository.save(any(DownloadQueue.class))).thenReturn(mockQueue);
        when(downloadQueueRepository.findById(anyString())).thenReturn(Optional.of(mockQueue));
        when(htmlDownloadService.downloadHtml(anyString())).thenThrow(new RuntimeException("Download failed"));

        // When
        downloadQueueService.consumeUrlDiscovery(eventId, payload);

        // Then
        verify(downloadQueueRepository, times(3)).save(any(DownloadQueue.class)); // Initial save + 2 status updates
        verify(htmlDownloadService).downloadHtml("https://invalid-url.example");
        verifyNoInteractions(urlDiscoveryProducer);
    }
}