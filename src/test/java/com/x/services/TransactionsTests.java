package com.x.services;

import java.io.IOException;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.httpcomponents.RamlHttpClient;
import guru.nidi.ramltester.jaxrs.CheckingWebTarget;
import guru.nidi.ramltester.junit.RamlMatchers;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;

@RunWith(VertxUnitRunner.class)
public class TransactionsTests {

	private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TransactionsTests.class);
	private static Vertx vertx;
	
	private static final RamlDefinition api = RamlLoaders.fromClasspath().load("/api/accounts.yaml");//.assumingBaseUri("http://localhost:3423/bank");
	private ResteasyClient client = new ResteasyClientBuilder().build();
	private CheckingWebTarget checking;
	private RamlHttpClient browser;
	
	@BeforeClass
	public static void before(TestContext context){
		LOG.debug("VALIDATE API: {}", api.validate());
		//Assert.assertThat(api.validate(), RamlMatchers.validates());
		vertx = Vertx.vertx();
		vertx.deployVerticle(MainVerticle.class.getName(), context.asyncAssertSuccess());
	}
	@AfterClass
	public static void tearDown(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}
	
	@Before
	public void createTarget() {
		//checking = api.createWebTarget(client.target("http://localhost:8029"));
		browser = api.createHttpClient();
		//final Async async = context.async();
		//async.complete();
	}
	
	@Test
	public void emptyTest(){}
	
	//@Test
	public void fetchUrls(TestContext context) throws IOException{
		final Async async = context.async();
		/*
		WebTarget webTarget = checking.path("/bank/accounts/");
		LOG.debug("Requesting {}", webTarget.getUri());
		Response resp = checking.path("/accounts").request().get();
		LOG.debug("Code: {}, MediaType: {} Location: {}", resp.getStatus(), resp.getMediaType(), resp.getStringHeaders().size());
		LOG.debug("HEADER: {}", resp.getStringHeaders().keySet().iterator().next());
		LOG.debug("ENTITY: {}", resp.getEntity());
		LOG.debug("{}", checking.getLastReport());
		*/
		HttpGet get = new HttpGet("http://localhost:8029/bank/accounts");
		HttpResponse response = browser.execute(get);
		LOG.debug("response code: {}", response.getStatusLine().getStatusCode());
		LOG.debug("REPORT: {}", browser.getLastReport());
		Assert.assertThat(browser.getLastReport(), RamlMatchers.checks());
		async.complete();
	}
}
