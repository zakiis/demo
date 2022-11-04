package com.zakiis.elasticsearch.demo.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;

import com.zakiis.elasticsearch.demo.model.Person;

@SpringBootTest
public class ElasticSearchDemoApplicationTests {
	
	Logger log = LoggerFactory.getLogger(ElasticSearchDemoApplicationTests.class);

	@Autowired
	ElasticsearchRestTemplate template;
	
	@Test
	void contextLoads() {
//		initData(10000);
//		criteriaQuery();
//		stringQuery();
		nativeQuery();
	}
	
	private void criteriaQuery() {
		Criteria criteria = new Criteria("age")
				.greaterThanEqual(18)
				.lessThan(30)
				.subCriteria(
					new Criteria().or("name").startsWith("HM")	//matches必须分词与之匹配，contains、startsWith&endsWith则可以部分匹配
						.or("name").is("CICOX Jvum"));
		Query query = new CriteriaQuery(criteria);
		SearchHits<Person> searchHits = template.search(query, Person.class);
		printSearchHits(searchHits);
	}
	
	// 直接使用ES的json query语法，可以从kibana上请求参数中拷贝出来：age >= 18 and age < 30 and (name: HM* or name: "CICOX Jvum")
	private void stringQuery() {
		Query query = new StringQuery("{\"bool\":{\"filter\":[{\"bool\":{\"should\":[{\"range\":{\"age\":{\"gte\":\"18\"}}}],\"minimum_should_match\":1}},{\"bool\":{\"should\":[{\"range\":{\"age\":{\"lt\":\"30\"}}}],\"minimum_should_match\":1}},{\"bool\":{\"should\":[{\"bool\":{\"should\":[{\"query_string\":{\"fields\":[\"name\"],\"query\":\"HM*\"}}],\"minimum_should_match\":1}},{\"bool\":{\"should\":[{\"match_phrase\":{\"name\":\"CICOX Jvum\"}}],\"minimum_should_match\":1}}],\"minimum_should_match\":1}}]}}")
				.setPageable(PageRequest.of(0, 20));
		SearchHits<Person> searchHits = template.search(query, Person.class);
		printSearchHits(searchHits);
	}
	
	private void nativeQuery() {
		Query query = new NativeSearchQueryBuilder()
			.withAggregations(AggregationBuilders.terms("ageDistribution").field("age").size(5))
//			.withQuery(QueryBuilders.matchPhrasePrefixQuery("name", "HM"))
			.withQuery(QueryBuilders.queryStringQuery("*HM*").field("name"))
			.withMaxResults(0)
			.build();
//			.setPageable(PageRequest.of(0, 1));
		SearchHits<Person> searchHits = template.search(query, Person.class);
		Aggregations aggregations = (Aggregations)searchHits.getAggregations().aggregations();
		Terms terms = (Terms)aggregations.get("ageDistribution");
		for (Bucket bucket : terms.getBuckets()) {
			System.out.println(String.format("Age: %s, Count: %s", bucket.getKey(), bucket.getDocCount()));
		}
		printSearchHits(searchHits);
	}
	
	private void printSearchHits(SearchHits<Person> searchHits) {
		log.info("matched records: {}", searchHits.getTotalHits());
		searchHits.forEach(hit -> {
			System.out.println(hit.getContent());
		});
	}

	private void initData(int count) {
		List<Person> persons = mockPerson(count);
		long start = System.currentTimeMillis();
		template.save(persons);
		long end = System.currentTimeMillis();
		log.info("batch insert {} records cost {} milliseconds", count, end - start);
	}
	
	private List<Person> mockPerson(int count) {
		List<Person> persons = new ArrayList<Person>(count);
		for (int i = 0; i < count; i++) {
			Person person = new Person();
			person.setAge(RandomUtils.nextInt(1, 100));
			person.setName(RandomStringUtils.randomAlphabetic(3, 8) + " " + RandomStringUtils.randomAlphabetic(2, 5));
			persons.add(person);
		}
		return persons;
	}
}
