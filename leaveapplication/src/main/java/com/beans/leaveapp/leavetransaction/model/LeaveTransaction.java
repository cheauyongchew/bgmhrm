package com.beans.leaveapp.leavetransaction.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import com.beans.common.leave.rules.model.LeaveFlowDecisionsTaken;
import com.beans.common.leave.rules.model.LeaveRuleBean;
import com.beans.leaveapp.employee.model.Employee;
import com.beans.leaveapp.leavetype.model.LeaveType;

@Entity
@Table(name="LeaveTransaction")
public class LeaveTransaction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8702749544815399487L;
	private int id;
	private Date applicationDate;
	private Date  startDateTime;
	private Date  endDateTime;
	private Double yearlyLeaveBalance;
	private Double numberOfDays;
	private String reason;
	private LeaveType leaveType;
	private Employee employee;
	private Long taskId;
	private String status;
	private String rejectReason;
	private String timings;
	private String sickLeaveAttachmentName;
	private LeaveRuleBean leaveRuleBean;
	private LeaveFlowDecisionsTaken decisionsBean;
	private String decisionToBeTaken;
	
	@Lob
	private byte[] sickLeaveAttachment;
	private String createdBy;
	private Date creationTime;
	private String lastModifiedBy;
	private Date lastModifiedTime;
	private boolean isDelete;
	
	public LeaveTransaction() {
		
	}
	
	
	public LeaveTransaction(int id, Date applicationDate, Date startDateTime,
			Date endDateTime, Double yearlyLeaveBalance, Double numberOfDays,
			String reason, LeaveType leaveType, Employee employee,
			String status, String timings, LeaveRuleBean leaveRuleBean,
			LeaveFlowDecisionsTaken decisionsBean, boolean isDelete) {
		super();
		this.id = id;
		this.applicationDate = applicationDate;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		this.yearlyLeaveBalance = yearlyLeaveBalance;
		this.numberOfDays = numberOfDays;
		this.reason = reason;
		this.leaveType = leaveType;
		this.employee = employee;
		this.status = status;
		this.timings = timings;
		this.leaveRuleBean = leaveRuleBean;
		this.decisionsBean = decisionsBean;
		this.isDelete = isDelete;
	}


	@Id
	@GeneratedValue
	@Column( name="id",nullable=false,unique=true)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@Column(name="applicationDate",nullable=false)
	public Date getApplicationDate() {
		return applicationDate;
	}
	public void setApplicationDate(Date applicationDate) {
		
		this.applicationDate = applicationDate;
	}
	
	@Column(name="startDateTime",nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getStartDateTime() {
		return startDateTime;
	}
	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}
	
	@Column(name="endDateTime",nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getEndDateTime() {
		return endDateTime;
	}
	
	public String fetchStartTimeStr(){
		if(startDateTime!=null){
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			return sdf.format(startDateTime); 
		}
		return startDateTime.toString();
	}
	public String fetchEndTimeStr(){
		if(endDateTime!=null){
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			return sdf.format(endDateTime); 
		}
		return endDateTime.toString();
	}
	
	
	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}
	
	@Column(name="yearlyLeaveBalance",nullable=true)
	public Double getYearlyLeaveBalance() {
		return yearlyLeaveBalance;
	}
	public void setYearlyLeaveBalance(Double yearlyLeaveBalance) {
		this.yearlyLeaveBalance = yearlyLeaveBalance;
	}
	
	
	@Column(name="numberOfDays",nullable=true)
	public Double getNumberOfDays() {
		return numberOfDays;
	}
	
	public void setNumberOfDays(Double numberOfDays) {
		this.numberOfDays = numberOfDays;
	}
	
	@Column(name="reason",nullable=false)
	public String getReason() {
		return reason;
	} 
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	@Column(name="isdeleted", columnDefinition="TINYINT(1)") 
	@Type(type="org.hibernate.type.NumericBooleanType")
	public boolean isDeleted() {
		return isDelete;
	}
	public void setDeleted(boolean isDeleted) {
		this.isDelete = isDeleted;
	}
	

	@OneToOne
	@JoinColumn(name="leaveTypeId",nullable=false)
	public LeaveType getLeaveType(){
		return leaveType;
	}
	public void setLeaveType(LeaveType leaveType) {
		this.leaveType = leaveType;
	}
	
    @OneToOne
	@JoinColumn(name="employeeId")
	public Employee getEmployee() {
		return employee;
	}
	public void setEmployee(Employee employee) {
		this.employee = employee;
	}
	
	@Column(name="taskId", nullable=true)
	public Long getTaskId() {
		return taskId;
	}
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public void setLastModifiedTime(Date lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
			
	@Column(name="creationTime",nullable=true)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreationTime() {
		return creationTime;
	}
	@Column(name="createdBy",nullable=true)
	public String getCreatedBy() {
		return createdBy;
	}
	@Column(name="lastModifiedTime",nullable=true)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastModifiedTime() {
		return lastModifiedTime;
	}
	@Column(name="lastModifiedBy",nullable=true)
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	
	@Column(name="status",nullable=true)
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	@Column(name="rejectReason",nullable=true)
	public String getRejectReason() {
		return rejectReason;
	}
	public void setRejectReason(String rejectReason) {
		this.rejectReason = rejectReason;
	}
	
	@Column(name="sickLeaveAttachment",nullable=true)
	public byte[] getSickLeaveAttachment() {
		return sickLeaveAttachment;
	}
	public void setSickLeaveAttachment(byte[] sickLeaveAttachment) {
		this.sickLeaveAttachment = sickLeaveAttachment;
	}
	
	@Column(name="decisionToBeTaken",nullable=true)
	public String getDecisionToBeTaken() {
		return decisionToBeTaken;
	}


	public void setDecisionToBeTaken(String decisionToBeTaken) {
		this.decisionToBeTaken = decisionToBeTaken;
	}


	@Column(name="timings",nullable=true)
	public String getTimings() {
		return timings;
	}
	public void setTimings(String timings) {
		this.timings = timings;

	}
	
	@Column(name="sickLeaveAttachmentName",nullable=true)
	public String getSickLeaveAttachmentName() {
		return sickLeaveAttachmentName;
	}
	public void setSickLeaveAttachmentName(String sickLeaveAttachmentName) {
		this.sickLeaveAttachmentName = sickLeaveAttachmentName;
	}
	
	@OneToOne
	@JoinColumn(name="leaveRuleBeanId")
	public LeaveRuleBean getLeaveRuleBean() {
		return leaveRuleBean;
	}

	public void setLeaveRuleBean(LeaveRuleBean leaveRuleBean) {
		this.leaveRuleBean = leaveRuleBean;
	}

	@OneToOne
	@JoinColumn(name="decisionBeanId")
	public LeaveFlowDecisionsTaken getDecisionsBean() {
		return decisionsBean;
	}

	public void setDecisionsBean(LeaveFlowDecisionsTaken decisionsBean) {
		this.decisionsBean = decisionsBean;
	}


	@Override
	public String toString() {
		return "LeaveTransaction [id=" + id + ", status=" + status
				+ ", leaveRuleBean=" + leaveRuleBean + ", decisionsBean="
				+ decisionsBean + ", decisionToBeTaken=" + decisionToBeTaken
				+ "]";
	}	
}
