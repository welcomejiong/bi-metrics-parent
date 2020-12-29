package org.corps.bi.core;

public class MetricRequestParams {
	
	private	String metric;
	
	private String gameId;
	
	private String ds;
	
	private	String jsonData;
	
	private	int retry;

	public MetricRequestParams() {
		super();
	}

	public MetricRequestParams(String metric, String gameId, String ds, String jsonData) {
		super();
		this.metric = metric;
		this.gameId = gameId;
		this.ds = ds;
		this.jsonData = jsonData;
	}


	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}
	

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getDs() {
		return ds;
	}

	public void setDs(String ds) {
		this.ds = ds;
	}

	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}
	
	public void incrRetry(){
		this.retry+=1;
	}

	
	
}
