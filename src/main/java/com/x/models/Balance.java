package com.x.models;

import java.io.Serializable;
import java.math.BigDecimal;

public class Balance implements Serializable{
	private static final long serialVersionUID = -1144879828366837759L;
	private BigDecimal balance = BigDecimal.ZERO;
	private long accounts;
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public long getAccounts() {
		return accounts;
	}
	public void setAccounts(long accounts) {
		this.accounts = accounts;
	}
}
