package org.corps.bi.recording.service.impl;

import java.nio.charset.Charset;

import org.corps.bi.recording.clients.rollfile.RollFileClient;
import org.corps.bi.recording.service.RecordingService;

public class RollFileRecordingServiceImpl implements RecordingService {
	
	private final RollFileClient rollFileClient;

	public RollFileRecordingServiceImpl() {
		super();
		this.rollFileClient=new RollFileClient();
	}

	@Override
	public boolean send(String category, String event, String body) {
		return this.rollFileClient.send(category, event, body);
	}

	@Override
	public boolean send(String category, String event, String body, Charset bodyCharset) {
		return this.rollFileClient.send(category, event, body,bodyCharset);
	}

	@Override
	public boolean send(String category, String event, byte[] body) {
		return this.rollFileClient.send(category, event, body);
	}

}
