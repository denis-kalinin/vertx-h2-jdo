package com.x.codecs;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import org.apache.commons.lang3.SerializationUtils;

import com.x.models.Balance;

public class BalanceMessageCodec implements MessageCodec<Balance, Balance>{

	@Override
	public void encodeToWire(Buffer buffer, Balance balance) {
		buffer.appendBytes(SerializationUtils.serialize(balance));
	}

	@Override
	public Balance decodeFromWire(int pos, Buffer buffer) {
		return (Balance) SerializationUtils.deserialize(buffer.getBytes());
	}

	@Override
	public Balance transform(Balance balance) {
		// If a message is sent *locally* across the event bus - sends message just as is
		return balance;
	}

	@Override
	public String name() {
		return getClass().getSimpleName();
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}

}

