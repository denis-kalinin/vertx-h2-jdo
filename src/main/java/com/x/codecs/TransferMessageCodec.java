package com.x.codecs;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import org.apache.commons.lang3.SerializationUtils;

import com.x.models.Transfer;

public class TransferMessageCodec implements MessageCodec<Transfer, Transfer>{

	@Override
	public void encodeToWire(Buffer buffer, Transfer transfer) {
		buffer.appendBytes(SerializationUtils.serialize(transfer));
	}

	@Override
	public Transfer decodeFromWire(int pos, Buffer buffer) {
		return (Transfer) SerializationUtils.deserialize(buffer.getBytes());
	}

	@Override
	public Transfer transform(Transfer transfer) {
		// If a message is sent *locally* across the event bus - sends message just as is
		return transfer;
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
