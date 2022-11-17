package com.zakiis.elasticsearch.demo.test.lowapi;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zakiis.common.JsonUtil;
import com.zakiis.elasticsearch.demo.model.Product;
import com.zakiis.elasticsearch.demo.test.LowApiTest;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.transport.endpoints.BooleanResponse;

public class LoadingApiFromJsonTest extends LowApiTest {
	
	Logger log = LoggerFactory.getLogger(LoadingApiFromJsonTest.class);
	String indexName = "elasticsearch-demo.product";
	
	/**
	 * ES Date类型可以有以下三种表现形式：
	 * 1. 日期格式的字符串，如2022-11-16或2022-11-16 14:52:03
	 * 2. long类型的毫秒数，milliseconds-since-the-epoch
	 * 3. int类型的秒数，seconds-since-the-epoch
	 * ES内部会将所有的Date类型转换成UTC时间戳，最后以milliseconds-since-the-epoch 作为存储的格式
	 */
	@Test
	public void testCreateIndex() throws ElasticsearchException, IOException {
		BooleanResponse booleanResponse = client.indices().exists(e -> e.index(indexName));
		if (booleanResponse.value()) {
			client.indices()
				.delete(d -> d.index(indexName));
			
		}
		Reader indexCreateJson = new StringReader("{"
				+ "  \"mappings\": {"
				+ "      \"properties\": {     "
				+ "        \"name\": {"
				+ "          \"type\": \"text\","
				+ "          \"fields\": {"
				+ "            \"keyword\": {"
				+ "              \"type\": \"keyword\","
				+ "              \"ignore_above\": 256"
				+ "            }"
				+ "          }"
				+ "        },"
				+ "        \"amount\": {"
				+ "          \"type\": \"long\""
				+ "        },"
				+ "        \"price\": {"
				+ "          \"type\": \"double\""
				+ "        },"
				+ "        \"createDate\": {"
				+ "          \"type\": \"date\","
				+ "          \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis\""
				+ "        },"
				+ "        \"updateDate\": {"
				+ "          \"type\": \"date\","
				+ "          \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis\""
				+ "        }"
				+ "      }"
				+ "  }"
				+ "}");
		client.indices().create(c -> c
				.index(indexName)
				.withJson(indexCreateJson));
	}
	
	@Test
	public void testPrepareData() throws ElasticsearchException, IOException {
		//单条数据插入
		Reader docCreateJson= new StringReader("{'name': 'iphone 14', 'price': 3999, 'discount': true, 'amount': 10, 'createDate': '2022-11-16 16:07:00', 'updateDate': '2022-11-16 16:07:00'}".replaceAll("'", "\""));
		IndexResponse response = client.index(i -> i
				.index(indexName)
				.id("df2df469-d878-48a8-9473-d85899f3d2d5")
				.withJson(docCreateJson));
		log.info("Indexed with version:{}", response.version());
		//批量数据插入
		List<Product> producList = mockProducList();
		BulkRequest.Builder br = new BulkRequest.Builder();
		for (Product product : producList) {
			br.operations(op -> op
					.index(idx -> idx
							.index(indexName)
							.id(product.getId())
							.document(product)
					)
			);
		}
		BulkResponse bulkResponse = client.bulk(br.build());
		if (bulkResponse.errors()) {
			log.error("Bulk had errors");
			for (BulkResponseItem item : bulkResponse.items()) {
				if (item.error() != null) {
					log.error(item.error().reason());
				}
			}
		}
	}
	
	/**
	 * 单词匹配，查询条件不会做分词处理
	 */
	@Test
	public void testTermQuery() throws ElasticsearchException, IOException {
		Reader termQueryJson= new StringReader(("{"
				+ "  'query': {"
				+ "    'term': {"
				+ "      'name.keyword': 'iphone 14'"
				+ "    }"
				+ "  }"
				+ "}").replaceAll("'", "\""));
		SearchResponse<Product> response = client.search(q -> q
				.index(indexName)
				.withJson(termQueryJson), Product.class);
		log.info("query matched record: {}", response.hits().total());
		for (Hit<Product> hit : response.hits().hits()) {
			Product product = hit.source();
			product.setId(hit.id());
			log.info("{}", product);
		}
	}
	
	/**
	 * 单词匹配，查询条件不会做分词处理
	 */
	@Test
	public void testTermsQuery() throws ElasticsearchException, IOException {
		Reader termQueryJson= new StringReader(("{"
				+ "  'query': {"
				+ "    'terms': {"
				+ "      'name.keyword': ['iphone 14', 'iphone 14 Pro']"
				+ "    }"
				+ "  }"
				+ "}").replaceAll("'", "\""));
		SearchResponse<Product> response = client.search(q -> q
				.index(indexName)
				.withJson(termQueryJson), Product.class);
		log.info("query matched record: {}", response.hits().total());
		for (Hit<Product> hit : response.hits().hits()) {
			Product product = hit.source();
			product.setId(hit.id());
			log.info("{}", product);
		}
	}
	
	/**
	 * 用于date或者number类型的字段范围查询
	 */
	@Test
	public void testRangeQuery() throws ElasticsearchException, IOException {
		Reader termQueryJson= new StringReader(("{"
				+ "  'query': {"
				+ "    'range': {"
				+ "      'price': {"
				+ "        'gte': 6000,"
				+ "        'lte': 10000"
				+ "      }"
				+ "    }"
				+ "  },"
				+ "  'sort': ["
				+ "    {"
				+ "      'price': {"
				+ "        'order': 'desc'"
				+ "      }"
				+ "    }"
				+ "  ]"
				+ "}").replaceAll("'", "\""));
		SearchResponse<Product> response = client.search(q -> q
				.index(indexName)
				.withJson(termQueryJson), Product.class);
		log.info("query matched record: {}", response.hits().total());
		for (Hit<Product> hit : response.hits().hits()) {
			Product product = hit.source();
			product.setId(hit.id());
			log.info("{}", product);
		}
	}
	
	/**
	 * 全文匹配，会先对查询条件进行分词处理
	 * @throws IOException 
	 * @throws ElasticsearchException 
	 */
	@Test
	public void testMatchQuery() throws ElasticsearchException, IOException {
		Reader termQueryJson= new StringReader(("{"
				+ "  'query': {"
				+ "    'match': {"
				+ "      'name': 'Pro Max'"
				+ "    }"
				+ "  }"
				+ "}").replaceAll("'", "\""));
		SearchResponse<Product> response = client.search(q -> q
				.index(indexName)
				.withJson(termQueryJson), Product.class);
		log.info("query matched record: {}", response.hits().total());
		for (Hit<Product> hit : response.hits().hits()) {
			Product product = hit.source();
			product.setId(hit.id());
			log.info("{}", product);
		}
	}
	
	/**
	 * 全文匹配，会先对查询条件进行分词处理，分词的条件会遵守顺序
	 * @throws IOException 
	 * @throws ElasticsearchException 
	 */
	@Test
	public void testMatchPhraseQuery() throws ElasticsearchException, IOException {
		Reader termQueryJson= new StringReader(("{"
				+ "  'query': {"
				+ "    'match_phrase': {"
				+ "      'name': 'Pro Max'"
				+ "    }"
				+ "  }"
				+ "}").replaceAll("'", "\""));
		SearchResponse<Product> response = client.search(q -> q
				.index(indexName)
				.withJson(termQueryJson), Product.class);
		log.info("query matched record: {}", response.hits().total());
		for (Hit<Product> hit : response.hits().hits()) {
			Product product = hit.source();
			product.setId(hit.id());
			log.info("{}", product);
		}
	}
	
	@Test
	public void testBoolQuery() throws ElasticsearchException, IOException {
		Reader termQueryJson= new StringReader(("{"
				+ "  'query': {"
				+ "    'bool': {"
				+ "      'must': ["
				+ "        {"
				+ "          'match': {"
				+ "            'name': '14 Plus Max'"
				+ "          }"
				+ "        },"
				+ "        {"
				+ "          'range': {"
				+ "            'price': {"
				+ "              'lte': 8000"
				+ "            }"
				+ "          }"
				+ "        }"
				+ "      ],"
				+ "      'should': {"
				+ "        'exists': {"
				+ "          'field': 'discount'"
				+ "        }"
				+ "      },"
				+ "      'filter': {"
				+ "        'range': {"
				+ "          'amount': {"
				+ "            'gt': 0"
				+ "          }"
				+ "        }"
				+ "      }"
				+ "    }"
				+ "  }"
				+ "}").replaceAll("'", "\""));
		SearchResponse<Product> response = client.search(q -> q
				.index(indexName)
				.withJson(termQueryJson), Product.class);
		log.info("query matched record: {}", response.hits().total());
		for (Hit<Product> hit : response.hits().hits()) {
			Product product = hit.source();
			product.setId(hit.id());
			log.info("{}", product);
		}
	}
	
	private List<Product> mockProducList() {
		return Arrays.asList(JsonUtil.toObject("{'id': '527c604b-3502-40ec-909d-9c494ffbb89a','name': 'iphone 14', 'price': 5399, 'amount': 2000, 'createDate': '2022-11-16 16:07:00', 'updateDate': '2022-11-16 16:07:00'}".replaceAll("'", "\""), Product.class)
				, JsonUtil.toObject("{'id': '527c604b-3502-40ec-909d-9c494ffbb89b','name': 'iphone 14', 'price': 5399, 'amount': 2000, 'createDate': '2022-11-16 16:07:00', 'updateDate': '2022-11-16 16:07:00'}".replaceAll("'", "\""), Product.class)
				, JsonUtil.toObject("{'id': '18627f23-f376-4519-9749-ac6f5152f371','name': 'iphone 14 Plus', 'price': 6399, 'amount': 0, 'createDate': '2022-11-16 16:07:00', 'updateDate': '2022-11-16 16:07:00'}".replaceAll("'", "\""), Product.class)
				, JsonUtil.toObject("{'id': '18627f23-f376-4519-9749-ac6f5152f372','name': 'iphone Plus 14', 'price': 1399, 'amount': 25000, 'createDate': '2022-11-16 16:07:00', 'updateDate': '2022-11-16 16:07:00'}".replaceAll("'", "\""), Product.class)
				, JsonUtil.toObject("{'id': '3681c4e3-e72a-4aca-aa91-7bc47d4c8c93','name': 'iphone 14 Pro', 'price': 7399, 'amount': 3500, 'createDate': '2022-11-16 16:07:00', 'updateDate': '2022-11-16 16:07:00'}".replaceAll("'", "\""), Product.class)
				, JsonUtil.toObject("{'id': 'cf085030-b376-48cc-996c-f685c1e8b176','name': 'iphone 14 Pro Max', 'price': 8399, 'amount': 1500, 'createDate': '2022-11-16 16:07:00', 'updateDate': '2022-11-16 16:07:00'}".replaceAll("'", "\""), Product.class)
				, JsonUtil.toObject("{'id': 'cf085030-b376-48cc-996c-f685c1e8b177','name': 'iphone 14 Max Pro', 'price': 2399, 'amount': 15000, 'createDate': '2022-11-16 16:07:00', 'updateDate': '2022-11-16 16:07:00'}".replaceAll("'", "\""), Product.class)
				, JsonUtil.toObject("{'id': 'cf085030-b376-48cc-996c-f685c1e8b178','name': 'ipad Pro 2021', 'price': 5299, 'amount': 1500, 'createDate': '2022-11-16 16:07:00', 'updateDate': '2022-11-16 16:07:00'}".replaceAll("'", "\""), Product.class)
				);
	}

}
