package my.energotrans;

import my.energotrans.config.Config;
import my.energotrans.model.Account;
import my.energotrans.model.Client;
import my.energotrans.model.Entry;
import my.energotrans.service.AccountingEntriesApi;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Instant;
import java.util.List;

public class App{
    public static void main( String[] args ){
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(Config.class);
        applicationContext.start();

        AccountingEntriesApi accountingEntriesApi = applicationContext.getBean(AccountingEntriesApi.class);

        // Сохранить клиента
        Client aryaStark = new Client(1L,"Arya Stark");
        accountingEntriesApi.saveClient(aryaStark);

        // Сохранить счет
        Account account = new Account(1L, 1L, 1000.0);
        aryaStark.addAccount(account);
        accountingEntriesApi.saveAccount(account);

        // Сохранить второй счет
        account = new Account(2L, 1L, 4000.0);
        aryaStark.addAccount(account);
        accountingEntriesApi.saveAccount(account);

        // Сохранение второго клиента
        Client sansaStark = new Client(2L,"Sansa Stark");
        accountingEntriesApi.saveClient(sansaStark);

        // Сохранить счет второго клиента
        account = new Account(3L, 2L, 0.0);
        sansaStark.addAccount(account);
        accountingEntriesApi.saveAccount(account);

        // Cписок всех клиентов со счетами
        System.out.println("Список всех клиентов");
        List<Client> clients = accountingEntriesApi.findAllClients();
        for (Client clnt : clients) {
            System.out.println(clnt.getClientId() + " " + clnt.getClientName() + " " + clnt.getAccounts());
        }

        // 1 проводка со счета 2 на счет 3 2000.0
        Entry entry = new Entry(1L, sansaStark.getAccounts().get(0).getAccountId(),
                aryaStark.getAccounts().get(1).getAccountId(), 2000.0, Instant.now());
        accountingEntriesApi.saveEntry(entry);

        // Cписок всех клиентов со счетами
        System.out.println("Список всех клиентов");
        clients = accountingEntriesApi.findAllClients();
        for (Client clnt : clients) {
            System.out.println(clnt.getClientId() + " " + clnt.getClientName() + " " + clnt.getAccounts());
        }

        // Список всех проводок
        System.out.println("Список всех проводок");
        List<Entry> entries = accountingEntriesApi.findAllEntries();
        for (Entry e: entries) {
            System.out.println(e);
        }

        // Экспорт списка клиентов в XML-файл
        accountingEntriesApi.exportClientsToXML();

        // Импорт списка клиентов в БД
        accountingEntriesApi.importClientsFromXML();
    }
}
