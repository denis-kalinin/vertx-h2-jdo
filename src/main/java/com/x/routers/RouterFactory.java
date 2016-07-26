package com.x.routers;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public interface RouterFactory {
	Router getRouter(Vertx vertx);
	
	/**
	 * Mount point of {@linkplain ApiEndpoints} &mdash; <code>/bank</code>
	 */
	public static String MOUNTPOINT = "/bank";
	/**
	 * Api URL endpoints
	 * @author Kalinin_DP
	 *
	 */
	public static enum ApiEndpoints{
		/**
		 *  /accounts
		 */
		ACCOUNTS("/accounts"),
		/**
		 *  /transfers
		 */
		TRANSFERS("/transfers"),
		/**
		 *  /balance
		 */
		BALANCE("/balance");
		
		private String path;
		private ApiEndpoints(String path){
			this.path = path;
		}
		public String getPath(){
			return path;
		}
		@Override
		public String toString(){
			return path;
		}
	}

}
