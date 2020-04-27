package org.corps.bi.metrics;

public class Meta {
	
	private String metric;
	
	private String snId;
	
	private String gameId;
	
	private String ds;
	
	private String extra;

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public String getSnId() {
		return snId;
	}

	public void setSnId(String snId) {
		this.snId = snId;
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

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}
	
	public String getMetaId() {
		StringBuilder sb=new StringBuilder(this.metric);
		sb.append("_").append(this.snId);
		sb.append("_").append(this.gameId);
		sb.append("_").append(this.ds);
		return sb.toString();
	}

}
