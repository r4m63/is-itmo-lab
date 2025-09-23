package ru.itmo.isitmolab.config;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;

@DataSourceDefinition(
        name = "java:/jdbc/StudsDS",
        className = "org.postgresql.Driver",
        url = "${env.DB_URL}",
        user = "${env.DB_USER}",
        password = "${env.DB_PASS}",
        properties = {
                "ssl=${env.DB_SSL:false}"
        }
)
@ApplicationScoped
public class DatasourceConfig {

}
