package my.energotrans.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import my.energotrans.model.Account;
import my.energotrans.model.Client;
import my.energotrans.model.ClientList;
import my.energotrans.model.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * AccountingEntriesApi
 * <p>
 * Реализация интерфейса управления клиентами и их счетами {@link AccountingEntriesApi} *
 * @author Karina Morozova
 * 11.07.2023
 */

@Component
public class AccountingEntriesApiImpl implements AccountingEntriesApi {
    private static final int BATCH_SIZE = 1000;
    private static final String INSERT_CLIENT = "insert into client (client_id, client_name) values (?, ?);";
    private static final String INSERT_ACCOUNT = "insert into account (account_id, client_id, balance) values (?, ?, ?);";
    private static final String ALL_CLIENTS = "select a.client_id, a.client_name, b.account_id, b.balance " +
                                              "from client a " +
                                              "left join account b on a.client_id = b.client_id " +
                                              "order by a.client_id ";

    private static final String SELECT_ACCOUNT = "select acc.* from account acc where acc.account_id = %d;";

    private static final String UPDATE_ACCOUNT = "update account set balance = ? " +
            "where account_id = ?;";
    private static final String ALL_ENTRIES = "select e.* from entry e";
    private static final String CREATE_ENTRY = "insert into entry (entry_id, debit_account_id, credit_account_id, sum, creation_date) values (?, ?, ?, ?, ?);";
    private DataSource dataSource;

    @Autowired
    public AccountingEntriesApiImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean saveClient(Client client) {
        try (Connection connection = this.dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_CLIENT) ) {
            preparedStatement.setLong(1, client.getClientId());
            preparedStatement.setString(2, client.getClientName());

            int row = preparedStatement.executeUpdate();
            return true;
        }
        catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean saveAccount(Account account) {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_ACCOUNT) ) {
            preparedStatement.setLong(1, account.getAccountId());
            preparedStatement.setLong(2, account.getClientId());
            preparedStatement.setDouble(3, account.getBalance());

            int row = preparedStatement.executeUpdate();
            return true;
        }
        catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            return false;
        }
    }

    @Override
    public List<Client> findAllClients() {
        List<Client> clients = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();

        try (Connection connection = this.dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(ALL_CLIENTS)) {

            Long oldClientId = -1L;
            Client client = null;
            while (rs.next()) {
                Long clientId = rs.getLong("client_id");

                if (client != null && !(oldClientId.equals(clientId) || oldClientId == -1L)) {
                    client.setAccounts(accounts);
                    clients.add(client);
                    accounts = new ArrayList<>();
                }

                String clientName = rs.getString("client_name");

                client = new Client(clientId, clientName);

                Long accountId = rs.getLong("account_id");
                Double balance = rs.getDouble("balance");
                Account account = new Account(accountId, clientId, balance);

                accounts.add(account);
                oldClientId = clientId;
            }
            if (client != null) {
                client.setAccounts(accounts);
                clients.add(client);
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        }

        return clients;
    }

    @Override
    public boolean saveEntry(Entry entry) {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(CREATE_ENTRY);

             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(String.format(SELECT_ACCOUNT, entry.getCreditAccountId()));

             Statement stmt = connection.createStatement();
             ResultSet rs2 = stmt.executeQuery(String.format(SELECT_ACCOUNT, entry.getDebitAccountId())) ) {

            preparedStatement.setLong(1, entry.getEntryId());
            preparedStatement.setLong(2, entry.getDebitAccountId());
            preparedStatement.setLong(3, entry.getCreditAccountId());
            preparedStatement.setDouble(4, entry.getSum());
            preparedStatement.setTimestamp(5, Timestamp.from(entry.getCreationDate()));
            int row = preparedStatement.executeUpdate();

            Account credit = new Account();
            if (rs.next()) {
                credit.setAccountId(rs.getLong("account_id"));
                credit.setClientId(rs.getLong("client_id"));
                credit.setBalance(rs.getDouble("balance") - entry.getSum());
            }
            updateAccount(credit);

            Account debit = new Account();
            if (rs2.next()) {
                debit.setAccountId(rs2.getLong("account_id"));
                debit.setClientId(rs2.getLong("client_id"));
                debit.setBalance(rs2.getDouble("balance") + entry.getSum());
            }
            updateAccount(debit);

            return true;
        }
        catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            return false;
        }
    }

    private boolean updateAccount(Account account) {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_ACCOUNT) ) {
            preparedStatement.setDouble(1, account.getBalance());
            preparedStatement.setLong(2, account.getAccountId());

            int row = preparedStatement.executeUpdate();
            return true;
        }
        catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            return false;
        }
    }

    @Override
    public void exportClientsToXML() {
        XmlMapper xmlMapper;
        List<Client> clients = findAllClients();

        try {
            xmlMapper = new XmlMapper();
            xmlMapper.writeValue(new File(System.getProperty("user.dir") + "\\clients.xml"), clients);
        }
        catch (IOException ex) {
            ex.getStackTrace();
        }
    }

    @Override
    public void importClientsFromXML() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            ClientList list = xmlMapper.readValue(new File (System.getProperty("user.dir") + "\\cl.xml"), ClientList.class);
            batchInsert(list);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<Entry> findAllEntries() {
        List<Entry> entries = new ArrayList<>();

        try (Connection connection = this.dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(ALL_ENTRIES) ) {

            while (rs.next()) {
                Long entryId = rs.getLong("entry_id");
                Long debitAccountId = rs.getLong("debit_account_id");
                Long creditAccountId = rs.getLong("credit_account_id");
                Double sum = rs.getDouble("sum");
                Instant creationDate = rs.getTimestamp("creation_date").toInstant();

                entries.add(new Entry(entryId, debitAccountId, creditAccountId, sum, creationDate));
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        }

        return entries;
    }

    private void batchInsert(List<Client> clients) {
        try {
            Connection connection = this.dataSource.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_CLIENT);
                 PreparedStatement preparedStmt = connection.prepareStatement(INSERT_ACCOUNT) ) {

                for (int i = 0; i < clients.size(); i++) {
                    Client current = clients.get(i);
                    preparedStatement.setLong(1, current.getClientId());
                    preparedStatement.setString(2, current.getClientName());
                    preparedStatement.addBatch();

                    for (int j = 0; j < current.getAccounts().size(); j++) {
                        Account currentAccount = current.getAccounts().get(j);
                        preparedStmt.setLong(1, currentAccount.getAccountId());
                        preparedStmt.setLong(2, currentAccount.getClientId());
                        preparedStmt.setDouble(3, currentAccount.getBalance());
                        preparedStmt.addBatch();
                    }

                    if (i % BATCH_SIZE == 0 || i == clients.size() - 1) {
                        try {
                            int[] result = preparedStatement.executeBatch();
                            int[] accs = preparedStmt.executeBatch();

                            connection.commit();
                        } catch (BatchUpdateException bex) {
                            System.err.format("SQL State: %s\n%s", bex.getSQLState(), bex.getMessage());
                            connection.rollback();
                        }
                    }
                }
            }
            System.out.println("Импорт данных из файла успешно завершен");
        }
        catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        }
    }
}
