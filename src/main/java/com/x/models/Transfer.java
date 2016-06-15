package com.x.models;

import java.math.BigDecimal;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable (detachable="true")
@DatastoreIdentity(strategy=IdGeneratorStrategy.IDENTITY)
public class Transfer {
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
	private String id;
	private Account from;
	private Account to;
	@Column(jdbcType="DECIMAL", length=20, scale=2)
	private BigDecimal amount;
	private Date date;
	private Status status;
	private ServiceMessage message;
	
	public String getId(){
		return id;
	}
	
	public Account getFrom() {
		return from;
	}


	public void setFrom(Account from) {
		this.from = from;
	}


	public Account getTo() {
		return to;
	}


	public void setTo(Account to) {
		this.to = to;
	}


	public BigDecimal getAmount() {
		return amount;
	}


	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}


	public Date getDate() {
		return date;
	}


	public void setDate(Date date) {
		this.date = date;
	}


	public Status getStatus() {
		return status;
	}


	public void setStatus(Status status) {
		this.status = status;
	}


	public ServiceMessage getMessage() {
		return message;
	}


	public void setMessage(ServiceMessage message) {
		this.message = message;
	}


	public enum Status{
		Success,
		Failed;
	}
}
