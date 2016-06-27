package com.x.models;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@PersistenceCapable (detachable="true")
@FetchGroup(name="account-metainfo", members={@Persistent(name="id"), @Persistent(name="customerId")})
public class Account implements Serializable {

	private static final long serialVersionUID = 7057347509311290821L;
	
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
	private String id;
	private String customerId;
	@Column(jdbcType="DECIMAL", length=20, scale=2)
	private BigDecimal deposit = BigDecimal.ZERO;
	
	private Account(){}
	public Account (String customerId){
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
	public void setCustomerId(String customerId){
		this.customerId = customerId;
	}
	/**
	 * This is <code>factory</code> method to be used internally by Jackson to deserialize JSON to POJO
	 * &mdash; Account doesn't have public <em>empty</em> constructor considering security, so this <em>factory</em> method is annotated for Jackson to create POJO instance.
	 * @param id Jackson should itself support this value
	 * @return Account as POJO from JSON
	 */
	@JsonCreator
	public static Account createAccountFromJSON(@JsonProperty("id") String id) {
		Account account = new Account();
		account.id = id;
		return account;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Account ID=").append(id).append(" Customer=").append(customerId).append(" deposit=").append(deposit);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((customerId == null) ? 0 : customerId.hashCode());
		result = prime * result + ((deposit == null) ? 0 : deposit.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Account other = (Account) obj;
		if (customerId == null) {
			if (other.customerId != null)
				return false;
		} else if (!customerId.equals(other.customerId))
			return false;
		if (deposit == null) {
			if (other.deposit != null)
				return false;
		} else if (!deposit.equals(other.deposit))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
