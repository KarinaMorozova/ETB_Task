package my.energotrans.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JacksonXmlRootElement(localName = "clientList")
public class ClientList extends ArrayList<Client> {
    List<Client> clients;
}
