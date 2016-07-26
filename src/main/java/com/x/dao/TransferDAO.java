package com.x.dao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.x.models.Transfer;
/**
 * Transfers DAO
 * @author Kalinin_DP
 *
 */
public interface TransferDAO {
	/**
	 * Gets a list of transfers for an account
	 * @param accountId ID/number of the account
	 * @return list of transfers to/from the account
	 */
	List<Transfer> getTranfersForAccount(String accountId);
	/**
	 * Commit transfer &mdash; change amounts on corresponding accounts and register transfer
	 * @param transferToCommit transfer to be committed
	 * @return committed transfer with assigned {@linkplain Transfer#getId() ID}
	 * @throws Exception if transaction failed
	 */
	Transfer commitTransfer(Transfer transferToCommit) throws Exception;
	/**
	 * Gets transfer by ID
	 * @param id of transfer
	 * @return optional of transfer
	 */
	Optional<Transfer> getTransferById(String id);
	/**
	 * Gets all transfers
	 * @return all transfers
	 */
	Collection<Transfer> getTransfers();
}
