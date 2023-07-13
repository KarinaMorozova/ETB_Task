package my.energotrans.config;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ComponentScan("my.energotrans")
public class Config {
    @Bean
    public DataSource dataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerName("localhost");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");
        dataSource.setDatabaseName("postgres");
        dataSource.setPortNumber(5431);  // для Postgres порт по умолчанию 5432

        String ddl = ""
                + "drop table if exists client cascade;"
                + "create table if not exists client (\n"
                + "client_id integer primary key,\n"
                + "client_name varchar(255)\n"
                + ")";

        DbUtil.applyDdl(ddl, dataSource);

        ddl = ""
                + "drop table if exists account cascade;"
                + "create table if not exists account (\n"
                + "account_id integer primary key,\n"
                + "client_id integer references client(client_id),\n"
                + "balance decimal\n"
                + ")";

        DbUtil.applyDdl(ddl, dataSource);

        ddl = ""
                + "drop table if exists entry cascade;"
                + "create table if not exists entry (\n"
                + "entry_id integer primary key,\n"
                + "debit_account_id integer,\n"
                + "credit_account_id integer,\n"
                + "sum decimal,\n"
                + "creation_date timestamp\n"
                + ")";

        DbUtil.applyDdl(ddl, dataSource);

        return dataSource;
    }


}
