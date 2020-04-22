package org.corps.bi.metrics.converter;

import java.util.HashMap;
import java.util.Map;

import org.corps.bi.metrics.AdTracking;
import org.corps.bi.metrics.Counter;
import org.corps.bi.metrics.CustomBinaryBodyMetric;
import org.corps.bi.metrics.Dau;
import org.corps.bi.metrics.Economy;
import org.corps.bi.metrics.GameInfo;
import org.corps.bi.metrics.IMetric;
import org.corps.bi.metrics.Install;
import org.corps.bi.metrics.Milestone;
import org.corps.bi.metrics.Payment;
import org.corps.bi.metrics.protobuf.AdTrackingProto;
import org.corps.bi.metrics.protobuf.CounterProto;
import org.corps.bi.metrics.protobuf.CustomBinaryBodyMetricProto;
import org.corps.bi.metrics.protobuf.DauProto;
import org.corps.bi.metrics.protobuf.EconomyProto;
import org.corps.bi.metrics.protobuf.GameInfoProto;
import org.corps.bi.metrics.protobuf.InstallProto;
import org.corps.bi.metrics.protobuf.MetaProto;
import org.corps.bi.metrics.protobuf.MilestoneProto;
import org.corps.bi.metrics.protobuf.PaymentProto;
import org.corps.bi.tools.util.JSONUtils;

import com.google.protobuf.GeneratedMessageV3;

public enum MetricEntityConverterManager {
	
	DAU("dau") {
		@Override
		public IMetric parseMetricEntityFromJson(String jsonData) {
			return JSONUtils.fromJSON(jsonData, Dau.class);
		}

		@Override
		public byte[] toProtobufBytes(IMetric imetric) {
			DauConverter converter=new DauConverter((Dau)imetric);
			return converter.toByteArray();
		}

		@Override
		public IMetric parseMetricEntityFromBytes(byte[] bytes) {
			DauConverter converter=new DauConverter(bytes);
			return converter.getEntity();
		}

		@Override
		public GeneratedMessageV3 parseProtoFromBytes(byte[] bytes) {
			try {
				return DauProto.parseFrom(bytes);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(),e);
			}
		}
		
	},
	
	INSTALL("install") {
		@Override
		public IMetric parseMetricEntityFromJson(String jsonData) {
			return JSONUtils.fromJSON(jsonData, Install.class);
		}

		@Override
		public byte[] toProtobufBytes(IMetric imetric) {
			InstallConverter converter=new InstallConverter((Install)imetric);
			return converter.toByteArray();
		}

		@Override
		public IMetric parseMetricEntityFromBytes(byte[] bytes) {
			InstallConverter converter=new InstallConverter(bytes);
			return converter.getEntity();
		}
		
		@Override
		public GeneratedMessageV3 parseProtoFromBytes(byte[] bytes) {
			try {
				return InstallProto.parseFrom(bytes);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(),e);
			}
		}
	},
	
	COUNTER("counter") {
		@Override
		public IMetric parseMetricEntityFromJson(String jsonData) {
			return JSONUtils.fromJSON(jsonData, Counter.class);
		}

		@Override
		public byte[] toProtobufBytes(IMetric imetric) {
			CounterConverter converter=new CounterConverter((Counter)imetric);
			return converter.toByteArray();
		}

		@Override
		public IMetric parseMetricEntityFromBytes(byte[] bytes) {
			CounterConverter converter=new CounterConverter(bytes);
			return converter.getEntity();
		}
		@Override
		public GeneratedMessageV3 parseProtoFromBytes(byte[] bytes) {
			try {
				return CounterProto.parseFrom(bytes);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(),e);
			}
		}
	},
	
	ECONOMY("economy") {
		@Override
		public IMetric parseMetricEntityFromJson(String jsonData) {
			return JSONUtils.fromJSON(jsonData, Economy.class);
		}

		@Override
		public byte[] toProtobufBytes(IMetric imetric) {
			EconomyConverter converter=new EconomyConverter((Economy)imetric);
			return converter.toByteArray();
		}

		@Override
		public IMetric parseMetricEntityFromBytes(byte[] bytes) {
			EconomyConverter converter=new EconomyConverter(bytes);
			return converter.getEntity();
		}
		@Override
		public GeneratedMessageV3 parseProtoFromBytes(byte[] bytes) {
			try {
				return EconomyProto.parseFrom(bytes);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(),e);
			}
		}
	},
	
	GAMEINFO("gameinfo") {
		@Override
		public IMetric parseMetricEntityFromJson(String jsonData) {
			return JSONUtils.fromJSON(jsonData, GameInfo.class);
		}

		@Override
		public byte[] toProtobufBytes(IMetric imetric) {
			GameInfoConverter converter=new GameInfoConverter((GameInfo)imetric);
			return converter.toByteArray();
		}

		@Override
		public IMetric parseMetricEntityFromBytes(byte[] bytes) {
			GameInfoConverter converter=new GameInfoConverter(bytes);
			return converter.getEntity();
		}
		@Override
		public GeneratedMessageV3 parseProtoFromBytes(byte[] bytes) {
			try {
				return GameInfoProto.parseFrom(bytes);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(),e);
			}
		}
	},
	
	MILESTONE("milestone") {
		@Override
		public IMetric parseMetricEntityFromJson(String jsonData) {
			return JSONUtils.fromJSON(jsonData, Milestone.class);
		}

		@Override
		public byte[] toProtobufBytes(IMetric imetric) {
			MilestoneConverter converter=new MilestoneConverter((Milestone)imetric);
			return converter.toByteArray();
		}

		@Override
		public IMetric parseMetricEntityFromBytes(byte[] bytes) {
			MilestoneConverter converter=new MilestoneConverter(bytes);
			return converter.getEntity();
		}
		@Override
		public GeneratedMessageV3 parseProtoFromBytes(byte[] bytes) {
			try {
				return MilestoneProto.parseFrom(bytes);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(),e);
			}
		}
	},
	
	PAYMENT("payment") {
		@Override
		public IMetric parseMetricEntityFromJson(String jsonData) {
			return JSONUtils.fromJSON(jsonData, Payment.class);
		}

		@Override
		public byte[] toProtobufBytes(IMetric imetric) {
			PaymentConverter converter=new PaymentConverter((Payment)imetric);
			return converter.toByteArray();
		}

		@Override
		public IMetric parseMetricEntityFromBytes(byte[] bytes) {
			PaymentConverter converter=new PaymentConverter(bytes);
			return converter.getEntity();
		}
		@Override
		public GeneratedMessageV3 parseProtoFromBytes(byte[] bytes) {
			try {
				return PaymentProto.parseFrom(bytes);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(),e);
			}
		}
	},
	
	ADTRACKING("adtracking") {
		@Override
		public IMetric parseMetricEntityFromJson(String jsonData) {
			return JSONUtils.fromJSON(jsonData, AdTracking.class);
		}

		@Override
		public byte[] toProtobufBytes(IMetric imetric) {
			AdTrackingConverter converter=new AdTrackingConverter((AdTracking)imetric);
			return converter.toByteArray();
		}

		@Override
		public IMetric parseMetricEntityFromBytes(byte[] bytes) {
			AdTrackingConverter converter=new AdTrackingConverter(bytes);
			return converter.getEntity();
		}
		@Override
		public GeneratedMessageV3 parseProtoFromBytes(byte[] bytes) {
			try {
				return AdTrackingProto.parseFrom(bytes);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(),e);
			}
		}
	},
	
	CUSTOMBINARYBODYMETRIC("custombinarybodymetric") {
		@Override
		public IMetric parseMetricEntityFromJson(String jsonData) {
			return JSONUtils.fromJSON(jsonData, CustomBinaryBodyMetric.class);
		}

		@Override
		public byte[] toProtobufBytes(IMetric imetric) {
			CustomBinaryBodyMetricConverter converter=new CustomBinaryBodyMetricConverter((CustomBinaryBodyMetric)imetric);
			return converter.toByteArray();
		}

		@Override
		public IMetric parseMetricEntityFromBytes(byte[] bytes) {
			CustomBinaryBodyMetricConverter converter=new CustomBinaryBodyMetricConverter(bytes);
			return converter.getEntity();
		}
		@Override
		public GeneratedMessageV3 parseProtoFromBytes(byte[] bytes) {
			try {
				return CustomBinaryBodyMetricProto.parseFrom(bytes);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(),e);
			}
		}
	};
	
	private static final Map<String,MetricEntityConverterManager> METRIC_CONVERT_MAP=new HashMap<String,MetricEntityConverterManager>();
	
	private final String metric;

	private MetricEntityConverterManager(String metric) {
		this.metric = metric;
	}
	
	public abstract IMetric parseMetricEntityFromJson(String jsonData);
	
	public abstract IMetric parseMetricEntityFromBytes(byte[] bytes);
	
	public abstract byte[] toProtobufBytes(IMetric imetric);
	
	public abstract GeneratedMessageV3 parseProtoFromBytes(byte[] bytes);
	
	public  byte[] toProtobufBytes(String jsonData) {
		IMetric iMetric=this.parseMetricEntityFromJson(jsonData);
		return this.toProtobufBytes(iMetric);
	}


	public String getMetric() {
		return metric;
	}
	
	
	public static MetricEntityConverterManager parseFromName(String metric) {
		if(METRIC_CONVERT_MAP.containsKey(metric)) {
			return METRIC_CONVERT_MAP.get(metric);
		}
		// 如果其中一个metric没在，说明是第一次调用，则全部重新加一遍
		for (MetricEntityConverterManager metricEntityConvert : MetricEntityConverterManager.values()) {
			METRIC_CONVERT_MAP.put(metricEntityConvert.metric, metricEntityConvert);
		}
		return METRIC_CONVERT_MAP.get(metric);
	}
	
	public static byte[] keyProtobufBytes(String metric,String snId,String gameId,String ds,String extra) {
		MetaProto.Builder builder=MetaProto.newBuilder();
		builder.setMetric(metric);
		builder.setSnId(snId);
		builder.setGameId(gameId);
		builder.setDs(ds);
		builder.setExtra(extra);
		return builder.build().toByteArray();
	}
	

}
