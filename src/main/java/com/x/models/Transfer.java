package com.x.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@PersistenceCapable (detachable="true")
//@DatastoreIdentity(strategy=IdGeneratorStrategy.IDENTITY)
@FetchGroup(name="transfer-base", members={
		@Persistent(name="id"),
		@Persistent(name="to"),
		@Persistent(name="from"),
		@Persistent(name="amount"),
		@Persistent(name="date"),
		@Persistent(name="status")
})
//@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Transfer implements Serializable {
	private static final long serialVersionUID = -7655147924492209453L;
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
	private String id;
	
	@Persistent(defaultFetchGroup="true")
	@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
	@JsonIdentityReference(alwaysAsId=true)
	private Account from;
	
	@Persistent(defaultFetchGroup="true")
	@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
	@JsonIdentityReference(alwaysAsId=true)
	private Account to;
	
	@Column(jdbcType="DECIMAL", length=20, scale=2)
	private BigDecimal amount;
	
	@Persistent(customValueStrategy="timestamp")
	private Date date;
	private Status status;
	private ServiceMessage message;
	
	public Transfer(){}
	
	public Transfer(@JsonProperty("to") String toId, @JsonProperty("from") String fromId){
		if(toId!=null){
			this.to = Account.createAccountFromJSON(toId);
		}
		if(fromId!=null){
			this.from = Account.createAccountFromJSON(fromId);
		}
	}
	
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
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Transfer ID=").append(id).append(" Sum=").append(amount).append(" From=")
			.append(from != null ? (from.getCustomerId() == null ? from.getId() : from.getCustomerId()) : null)
			.append(" To=")
			.append(to != null ? (to.getCustomerId() == null ? to.getId() : to.getCustomerId() ) : null)
			.append(" date=").append(date);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transfer other = (Transfer) obj;
		if (amount == null) {
			if (other.amount != null)
				return false;
		} else if (!amount.equals(other.amount))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (status != other.status)
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}
	
}