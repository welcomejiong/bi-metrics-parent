package org.corps.bi.recording.clients.rollfile;

import java.nio.charset.Charset;

public class RollFileEvent {

	private final String category;
	
	private final String event;
	
	private final byte[] body;
	
	public RollFileEvent(String category, String event, String body,Charset bodyCharset) {
		super();
		this.category = category;
		this.event = event;
		if(body!=null){
			this.body=body.getBytes(bodyCharset);
		}else{
			this.body=null;
		}
	}
	
	public RollFileEvent(String category, String event, byte[] body) {
		super();
		this.category = category;
		this.event = event;
		this.body = body;
	}

	public String getCategory() {
		return category;
	}

	public String getEvent() {
		return event;
	}

	public byte[] getBody() {
		return body;
	}
	
}
