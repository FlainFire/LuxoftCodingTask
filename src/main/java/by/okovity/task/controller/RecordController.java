package by.okovity.task.controller;

import by.okovity.task.entity.Record;
import by.okovity.task.repo.RecordRepo;
import by.okovity.task.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
@Slf4j
public class RecordController {

    private final RecordRepo recordRepo;
    private final RecordService recordService;

    @GetMapping("/{id}")
    public ResponseEntity<Record> getRecord(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        return recordRepo.findById(id).map(record -> ResponseEntity.ok().body(record))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam(name = "file", required = false) MultipartFile file) {
        if (Objects.isNull(file) || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File does not exist or empty");
        }

        log.info("Start file processing. File name: '{}', file size: {} byte, file content type: '{}'",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        int recordsProcessed = 0;
        try (InputStream is = file.getInputStream()) {
            List<Record> records = recordService.parseFile(is);
            recordRepo.saveAll(records);
            recordsProcessed = records.size();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Get exception during file processing with message: " + e.getMessage());
        }

        log.info("Finish file processing. File name: '{}', records saved: {}",
                file.getOriginalFilename(), recordsProcessed);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecord(@PathVariable Long id) {
        if (!recordRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        recordRepo.deleteById(id);

        if (recordRepo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Record not deleted");
        } else {
            return ResponseEntity.ok().build();
        }
    }

}
