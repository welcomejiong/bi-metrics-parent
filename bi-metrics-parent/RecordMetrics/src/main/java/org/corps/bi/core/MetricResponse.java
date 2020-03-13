package org.corps.bi.core;

public class MetricResponse {
	
	private String status;
	
	private String msg;

	public MetricResponse() {
		super();
	}

	public MetricResponse(String status, String msg) {
		super();
		this.status = status;
		this.msg = msg;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public boolean isSucc(){
		return "succ".equals(this.status);
	}
	
	

}
