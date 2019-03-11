package com.neo.datasource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * Created by summer on 2016/11/25.
 */
@Configuration
@MapperScan(basePackages = "com.neo.mapper.gwsbim", sqlSessionTemplateRef  = "gwsbimSqlSessionTemplate")
public class DataSourceGwsbimConfig {

    static final String MAPPER_LOCATION = "classpath:mybatis/gwsbim/*.xml";

    @Bean(name = "gwsbimDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.gwsbim")
    
    public DataSource testDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "gwsbimSqlSessionFactory")
    
    public SqlSessionFactory testSqlSessionFactory(@Qualifier("gwsbimDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(MAPPER_LOCATION));
        return bean.getObject();
    }

    @Bean(name = "gwsbimTransactionManager")
    
    public DataSourceTransactionManager testTransactionManager(@Qualifier("gwsbimDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "gwsbimSqlSessionTemplate")
    
    public SqlSessionTemplate testSqlSessionTemplate(@Qualifier("gwsbimSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
