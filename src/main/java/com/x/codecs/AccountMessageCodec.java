package com.x.codecs;

import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;

import com.x.models.Account;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
/**
 * A message codec allows an {@linkplain Account} message type to be marshalled across the event bus.
 * @author Kalinin_DP
 * @see MessageCodec
 */
@Singleton
public class AccountMessageCodec implements MessageCodec<Account, Account> {

	@Override
	public void encodeToWire(Buffer buffer, Account account) {
		buffer.appendBytes(SerializationUtils.serialize(account));
	}

	@Override
	public Account decodeFromWire(int pos, Buffer buffer) {
		return (Account) SerializationUtils.deserialize(buffer.getBytes());
	}

	@Override
	public Account transform(Account account) {
		// If a message is sent *locally* across the event bus - sends message just as is
		return account;
	}

	@Override
	public String name() {
		// Each codec must have a unique name.
		// This is used to identify a codec when sending a message and for unregistering codecs.
		return getClass().getSimpleName();
	}

	@Override
	public byte systemCodecID() {
		// Always -1
		return -1;
	}

}
