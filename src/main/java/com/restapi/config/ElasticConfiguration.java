package com.restapi.config;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.http.HttpHeaders;

/*
@Configuration
public class ElasticConfiguration extends AbstractElasticsearchConfiguration {
    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    }
}*/

@Configuration(proxyBeanMethods = false)
public class ElasticConfiguration extends AbstractElasticsearchConfiguration {

    // if wish to use the new client, refer to
    // https://docs.spring.io/spring-data/elasticsearch/docs/4.4.12/reference/html/#elasticsearch-migration-guide-4.2-4.3.breaking-changes:~:text=How%20to%20use%20the%20new%20client
    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {

        HttpHeaders compatibilityHeaders = new HttpHeaders();
        compatibilityHeaders.add("Accept", "application/vnd.elasticsearch+json;compatible-with=7");
        compatibilityHeaders.add("Content-Type", "application/vnd.elasticsearch+json;" + "compatible-with=7");

        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("localhost:9200")
                .withDefaultHeaders(compatibilityHeaders)
                .build();

        return RestClients.create(clientConfiguration).rest();
    }
}