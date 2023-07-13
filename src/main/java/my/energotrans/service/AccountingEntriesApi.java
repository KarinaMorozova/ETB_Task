package my.energotrans.service;

import my.energotrans.model.Account;
import my.energotrans.model.Client;
import my.energotrans.model.Entry;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AccountingEntriesApi
 * <p>
 * Интерфейс управления клиентами и их счетами
 * @author KarinaMorozova
 * 10.07.2023
 */

@Component
public interface AccountingEntriesApi {
    /**
     * Создание клиента
     *
     * @return boolean - маркер создания
     */
    boolean saveClient(Client client);

    /**
     * Создание счета
     *
     * @return boolean - маркер создания
     */
    boolean saveAccount(Account account);

    /**
     * Осуществляет прямой запрос в БД и возвращает список клиентов со счетами,
     * зарегистрированных в базе данных.
     *
     * @return Список Client {@link Client}
     */
    List<Client> findAllClients();

    /**
     * Создание проводки
     *
     * @return boolean - маркер создания проводки
     */
    boolean saveEntry(Entry entry);

    /**
     * Экспорт списка клиентов в XML-файл
     */
    void exportClientsToXML();

    /**
     * Импорт списка клиентов из XML-файла в БД
     */
    void importClientsFromXML();

    /**
     * Осуществляет прямой запрос в БД и возвращает список проводок,
     * зарегистрированных в базе данных
     *
     * @return Список Entry {@link Entry}
     */
    List<Entry> findAllEntries();
}
