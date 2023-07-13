package my.energotrans.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@JacksonXmlRootElement(localName = "client")
@Component
public class Client {
    @JacksonXmlProperty(localName = "clientId", isAttribute = true)
    private Long clientId;

    @JacksonXmlProperty(localName = "clientName")
    private String clientName;

    @JacksonXmlElementWrapper(localName = "accounts")
    @JacksonXmlProperty(localName = "account")
    private List<Account> accounts = new ArrayList<>();

    public Client() {}

    public Client(String clientName) {
        this.clientName = clientName;
    }

    public Client(Long clientId, String clientName) {
        this.clientId = clientId;
        this.clientName = clientName;
    }

    public void addAccount(Account account) {
        if (this.clientId.equals(account.getClientId())) {
            this.accounts.add(account);
        }
        else {
            System.err.format("Internal Error State: %s\n%s", "Невозможно добавить данный счет к клиенту");
        }
    }
}
