package by.okovity.task.data;

import lombok.Getter;

public enum RecordCsvLine {

    PRIMARY_KEY(0),
    NAME(1),
    DESCRIPTION(2),
    UPDATED_TIMESTAMP(3);

    @Getter
    private final int index;

    RecordCsvLine(int index) {
        this.index = index;
    }
}
