package de.dreadlabs.multipartfileuploads;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class UploadSizeExceptionAdvice {

    private final DistributionSummary partSizeSummary;
    private final DistributionSummary requestSizeSummary;

    public UploadSizeExceptionAdvice(MeterRegistry meterRegistry) {
        partSizeSummary = DistributionSummary.builder("multipartfileupload_limit_exceeding_part_size")
                .baseUnit("bytes")
                .publishPercentiles(0.5, 0.75, 0.95, 0.999)
                .register(meterRegistry);

        requestSizeSummary = DistributionSummary.builder("multipartfileupload_limit_exceeding_request_size")
                .baseUnit("bytes")
                .publishPercentiles(0.5, 0.75, 0.95, 0.999)
                .register(meterRegistry);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<String> handleMaxUploadSizeExceededException(HttpServletRequest request, Throwable ex) {
        recordMetrics(ex);

        // Check if original exception is `o.a.t.u.h.f.i.MaxUploadSizeExceededException`, which is thrown if
        // total request size limit is exceeded.
        // - Tomcat wraps root cause in `j.l.IllegalStateException`
        // - Spring also checks exception message contents (see `o.s.w.m.s.StandardMultipartHttpServletRequest`
        String message = ex.getCause().getMessage();
        if (message != null && message.contains("request")) {
            return ResponseEntity.badRequest().body("The total size of all uploaded files is too large.");
        }

        return ResponseEntity.badRequest().body("An uploaded file is too large.");
    }

    private void recordMetrics(Throwable ex) {
        if (ex.getCause() instanceof IllegalStateException) {
            Throwable wrappedException = ex.getCause();

            if (wrappedException != null) {
                Throwable rootException = wrappedException.getCause();

                if (rootException instanceof FileSizeLimitExceededException partSizeException) {
                    partSizeSummary.record(partSizeException.getActualSize());
                } else if (rootException instanceof SizeLimitExceededException requestSizeException) {
                    requestSizeSummary.record(requestSizeException.getActualSize());
                }
            }
        }
    }
}
