package com.zakiis.elasticsearch.demo.test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.zakiis.elasticsearch.demo.model.Person;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class LowApiTest {
	
	protected static ElasticsearchClient client;
	
	@BeforeAll
	public static void before() throws JsonMappingException, JsonProcessingException {
		// credentials
		BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
		credsProv.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "123456"));
		// object mapper
		ObjectMapper mapper = new ObjectMapper();
		mapper
			.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
			.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		//client
		RestClient restClient = RestClient.builder(
				new HttpHost("192.168.137.100", 9200),
				new HttpHost("192.168.137.100", 9201),
				new HttpHost("192.168.137.100", 9202))
				.setHttpClientConfigCallback(hc -> hc
						.setDefaultCredentialsProvider(credsProv))
				.build();
		RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(mapper));
		client = new ElasticsearchClient(transport);
	}
	
	
	/**
	 * 当进行精确查找时，会使用过滤器，过滤器执行效率高，不会计算相关度评分而且很容易被缓存。
	 * Term query会去倒排索引中进行精确匹配，它并不知道分词器的存在，适合keyword、numeric、date
	 * @throws ElasticsearchException
	 * @throws IOException
	 */
	@Test
	public void testTermQuery() throws ElasticsearchException, IOException {
		SearchResponse<Person> search = client.search(s -> s.index("elasticsearch-demo.person")
				.query(q -> q.term(t -> t.field("name.keyword")	//string会被分词，所以这里要用<fieldName>.keyword来查询
						.value(v -> v.stringValue("CICOX Jvum"))))
//				.query(q -> q.term(t -> t.field("age")
//						.value(v -> v.longValue(20L))))
				, Person.class);
		for (Hit<Person> hit : search.hits().hits()) {
			// _id在hit对象中，不在_source里面
			hit.source().setId(hit.id());
			System.out.println(hit.source());
		}
	}
	
	@Test
	public void testTermsQuery() throws ElasticsearchException, IOException {
		SearchResponse<Person> search = client.search(s -> s.index("elasticsearch-demo.person")
				.query(q -> q.terms(t -> t.field("name.keyword")
						.terms(f -> f
								.value(Arrays.asList(FieldValue.of("CICOX Jvum"), FieldValue.of("zux LOx"))))
						))
				, Person.class);
		for (Hit<Person> hit : search.hits().hits()) {
			// _id在hit对象中，不在_source里面
			hit.source().setId(hit.id());
			System.out.println(hit.source());
		}
	}
	
	@Test
	public void testRangeQuery() throws ElasticsearchException, IOException {
		//TODO
		SearchResponse<Person> search = client.search(s -> s.index("elasticsearch-demo.person")
				.query(q -> q.range(r -> r.field("age").gte(null)))
				, Person.class);
	}
	
	@Test
	public void testLoadingApiObjFromJson() {
		
	}
	
	@Test
	public void testCreateIndex() throws ElasticsearchException, IOException {
		client.indices()
			.create(c -> c.index("elasticsearch-demo.product"));
	}
}
