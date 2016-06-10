package com.x.services;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpClient;
import io.vertx.rxjava.core.http.HttpClientRequest;
import io.vertx.rxjava.core.http.HttpClientResponse;
import rx.Observable;
import rx.Subscriber;

@RunWith(VertxUnitRunner.class)
public class RxTests {
	
	private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RxTests.class);
	private static Vertx vertx;
	
	@BeforeClass
	public static void before(TestContext context){
		vertx = Vertx.vertx();
		vertx.deployVerticle(MainVerticle.class.getName(), context.asyncAssertSuccess());
	}
	
	@AfterClass
	public static void tearDown(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}
	
	@Test
	public void fetchUrls(TestContext context){
		final Async async = context.async();
		
		String urls[] = new String[]{"http://ya.ru", "http://google.ru"};
		
		Observable.from(urls)
			.map(url -> {
				return getResponseObservable(url);
			})
			.flatMap(resp -> resp)
			.map(resp -> resp.statusCode())
			.subscribe(val -> {
					LOG.debug("Status: {}", val);
				},
				error -> {
					try{
						LOG.error("Error onError", error);
						context.fail(error);
					}catch (Throwable e){}
				},
				() -> {
					async.complete();
				}
			);
	}
	@Test(expected=IOException.class)
	public void getWrongUrl(TestContext context){
		final Async async = context.async();
		String urls[] = new String[]{"http://asdfas.ruas"};
		Observable.from(urls)
		.map(url -> {
			return getResponseObservable(url);
		})
		.flatMap(resp -> resp)
		.map(resp -> resp.statusCode())
		.subscribe(val -> {
				LOG.debug("Status: {}", val);
			},
			error -> {
				try{
					LOG.info("EXPECTED ERROR", error.getMessage());
					context.fail(error);
				}catch (Throwable e){}
			},
			() -> {
				async.complete();
			}
		);
	}
	//@Test
	public void testRX(){
		Observable.just("Hello, world!", "Hola mundo!")
		.map(s -> s + " -Dan")
	    .subscribe(s -> System.out.println(s));
	}
	
	private Observable<HttpClientResponse> getResponseObservable(String url){
		return Observable.create( (Subscriber<? super HttpClientResponse> subscriber) -> {
			if ( subscriber.isUnsubscribed() ) {return;}
			HttpClient client = vertx.createHttpClient();
			HttpClientRequest req = client.getAbs(url);
			req.handler(resp -> {
				resp.exceptionHandler( error -> {
					subscriber.onError(new RuntimeException("Response failed for "+url, error));
				});
				subscriber.onNext( resp );
				subscriber.onCompleted();
			});
			req.exceptionHandler(error -> {
				//subscriber.onError(new RuntimeException("Request failed for "+url, error));
				subscriber.onError(new IOException(error));
			});
			req.end();
		});
	}
	
	
	
}
