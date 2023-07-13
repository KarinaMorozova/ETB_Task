package my.energotrans.model;

import lombok.*;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Data
@AllArgsConstructor
@Component
public class Entry {
    private Long entryId;

    private Long debitAccountId;

    private Long creditAccountId;

    private Double sum;

    private Instant creationDate;

    public Entry(){}
}
