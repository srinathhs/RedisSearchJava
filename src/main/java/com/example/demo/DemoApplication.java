package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.redislabs.lettusearch.RediSearchClient;
import com.redislabs.lettusearch.RediSearchCommands;
import com.redislabs.lettusearch.StatefulRediSearchConnection;
import com.redislabs.lettusearch.index.CreateOptions;
import com.redislabs.lettusearch.index.DropOptions;
import com.redislabs.lettusearch.index.Schema;
import com.redislabs.lettusearch.index.field.TextField;
import com.redislabs.lettusearch.search.Document;
import com.redislabs.lettusearch.search.SearchResults;

import io.lettuce.core.RedisURI;

@SpringBootApplication
@Configuration
public class DemoApplication implements CommandLineRunner {

	@Autowired
	private StringRedisTemplate redisTemplate;

	private String INDEX = "test";

	private static Logger LOG = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);

	}

	@Override
	public void run(String... args) throws Exception {
		redisTemplate.opsForValue().set("test", "me");
		LOG.warn(redisTemplate.opsForValue().get("test"));

		RedisURI sentilelUri = RedisURI.builder().withSentinel("10.170.3.171", 26379).withSentinelMasterId("mymaster")
				.build();

		RediSearchClient client = RediSearchClient.create(sentilelUri);
		StatefulRediSearchConnection<String, String> conn = client.connect();
		RediSearchCommands<String, String> commands = conn.sync();

		Schema schema = Schema.builder().field(TextField.builder().name("name").build()).build();

		commands.create(INDEX, schema, CreateOptions.builder().build());

		Document<String, String> doc = Document.<String, String>builder().id("1").score(1D).build();
		doc.put("name", "Sagar Kumar");
		commands.add("test", doc, null);
		
		doc = Document.<String, String>builder().id("2").score(1D).build();
		doc.put("name", "Sag par");
		commands.add("test", doc, null);

		SearchResults<String, String> results = commands.search(INDEX, "Sag*");
		
		LOG.warn("Results :"+results.getCount());
		results.forEach(r -> {
			LOG.warn(r.get("name"));
		});
		
		results = commands.search(INDEX, "kumar*");
		
		LOG.warn("Results :"+results.getCount());
		results.forEach(r -> {
			LOG.warn(r.get("name"));
		});
		
		commands.drop(INDEX, DropOptions.builder().build());
	}

}
