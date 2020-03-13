package org.corps.bi.recording.service;

import java.nio.charset.Charset;

public interface RecordingService {
	
	public boolean send(String category,String event,String body);
	
	public boolean send(String category,String event,String body,Charset bodyCharset);
	
	public boolean send(String category,String event,byte[] body);

}
