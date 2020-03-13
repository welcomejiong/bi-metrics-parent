package org.corps.bi.recording;

import java.util.HashMap;
import java.util.Map;

public class GameHeader {
	
	private Map<String, String> header;
	
	private String category;
	
	private String event;
	
	private Long gameid;
	
	private String ds;
	
	private final String headerId;
	
	public GameHeader(String category,String event) {
		this.header=new HashMap<String, String>();
		header.put("category", category);
		header.put("event", event);
		this.init();
		this.headerId=this.category+"_"+this.gameid+"_"+this.ds;
	}

	public GameHeader(Map<String, String> header) {
		super();
		this.header=header;
		this.init();
		this.headerId=this.category+"_"+this.gameid+"_"+this.ds;
	}
	
	private void init(){
		String version=this.header.get("v");
		if(version==null||"".equals(version)){
			this.initDefaultVersion();
		}
		// other version
	}
	
	private void initDefaultVersion(){
		String category=this.header.get("category");
		String event=this.header.get("event");
		if(category==null || event==null){
			throw new RuntimeException(" event header category:"+category+" or  event:"+event+" is null");
		}
		this.category=category;
		this.event=event;
		String[] tmpArr=event.split("_");
		this.gameid=Long.parseLong(tmpArr[1]);
		this.ds=tmpArr[2];
	}

	public String getCategory() {
		return category;
	}

	public String getEvent() {
		return event;
	}

	public Long getGameid() {
		return gameid;
	}

	public String getDs() {
		return ds;
	}

	public String getHeaderId() {
		return headerId;
	}

	@Override
	public String toString() {
		return this.headerId;
	}
	
	
	
}
