package com.clinomics.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.clinomics.repository.seq"
		, entityManagerFactoryRef = "seqEntityManagerFactory"
		, transactionManagerRef = "seqTransactionManager")
public class SeqMysqlConfig {

	public static final String UNITNAME = "seq_report";

	@Bean(name = "seqDataSource")
	@Qualifier("seqDataSource")
	@Primary
	@ConfigurationProperties(prefix = "datasource.seq")
	public DataSource seqDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "seqEntityManagerFactory")
	@Qualifier("seqEntityManagerFactory")
	@Primary
	public LocalContainerEntityManagerFactoryBean seqEntityManagerFactory(EntityManagerFactoryBuilder builder) {
		Map<String, Object> properties = new HashMap<String, Object>();
	    properties.put("hibernate.hbm2ddl.auto", "update");
	    properties.put("hibernate.hbm2ddl.import_files", "data.sql");
	    properties.put("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
	    DataSource ds = seqDataSource();
		return builder.dataSource(ds)
				.packages("com.clinomics.entity.seq")
				.persistenceUnit(UNITNAME)
				.properties(properties)
				.build();
	}

	@Bean(name = "seqDataSourceInitializer")
	public DataSourceInitializer seqDataSourceInitializer() {
		ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
		resourceDatabasePopulator.addScript(new ClassPathResource("/data.sql"));

		DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
		dataSourceInitializer.setDataSource(seqDataSource());
		dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
		return dataSourceInitializer;
	}

	@Bean(name = "seqTransactionManager")
	@Primary
	public PlatformTransactionManager seqTransactionManager() {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
		jpaTransactionManager.setDataSource(seqDataSource());
		jpaTransactionManager.setPersistenceUnitName(UNITNAME);
		return jpaTransactionManager;
	}
}
