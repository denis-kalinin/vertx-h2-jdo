package com.x.services;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.rxjava.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.http.HttpClient;

import com.x.models.Account;


@RunWith(VertxUnitRunner.class)
public class ApiTests {
	
	private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ApiTests.class);
	private Vertx vertx;

	@Before
	public void bootApp(TestContext context) {
		//Launcher.executeCommand("run", MainVerticle.class.getName());
		vertx = Vertx.vertx();
		vertx.deployVerticle(MainVerticle.class.getName(), context.asyncAssertSuccess());
		LOG.debug("Test Vert.x booted");
	}
	@After
	public void tearDown(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	@Test
	public void testHelloEndpoint(TestContext context) {
		LOG.debug("Getting bank accounts");
		final Async async = context.async();
		
		HttpClient client = vertx.createHttpClient();
		client.getNow(8080, "localhost", "/bank/accounts", response -> {
			response.handler(body -> {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Account> map;
				try {
					map = mapper.readValue(body.toString(), new TypeReference<Map<String, Account>>(){});
					context.assertTrue(map.containsKey("001"), "The expected ID 001 is not found");
					context.assertTrue(map.get("001").getId().equals("001"), "Key and Account ID mismatch");
				} catch (Exception e) {
					context.fail(e);
				} finally {
					async.complete();
				}
			});
		});
	}
	
	@Test
	public void notExistedAccount(TestContext context) {
		LOG.debug("Getting bank accounts");
		final Async async = context.async();
		
		HttpClient client = vertx.createHttpClient();
		client.getNow(8080, "localhost", "/bank/accounts/004", response -> {
			response.handler(body -> {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Account> map;
				try {
					map = mapper.readValue(body.toString(), new TypeReference<Map<String, Account>>(){});
					context.assertTrue(map.containsKey("001"), "The expected ID 001 is not found");
					context.assertTrue(map.get("001").getId().equals("001"), "Key and Account ID mismatch");
				} catch (Exception e) {
					context.fail(e);
				} finally {
					async.complete();
				}
			});
		});
	}
	
	
	
	//@Test
	public void testSendMoney(TestContext context) {
		LOG.debug("Getting bank accounts");
		final Async async = context.async();
		
		HttpClient client = vertx.createHttpClient();
		client.getNow(8080, "localhost", "/bank/accounts", response -> {
			response.handler(body -> {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Account> map;
				try {
					map = mapper.readValue(body.toString(), new TypeReference<Map<String, Account>>(){});
					context.assertTrue(map.containsKey("001"), "The expected ID 001 is not found");
					context.assertTrue(map.get("001").getId().equals("001"), "Key and Account ID mismatch");
				} catch (Exception e) {
					context.fail(e);
				} finally {
					async.complete();
				}
			});
		});
	}
}
