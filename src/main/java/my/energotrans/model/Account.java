package my.energotrans.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.*;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@JacksonXmlRootElement(localName = "account")
@Component
public class Account {
    @JacksonXmlProperty(localName = "accountId")
    private Long accountId;

    @JacksonXmlProperty(localName = "clientId")
    private Long clientId;

    @JacksonXmlProperty(localName = "balance")
    private Double balance;
    public Account(){}
}
