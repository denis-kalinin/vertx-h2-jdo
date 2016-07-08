package com.x.models;

import java.io.Serializable;
import java.math.BigDecimal;
/**
 * Concise information about accounts:
 * <ul>
 *   <li>number of accounts</li>
 *   <li>total balance of all accounts</li>
 * </ul>
 * @author Kalinin_DP
 *
 */
public class Balance implements Serializable{
	private static final long serialVersionUID = -1144879828366837759L;
	private BigDecimal balance = BigDecimal.ZERO;
	private long accounts;
	/**
	 * @return total balance of all accounts
	 */
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	/**
	 * @return the number of accounts
	 */
	public long getAccounts() {
		return accounts;
	}
	public void setAccounts(long accounts) {
		this.accounts = accounts;
	}
}
