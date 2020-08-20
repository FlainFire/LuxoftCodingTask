package by.okovity.task.service;

import by.okovity.task.data.RecordCsvLine;
import by.okovity.task.entity.Record;
import by.okovity.task.exception.RecordsProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecordService {

    @Value("${project.accept.corrupted.records}")
    private boolean acceptCorrupted;

    public List<Record> parseFile(InputStream is) throws IOException {
        List<Record> records = new ArrayList<>();

        try (CSVParser parser = CSVParser.parse(is, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader())) {
            checkFileHeader(parser.getHeaderMap());

            for (CSVRecord record : parser.getRecords()) {
                records.add(parseRecord(record));
            }
        }

        return records.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void checkFileHeader(Map<String, Integer> headerMap) throws RecordsProcessingException {
        if (!Arrays.stream(RecordCsvLine.values()).allMatch(headerName ->
                headerMap.containsKey(headerName.name()) && (headerName.getIndex() == headerMap.get(headerName.name()))
        )) {
            throw new RecordsProcessingException("CSV file doesn't contain valid headers");
        }
    }

    private Record parseRecord(CSVRecord record) {
        Record rec = null;
        try {
            rec = new Record();

            String id = record.get(RecordCsvLine.PRIMARY_KEY.name());
            if (StringUtils.isBlank(id)) {
                log.warn("Found empty id on line: " + record.getRecordNumber());
            } else {
                rec.setId(Long.parseLong(id));
                rec.setName(record.get(RecordCsvLine.NAME.name()));
                rec.setDescription(record.get(RecordCsvLine.DESCRIPTION.name()));

                String timestamp = record.get(RecordCsvLine.UPDATED_TIMESTAMP.name());
                if (StringUtils.isNotBlank(timestamp)) {
                    rec.setTimestamp(Timestamp.valueOf(timestamp));
                }
            }
        } catch (Exception e) {
            log.warn("Catch error during record parsing on line: " + record.getRecordNumber(), e);
        }

        if (isRecordCorrupted(rec)) {
            log.warn("Record on line {} is corrupted", record.getRecordNumber());
            return null;
        } else {
            return rec;
        }
    }

    private boolean isRecordCorrupted(Record record) {
        return !acceptCorrupted &&
                (record == null ||
                        Objects.isNull(record.getId()) ||
                        StringUtils.isBlank(record.getName()) ||
                        StringUtils.isBlank(record.getDescription()) ||
                        Objects.isNull(record.getTimestamp())
                );
    }
}
