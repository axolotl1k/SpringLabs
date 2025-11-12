package org.axolotlik.labs.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Mark {

    private Long id;
    private String studentName;
    private Integer grade; // Integer, щоб могти зберігати 'null'
    private boolean present = true;
    private LocalDateTime timestamp;

    // Конструктор для 'init()' в репозиторії
    public Mark(String studentName, Integer grade, boolean present) {
        this.studentName = studentName;
        this.grade = present ? grade : null;
        this.present = present;
        this.timestamp = LocalDateTime.now();
    }
}