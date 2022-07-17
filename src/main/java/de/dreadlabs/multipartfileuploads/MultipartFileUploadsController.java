package de.dreadlabs.multipartfileuploads;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/multipartfileuploads")
public class MultipartFileUploadsController {

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> upload(@RequestParam(required = false) List<MultipartFile> files) {
        if (files == null) {
            return ResponseEntity.ok("No files uploaded.");
        }

        if (files.size() == 1) {
            return ResponseEntity.ok(String.valueOf(files.get(0).getSize()));
        }

        return ResponseEntity.ok(
                files.stream()
                        .collect(Collectors.toMap(MultipartFile::getOriginalFilename, MultipartFile::getSize))
                        .entrySet()
                        .stream()
                        .map(it -> "%s: %s".formatted(it.getKey(), it.getValue().toString()))
                        .collect(Collectors.joining(", "))
        );
    }
}
