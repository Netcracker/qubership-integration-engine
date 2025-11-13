package org.qubership.integration.platform.engine.utils;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

public class PostgresTwoDbTestResource implements QuarkusTestResourceLifecycleManager {

    private PostgreSQLContainer<?> pg;

    @Override
    public Map<String, String> start() {
        pg = new PostgreSQLContainer<>("postgres:17.2")
                .withDatabaseName("postgres")
                .withUsername("postgres")
                .withPassword("postgres")
                // создаёт две БД при инициализации
                .withInitScript("postgresql/db_init.sql");
        pg.start();

        String host = pg.getHost();
        Integer port = pg.getMappedPort(5432);
        String base = host + ":" + port;

        Map<String, String> m = new HashMap<>();

        m.put("quarkus.datasource.checkpoints.db-kind", "postgresql");
        m.put("quarkus.datasource.checkpoints.username", "postgres");
        m.put("quarkus.datasource.checkpoints.password", "postgres");
        m.put("quarkus.datasource.checkpoints.jdbc.url",
                "jdbc:postgresql://" + base + "/engine_checkpoints_db");

        m.put("quarkus.datasource.checkpoints.jdbc.min-size", "0");
        m.put("quarkus.datasource.checkpoints.jdbc.max-size", "10");

        m.put("quarkus.flyway.checkpoints.migrate-at-start", "true");
        m.put("quarkus.flyway.checkpoints.clean-at-start", "true");

        m.put("quarkus.datasource.quartz.db-kind", "postgresql");
        m.put("quarkus.datasource.quartz.username", "postgres");
        m.put("quarkus.datasource.quartz.password", "postgres");
        m.put("quarkus.datasource.quartz.jdbc.url",
                "jdbc:postgresql://" + base + "/engine_qrtz_db");

        m.put("quarkus.datasource.quartz.jdbc.min-size", "0");
        m.put("quarkus.datasource.quartz.jdbc.max-size", "10");

        m.put("quarkus.quartz.store-type", "jdbc-cmt");
        m.put("quarkus.quartz.datasource", "quartz");
        m.put("quarkus.quartz.clustered", "false");

        m.put("quarkus.flyway.quartz.migrate-at-start", "true");
        m.put("quarkus.flyway.quartz.clean-at-start", "true");

        m.put("quarkus.hibernate-orm.log.sql", "false");

        return m;
    }

    @Override
    public void stop() {
        if (pg != null) pg.stop();
    }
}
