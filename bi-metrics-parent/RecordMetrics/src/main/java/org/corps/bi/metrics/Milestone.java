package org.corps.bi.metrics;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.utils.DateUtils;

/**
 * 
 * 区别于Counter，Milestone记录具有里程碑意义的用户行为，例如升级、完成一些重要任务等等。
 * 
 * <br/>
 *  手游扩展分类extra: <br/>
 *  页游："actorid:角色id"<br/>
 *  手游：升级："actorid:角色id,exp:升级时角色经验值"；其他："actorid:角色id"
 * 
 */
public class Milestone extends AbstractMetric implements IMetric{

	/**
	 * 里程碑的信息
	 */
	private String milestone;

	/**
	 * 里程碑的相对应的值
	 */
	private String value;

	/**
	 * 记录的日期 yyyy-MM-dd
	 */
	private String milestoneDate;

	/**
	 * 记录的时间 HH:mm:ss
	 */
	private String milestoneTime;
	
	public Milestone() {
		super();
	}
	
	/**
	 * 获取完成日期，日期格式(yyyy-MM-dd)
	 * @return
	 */
	public String getMilestoneDate() {
		return milestoneDate;
	}

	/**
	 * 设置完成日期，日期格式(yyyy-MM-dd)
	 * @param milestoneDate
	 */
	public void setMilestoneDate(String milestoneDate) {
		this.milestoneDate = milestoneDate;
	}

	/**
	 * 获取完成时间，时间格式(HH:mm:ss)
	 * @return
	 */
	public String getMilestoneTime() {
		return milestoneTime;
	}

	/**
	 * 设置完成时间，时间格式(HH:mm:ss)
	 * @param milestoneTime
	 */
	public void setMilestoneTime(String milestoneTime) {
		this.milestoneTime = milestoneTime;
	}

	/**
	 * 获取里程碑名称，示例：<br/>
	 * 1 升级时报送：level_up <br/>
	 * 2  获得某种预期（成就、称号等）时：achieve<br/>
	 * @return
	 */
	public String getMilestone() {
		return milestone;
	}

	/**
	 * 设置里程碑名称，示例：<br/>
	 * 1 升级时报送：level_up <br/>
	 * 2  获得某种预期（成就、称号等）时：achieve<br/>
	 * @param milestone
	 */
	public void setMilestone(String milestone) {
		this.milestone = milestone;
	}

	/**
	 * 获取里程碑的相对应的值
	 * 1 升级时报送：升到的级别 <br/>
	 * 2  获得某种预期（成就、称号等）时：成就或者称号的中文名称<br/>
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 设置里程碑的相对应的值
	 * 1 升级时报送：升到的级别 <br/>
	 * 2  获得某种预期（成就、称号等）时：成就或者称号的中文名称<br/>
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String prepareForDB() {
		StringBuffer buf = new StringBuffer();
		buf.append(this.getUserId());
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.getClientId());
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.milestone);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.value);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.milestoneDate);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.milestoneTime);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(super.getExtra());
		return buf.toString();
	}

	@Override
	public boolean checkBaseParmIsNull() {
		if(StringUtils.isEmpty(this.getUserId()) || StringUtils.isEmpty(milestone)){
			return true;
		}
		return false;
	}

	@Override
	public void processDate() {
		this.milestoneDate = (DateUtils.verifyDate(milestoneDate));
		this.milestoneTime = (DateUtils.verifyTime(milestoneTime));
	}

	@Override
	public String metric() {
		return "milestone";
	}
}
