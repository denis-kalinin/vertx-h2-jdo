package com.x.models;

import java.util.List;

public interface TransferDAO {
	
	List<Transfer> getTranfersForAccount(String accountId);
	void commitTransfer(Transfer transferToCommit);

}
