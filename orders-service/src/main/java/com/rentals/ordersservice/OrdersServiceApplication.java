package com.rentals.ordersservice;

import com.rentals.ordersservice.configuration.RedisConfigProperties;
import com.rentals.ordersservice.model.ProductOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.apache.http.HttpHeaders.TIMEOUT;

@SpringBootApplication(exclude = {
		MongoAutoConfiguration.class,
		MongoDataAutoConfiguration.class
})
@EnableEurekaClient
public class OrdersServiceApplication {

	@Autowired
	private RedisConfigProperties redisConfigProperties;
	private final Logger LOGGER = LoggerFactory.getLogger(OrdersServiceApplication.class);

	@Bean
	public JedisConnectionFactory jedisConnectionFactory(){
		String host = redisConfigProperties.getHost();
		int port = redisConfigProperties.getPort();
		LOGGER.info("REDIS HOST {} PORT {}", host, port);
		return new JedisConnectionFactory(new RedisStandaloneConfiguration(host, port));
	}

	@Bean
	public RedisTemplate<String, ProductOrder> redisTemplate(){
		RedisTemplate<String, ProductOrder> template = new RedisTemplate<>();
		template.setConnectionFactory(jedisConnectionFactory());
		return template;
	}

	@Bean
	@LoadBalanced
	public RestTemplate getRestTemplate(){
		RestTemplate restTemplate = new RestTemplate();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		restTemplate.setRequestFactory(requestFactory);
		return restTemplate;
	}

	public static void main(String[] args) {
		SpringApplication.run(OrdersServiceApplication.class, args);
	}

}
