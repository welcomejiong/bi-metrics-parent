package org.corps.bi.transport;

import java.util.List;

import org.corps.bi.core.MetricRequestParams;

public interface MetricsTransporter {
	
	public boolean transport(MetricRequestParams metricRequestParams);
	
	public boolean transport(List<MetricRequestParams> metricRequestParams);
	
}
