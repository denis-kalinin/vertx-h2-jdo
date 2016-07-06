package com.x.dao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.x.models.Transfer;

public interface TransferDAO {
	
	List<Transfer> getTranfersForAccount(String accountId);
	Transfer commitTransfer(Transfer transferToCommit) throws Exception;
	Optional<Transfer> getTransferById(String id);
	Collection<Transfer> getTransfers();
}
