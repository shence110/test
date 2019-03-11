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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * Created by summer on 2016/11/25.
 */
@Configuration
@MapperScan(basePackages = "com.neo.mapper.eafbim", sqlSessionTemplateRef  = "eafbimSqlSessionTemplate")
public class DataSourceEafbimConfig {

    static final String MAPPER_LOCATION = "classpath:mybatis/eafbim/*.xml";


    @Bean(name = "eafbimDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.eafbim")
    public DataSource testDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "eafbimSqlSessionFactory")
    public SqlSessionFactory testSqlSessionFactory(@Qualifier("eafbimDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(MAPPER_LOCATION));
        return bean.getObject();
    }

    @Bean(name = "eafbimTransactionManager")
    public DataSourceTransactionManager testTransactionManager(@Qualifier("eafbimDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "eafbimSqlSessionTemplate")
    public SqlSessionTemplate testSqlSessionTemplate(@Qualifier("eafbimSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
