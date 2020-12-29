package org.corps.bi.transport.persist;

import java.util.List;

import org.corps.bi.core.MetricRequestParams;

public interface Persister {
	
	public boolean persist(List<MetricRequestParams> metricRequestParamsList);
	
	public boolean persist(MetricRequestParams metricRequestParams);
	

}
