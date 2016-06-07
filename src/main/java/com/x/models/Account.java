package com.x.models;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@PersistenceCapable (detachable="true")
@DatastoreIdentity(strategy=IdGeneratorStrategy.IDENTITY)
public class Account implements Serializable {

	private static final long serialVersionUID = 7057347509311290821L;
	
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
	private String id;
	private String customerId;
	private BigDecimal deposit = BigDecimal.ZERO;
	
	public Account(String customerId){
		this.customerId = customerId;
	}
	
	public String getId() {
		return id;
	}
	public BigDecimal getDeposit() {
		return deposit;
	}
	public void setDeposit(BigDecimal deposit) {
		this.deposit = deposit;
	}
	public String getCustomerId(){
		return customerId;
	}
	/**
	 * This is <code>factory</code> method to be used internally by Jackson to deserialize JSON to POJO
	 * &mdash; Account doesn't have <em>empty</em> constructor considering security, so this <em>factory</em> method is annotated for Jackson to create POJO instance.
	 * @param id Jackson should itself support this value
	 * @return Account as POJO from JSON
	 */
	@JsonCreator
	protected static Account createAccountFromJSON(@JsonProperty("customerId") String customerId) {
		return new Account(customerId);
	}
	
}
