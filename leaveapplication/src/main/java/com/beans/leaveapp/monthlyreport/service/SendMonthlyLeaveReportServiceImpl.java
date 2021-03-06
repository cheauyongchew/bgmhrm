package com.beans.leaveapp.monthlyreport.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.stereotype.Service;

import com.beans.leaveapp.employee.model.Employee;
import com.beans.leaveapp.employee.repository.EmployeeRepository;
import com.beans.leaveapp.leavetransaction.model.LeaveTransaction;
import com.beans.leaveapp.leavetransaction.repository.LeaveTransactionRepository;
import com.beans.leaveapp.leavetype.model.LeaveType;
import com.beans.leaveapp.leavetype.repository.LeaveTypeRepository;
import com.beans.leaveapp.monthlyreport.model.AnnualLeaveReport;
import com.beans.leaveapp.monthlyreport.model.LeaveDeductHistory;
import com.beans.leaveapp.monthlyreport.model.MonthlyLeaveReport;
import com.beans.leaveapp.monthlyreport.repository.AnnualLeaveReportRepository;
import com.beans.leaveapp.monthlyreport.repository.LeaveDeductHistoryRepository;
import com.beans.leaveapp.monthlyreport.repository.MonthlyLeaveReportRepository;
import com.beans.leaveapp.yearlyentitlement.model.YearlyEntitlement;
import com.beans.leaveapp.yearlyentitlement.repository.YearlyEntitlementRepository;
import com.beans.util.email.EmailSender;
import com.beans.util.enums.EmployeeTypes;
import com.beans.util.enums.Leave;
import com.beans.util.log.ApplLogger;
@Service
public class SendMonthlyLeaveReportServiceImpl implements SendMonthlyLeaveReportService{

	@Resource
	EmployeeRepository employeeRepository;
	
	@Resource
	YearlyEntitlementRepository entitlementRepository;
	
	@Resource
	LeaveTransactionRepository leaveRepository;
	
	@Resource
	AnnualLeaveReportRepository annualLeaveRepository;
	
	@Resource
	MonthlyLeaveReportRepository monthlyLeaveRepository;
	
	@Resource
	LeaveTypeRepository leaveTypeRepository;
	
	@Resource
	LeaveDeductHistoryRepository leaveDeductRepository;
	
	@Override
	public void sendMonthlyLeaveReportToEmployees() {
		try{
		// Selecting all employee who are PERM , CONT and INT which excludes roles with ROLE_ADMIN and ROLE_OPERDIR
		List<Employee> employeeList = employeeRepository.findAllEmployeesForSendingMonthlyLeaveReport();
		if(employeeList!=null && employeeList.size()>0){
			
			for (Employee employee : employeeList) {
				
				if(EmployeeTypes.PERMANENT.equalsName(employeeRepository.findByEmployeeId(employee.getId()))){
					/* Send monthly leave report to Permanent Employees only*/
					
					StringBuffer monthlyLeaveReportBuffer = new StringBuffer();
					
					// finding number of leaves to be deducted in Feb month only for Annual Leave Report
					if(Calendar.getInstance().get(Calendar.MONTH)==1)
						calculateAndUpdateLeavesDeduction(employee);
					
					// Now time for Annual Leaves
					monthlyLeaveReportBuffer = getAnnualLeaveYearlyReport(employee);
					
					// Get leave deduction message only in feb month
					if(Calendar.getInstance().get(Calendar.MONTH)==1)
					monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getLeaveDeductionMessage(employee));
						
					// Now time for Marriage Leaves
					monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getSickLeaveYearlyReport(employee));
					
					// Now time for Marriage Leaves
					monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getMarriageLeaveYearlyReport(employee));
					
					// Now time for Compassionate Leaves
					monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getCompassionateLeaveYearlyReport(employee));
					
					if("M".equalsIgnoreCase(employee.getGender())){
						// Now time for Paternity Leaves
					monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getPaternityLeaveYearlyReport(employee));
					}
					else {
						// Now time for Maternity Leaves
						monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getMaternityLeaveYearlyReport(employee));
					}
					
					// Now time for Unpaid Leaves
					monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getUnpaidLeaveYearlyReport(employee));
					
					// Now time for Applied and Approved Leaves 
					monthlyLeaveReportBuffer= monthlyLeaveReportBuffer.append(getAllAppliedAndApprovedLeavesList(employee));
					
					// Now time for Time-in-Lieu leaves
					monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getAllAppliedAndApprovedTimeInLieuList(employee));
					
					// Send Remainder of carry forward leaves in January month only
					if(Calendar.getInstance().get(Calendar.MONTH)==0){
						monthlyLeaveReportBuffer= monthlyLeaveReportBuffer.append(getRemainderMessageInJanuary());
					}else if(Calendar.getInstance().get(Calendar.MONTH)==11)
						monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getRemainderMessageInDecember());
				
					
					sendEmailMontlyLeaveReportToEmployee(employee,monthlyLeaveReportBuffer.toString());
					
					}
				else if(EmployeeTypes.CONTRACT.equalsName(employeeRepository.findByEmployeeId(employee.getId()))){
					/* Send monthly leave report to Contract Employees only*/
					
					StringBuffer monthlyLeaveReportBuffer = new StringBuffer();
					
					// finding number of leaves to be deducted in Feb month only for Annual Leave Report
					if(Calendar.getInstance().get(Calendar.MONTH)==1)
						calculateAndUpdateLeavesDeduction(employee);
					
					// Now time for Annual Leaves
					monthlyLeaveReportBuffer = getAnnualLeaveYearlyReport(employee);
					
					// Get leave deduction message only in feb month
					if(Calendar.getInstance().get(Calendar.MONTH)==1)
					monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getLeaveDeductionMessage(employee));
					
					// Now time for Unpaid Leaves
					monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getUnpaidLeaveYearlyReport(employee));
				
					// Now time for Applied and Approved Leaves 
					monthlyLeaveReportBuffer= monthlyLeaveReportBuffer.append(getAllAppliedAndApprovedLeavesList(employee));
					
					// Now time for Time-in-Lieu leaves
					monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getAllAppliedAndApprovedTimeInLieuList(employee));
				
					// Send Email to Monthly report of Contract Employee
					sendEmailMontlyLeaveReportToEmployee(employee,monthlyLeaveReportBuffer.toString());
				}
				else{
					/* Send monthly leave report to Internship Employees only*/
					StringBuffer monthlyLeaveReportBuffer = new StringBuffer();
					
					monthlyLeaveReportBuffer = getTimeInLieuYearlyReportForInternship(employee);
					
					// Now time for Unpaid Leaves
					monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getUnpaidLeaveYearlyReport(employee));
				
					// Now time for Applied and Approved Leaves 
					monthlyLeaveReportBuffer= monthlyLeaveReportBuffer.append(getAllAppliedAndApprovedLeavesList(employee));
					
					// Now time for Time-in-Lieu leaves
					monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getAllAppliedAndApprovedTimeInLieuList(employee));
					
					// Send Email to Monthly report of Contract Employee
					sendEmailMontlyLeaveReportToEmployee(employee,monthlyLeaveReportBuffer.toString());
				}
				
				
				
			//	String[] resultData = getMonthlyUnpaidLeaveDataAndTotalDays(employee);
				
			//	sendEmailMontlyLeaveReportToEmployee(employee, getMonthlyLeaveEntitlementData(employee),resultData[0],resultData[1]);
			}
		}
		}catch(Exception e){
			System.out.println("Error while sending mails to employees of monthly leave report");
			e.printStackTrace();
		}
	}
	
	private void sendEmailMontlyLeaveReportToEmployee(Employee employee,String entitlementData){
	
	try {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("EmployeeMonthlyReport.html");
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		// read file into string
		String line = null;

		StringBuilder responseData = new StringBuilder();
		try {
			while ((line = bufferedReader.readLine()) != null) {
				responseData.append(line);
			}
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		// load your HTML email template
		String htmlEmailTemplate = responseData.toString();
		
		// create the email message
		HtmlEmail email = new HtmlEmail();

		// replacing the employee details in HTML
		htmlEmailTemplate = htmlEmailTemplate.replace("##employeeName##",employee.getName());
		htmlEmailTemplate = htmlEmailTemplate.replace("##month##", getPresentMonth( Calendar.getInstance().get(Calendar.MONTH)));
		htmlEmailTemplate = htmlEmailTemplate.replace("##reportDataTable##",entitlementData);
		// add reciepiant email address
		try {
			if(employee.getWorkEmailAddress()!=null && !employee.getWorkEmailAddress().equals(""))
			email.addTo(employee.getWorkEmailAddress(), employee.getName());
			email.setSubject("Reg : Monthly Leave Report of "+employee.getName());
		} catch (EmailException e) {
			e.printStackTrace();
		}
		// set the html message
		try {
			email.setHtmlMsg(htmlEmailTemplate);
		} catch (EmailException e2) {
			e2.printStackTrace();
		}

		// send the email
			EmailSender.sendEmail(email);
			System.out.println("Monthly Leave Report Email has been sent successfully to Employee "+employee.getName());
		
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Error while sending email to Employee "+employee.getName());
		}
	}
	
	@Override
	public void sendMonthlyLeaveReportToHR() {
		try{
			// Selecting all employee with role as ROLE_HR for sending all employees monthly leave report.
			List<Employee> hrList = employeeRepository.getAllUsersWithRole("ROLE_HR");
			if(hrList!=null && hrList.size()>0){
				for (Employee hrEmployee : hrList) {
					try{
						// Selecting all employee who are PERM , CONT and INT which excludes roles with ROLE_ADMIN and ROLE_OPERDIR
						List<Employee> employeeList = employeeRepository.findAllEmployeesForSendingMonthlyLeaveReport();
						if(employeeList!=null && employeeList.size()>0){
							
							StringBuffer monthlyLeaveReportBuffer = new StringBuffer();
							for (Employee employee : employeeList) {
								if(EmployeeTypes.INTERNSHIP.equalsName(employeeRepository.findByEmployeeId(employee.getId()))){
									/* Prepare monthly leave report of Internship Employees only*/
									monthlyLeaveReportBuffer.append("<table border='0px' width='800px'><tr><td align='center' style='color:blue;font-size:1.2em;'><u><b>"+employee.getName()+"</b></u></td></tr></table><br/><br/>");
									
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getTimeInLieuYearlyReportForInternship(employee));
									
									// Now time for Unpaid Leaves
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getUnpaidLeaveYearlyReport(employee));
								
									// Now time for Applied and Approved Leaves 
									monthlyLeaveReportBuffer= monthlyLeaveReportBuffer.append(getAllAppliedAndApprovedLeavesList(employee));
									
									// Now time for Time-in-Lieu leaves
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getAllAppliedAndApprovedTimeInLieuList(employee));
									
								}
						
								else if(EmployeeTypes.CONTRACT.equalsName(employeeRepository.findByEmployeeId(employee.getId()))){
									/* Prepare monthly leave report of Contract Employees only*/
									monthlyLeaveReportBuffer.append("<table border='0px' width='800px'><tr><td align='center' style='color:blue;font-size:1.2em;'><u><b>"+employee.getName()+"</b></u></td></tr></table><br/><br/>");
									
									// Now time for Annual Leaves
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getAnnualLeaveYearlyReport(employee));
									
									// Get leave deduction message only in feb month
									if(Calendar.getInstance().get(Calendar.MONTH)==1)
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getLeaveDeductionMessage(employee));
									
									// Now time for Unpaid Leaves
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getUnpaidLeaveYearlyReport(employee));
								
									// Now time for Applied and Approved Leaves 
									monthlyLeaveReportBuffer= monthlyLeaveReportBuffer.append(getAllAppliedAndApprovedLeavesList(employee));
									
									// Now time for Time-in-Lieu leaves
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getAllAppliedAndApprovedTimeInLieuList(employee));
								
								}
								else if(EmployeeTypes.PERMANENT.equalsName(employeeRepository.findByEmployeeId(employee.getId()))){
									/* Send monthly leave report to Permanent Employees only*/
									monthlyLeaveReportBuffer.append("<table border='0px' width='800px'><tr><td align='center' style='color:blue;font-size:1.2em;'><u><b>"+employee.getName()+"</b></u></td></tr></table><br/>");
									
									// Now time for Annual Leaves
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getAnnualLeaveYearlyReport(employee));
									
									// Get leave deduction message only in feb month
									if(Calendar.getInstance().get(Calendar.MONTH)==1)
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getLeaveDeductionMessage(employee));
									
									// Now time for Marriage Leaves
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getSickLeaveYearlyReport(employee));
									
									// Now time for Marriage Leaves
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getMarriageLeaveYearlyReport(employee));
									
									// Now time for Compassionate Leaves
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getCompassionateLeaveYearlyReport(employee));
									
									if("M".equalsIgnoreCase(employee.getGender())){
										// Now time for Paternity Leaves
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getPaternityLeaveYearlyReport(employee));
									}
									else {
										// Now time for Maternity Leaves
										monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getMaternityLeaveYearlyReport(employee));
									}
									
									// Now time for Unpaid Leaves
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getUnpaidLeaveYearlyReport(employee));
									
									// Now time for Applied and Approved Leaves 
									monthlyLeaveReportBuffer= monthlyLeaveReportBuffer.append(getAllAppliedAndApprovedLeavesList(employee));
									
									// Now time for Time-in-Lieu leaves
									monthlyLeaveReportBuffer = monthlyLeaveReportBuffer.append(getAllAppliedAndApprovedTimeInLieuList(employee));
								
									}
							}
							// Send mail to HR Employee with all the employees information in a single mail
							sendEmailMontlyLeaveReportToHR(hrEmployee,monthlyLeaveReportBuffer.toString());
							
						}
						}catch(Exception e){
							System.out.println("Error while preparing monthly leave report of Employee : ");
							e.printStackTrace();
						}
				}
			}
			}catch(Exception e){
				System.out.println("Error while sending monthly leave report mail of all employees to HR ");
				e.printStackTrace();
			}
	}
	
	private void sendEmailMontlyLeaveReportToHR(Employee hrEmployee,String entireReportData){
		
	try {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("HumanResourceMonthlyReport.html");
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		// read file into string
		String line = null;

		StringBuilder responseData = new StringBuilder();
		try {
			while ((line = bufferedReader.readLine()) != null) {
				responseData.append(line);
			}
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		// load your HTML email template
		String htmlEmailTemplate = responseData.toString();
		
		// create the email message
		HtmlEmail email = new HtmlEmail();

		// replacing the employee details in HTML
		htmlEmailTemplate = htmlEmailTemplate.replace("##fullName##",hrEmployee.getName());
		htmlEmailTemplate = htmlEmailTemplate.replace("##month##", getPresentMonth( Calendar.getInstance().get(Calendar.MONTH)));
		htmlEmailTemplate = htmlEmailTemplate.replace("##reportDataTable##",entireReportData);
		// add reciepiant email address
		try {
			if(hrEmployee.getWorkEmailAddress()!=null && !hrEmployee.getWorkEmailAddress().equals(""))
			email.addTo(hrEmployee.getWorkEmailAddress(), hrEmployee.getName());
			email.setSubject("Reg : Leave Report for "+getPresentMonth( Calendar.getInstance().get(Calendar.MONTH)));
		} catch (EmailException e) {
			e.printStackTrace();
		}
		// set the html message
		try {
			email.setHtmlMsg(htmlEmailTemplate);
		} catch (EmailException e2) {
			e2.printStackTrace();
		}

		// send the email
			EmailSender.sendEmail(email);
			System.out.println("Monthly Leave Report Email has been sent successfully to HR Employee "+hrEmployee.getName());
		
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Error while sending email to HR Employee "+hrEmployee.getName());
		}
	}
	
	
	
	
	
	private String getPresentMonth(int month){
		switch(month){
		case 0 : return "January";
		case 1 : return "February";
		case 2 : return "March";
		case 3 : return "April";
		case 4 : return "May";
		case 5 : return "June";
		case 6 : return "July";
		case 7 : return "August";
		case 8 : return "September";
		case 9 : return "October";
		case 10 : return "November";
		case 11 : return "December";
		
		}
		return "Current Month";
	}
	
	private StringBuffer getAnnualLeaveYearlyReport(Employee employee){
		StringBuffer leaveReportBuffer = new StringBuffer();
		try{
		// Firstly prepare Annual Leaves
		List<YearlyEntitlement>	 annualEntitlementList = entitlementRepository.findByEmployeeIdAndEmployeeTypeAndLeaveTypeName(employee.getId(),Leave.ANNUAL.toString());
		if(annualEntitlementList!=null && annualEntitlementList.size()>0){
			YearlyEntitlement	annualYearlyEntitlementBean = annualEntitlementList.get(0);
			// preparation of Table headers with styles
			leaveReportBuffer.append("<table border='0px' width='800px'><tr><td align='left' style='color:darkorange;font-size:1.2em;'><b>Annual Leave </b></td><td align='right'><b> Entitlement </b> = "+annualYearlyEntitlementBean.getEntitlement()+" days</td></tr></table>");
			leaveReportBuffer.append("<table border='1cm' width='800px' cellpadding='1px' cellspacing='0px' class='bolder'><tr><td align='center'>Month</td><td align='center'>Balance Brought Forward</td><td align='center'>Leave Auto-Credited</td><td align='center'>Annual Leave Taken</td><td align='center'>Time-In-Lieu Credited</td><td align='center'>Current Leave Balance</td><td align='center'>Yearly Leave Balance</td></tr>");
			// prepartation of Table data of Annual Leaves of employee
			List<AnnualLeaveReport>	 annualLeaveList = annualLeaveRepository.getAnnualLeaveDataOfEmployee(employee.getId(), Calendar.getInstance().get(Calendar.YEAR));
			if(annualLeaveList!=null && annualLeaveList.size()>0){
				
				for (AnnualLeaveReport annualLeaveReport : annualLeaveList) {
					leaveReportBuffer.append("<tr><td align='right'>"+(annualLeaveReport.getMonthOfYear()!=null?annualLeaveReport.getMonthOfYear():"-"));
					leaveReportBuffer.append("</td><td align='center'>"+(annualLeaveReport.getBalanceBroughtForward()!=null?annualLeaveReport.getBalanceBroughtForward():"-"));
					leaveReportBuffer.append("</td><td align='center'>"+(annualLeaveReport.getLeavesCredited()!=null?annualLeaveReport.getLeavesCredited():"-"));
					leaveReportBuffer.append("</td><td align='center'>"+(annualLeaveReport.getLeavesTaken()!=null?annualLeaveReport.getLeavesTaken():"-"));
					leaveReportBuffer.append("</td><td align='center'>"+(annualLeaveReport.getTimeInLieuCredited()!=null?annualLeaveReport.getTimeInLieuCredited():"-"));
					leaveReportBuffer.append("</td><td align='center'>"+(annualLeaveReport.getCurrentLeaveBalance()!=null?annualLeaveReport.getCurrentLeaveBalance():"-"));
					leaveReportBuffer.append("</td><td align='center'>"+(annualLeaveReport.getYearlyLeaveBalance()!=null?annualLeaveReport.getYearlyLeaveBalance():"-"+"</td></tr>"));
				}
				
			}
			leaveReportBuffer.append("</table><br/>");
		}
		}catch(Exception e){
			e.printStackTrace();
			leaveReportBuffer = new StringBuffer("<table border='0px' width='800px'><tr><td align='left' style='color:red;font-size:1.2em;'><b>Error while generationg monthly report on Annual Leave </b></td></tr></table><br/>");
		}
		return leaveReportBuffer;
	}
	
	private StringBuffer getSickLeaveYearlyReport(Employee employee){
		StringBuffer leaveReportBuffer = new StringBuffer();
		try{
		// Now time for Sick Leaves
			System.out.println("Result count is :");
		List<YearlyEntitlement>  sickEntitlementList =	entitlementRepository.findByEmployeeIdAndEmployeeTypeAndLeaveTypeName(employee.getId(), Leave.SICK.toString());
		if(sickEntitlementList!=null && sickEntitlementList.size()>0){
			YearlyEntitlement	sickYearlyEntitlementBean = sickEntitlementList.get(0);
			// preparation of Table headers with styles
			leaveReportBuffer.append("<table border='0px' width='800px' class='bolder'><tr><td align='left' style='color:darkorange;font-size:1.2em;'><b>Sick Leave </b></td><td align='right'><b> Entitlement </b> = "+sickYearlyEntitlementBean.getEntitlement()+" days</td></tr></table>");
			leaveReportBuffer.append("<table border='1cm' width='800px' cellpadding='1px' cellspacing='0px' class='bolder'><tr><td align='center'>Month</td><td align='center'>Sick Leave Taken</td><td align='center'>Yearly Leave Balance</td></tr>");
			// prepartation of Table data of Sick Leaves of employee
			List<MonthlyLeaveReport>  sickLeaveReportList =	 monthlyLeaveRepository.findAllLeaveReportsOfYearByIdAndLeaveId(employee.getId(), sickYearlyEntitlementBean.getLeaveType().getId(), Calendar.getInstance().get(Calendar.YEAR));
				if(sickLeaveReportList!=null && sickLeaveReportList.size()>0){
					for (MonthlyLeaveReport monthlyLeaveReport : sickLeaveReportList) {
						leaveReportBuffer.append("<tr><td align='right'>"+(monthlyLeaveReport.getMonthOfYear()!=null?monthlyLeaveReport.getMonthOfYear():"-"));
						leaveReportBuffer.append("</td><td align='center'>"+(monthlyLeaveReport.getLeavesTaken()!=null?monthlyLeaveReport.getLeavesTaken():"-"));
						leaveReportBuffer.append("</td><td align='center'>"+(monthlyLeaveReport.getYearlyLeaveBalance()!=null?monthlyLeaveReport.getYearlyLeaveBalance():"-"+"</td></tr>"));
					}
					
				}
				leaveReportBuffer.append("</table><br/>");
		}
		}catch(Exception e){
			e.printStackTrace();
			leaveReportBuffer = new StringBuffer("<table border='0px' width='800px'><tr><td align='left' style='color:red;font-size:1.2em;'><b>Error while generationg monthly report on Sick Leave </b></td></tr></table><br/>");
		}
		return leaveReportBuffer;
	}
	
	private StringBuffer getMarriageLeaveYearlyReport(Employee employee){
		StringBuffer leaveReportBuffer = new StringBuffer();
		try{
		// Now time for Marriage Leaves
		List<YearlyEntitlement>  marriageEntitlementList =	entitlementRepository.findByEmployeeIdAndEmployeeTypeAndLeaveTypeName(employee.getId(), Leave.MARRIAGE.toString());
		if(marriageEntitlementList!=null && marriageEntitlementList.size()>0){
			YearlyEntitlement	marriageYearlyEntitlementBean = marriageEntitlementList.get(0);
			// preparation of Table headers with styles
			leaveReportBuffer.append("<table border='0px' width='800px' class='bolder'><tr><td align='left' style='color:darkorange;font-size:1.2em;'><b>Marriage Leave </b></td><td align='right'><b> Entitlement </b> = "+marriageYearlyEntitlementBean.getEntitlement()+" days</td></tr></table>");
			leaveReportBuffer.append("<table border='1cm' width='800px' cellpadding='1px' cellspacing='0px' class='bolder'><tr><td align='center'>Month</td><td align='center'>Marriage Leave Taken</td><td align='center'>Yearly Leave Balance</td></tr>");
			// prepartation of Table data of Marriage Leaves of employee
			List<MonthlyLeaveReport>  marriageLeaveReportList =	 monthlyLeaveRepository.findAllLeaveReportsOfYearByIdAndLeaveId(employee.getId(), marriageYearlyEntitlementBean.getLeaveType().getId(), Calendar.getInstance().get(Calendar.YEAR));
				if(marriageLeaveReportList!=null && marriageLeaveReportList.size()>0){
					for (MonthlyLeaveReport monthlyLeaveReport : marriageLeaveReportList) {
						leaveReportBuffer.append("<tr><td align='right'>"+(monthlyLeaveReport.getMonthOfYear()!=null?monthlyLeaveReport.getMonthOfYear():"-"));
						leaveReportBuffer.append("</td><td align='center'>"+(monthlyLeaveReport.getLeavesTaken()!=null?monthlyLeaveReport.getLeavesTaken():"-"));
						leaveReportBuffer.append("</td><td align='center'>"+(monthlyLeaveReport.getYearlyLeaveBalance()!=null?monthlyLeaveReport.getYearlyLeaveBalance():"-"+"</td></tr>"));
					}
					
				}
				leaveReportBuffer.append("</table><br/>");
		}
		}catch(Exception e){
			e.printStackTrace();
			leaveReportBuffer = new StringBuffer("<table border='0px' width='800px'><tr><td align='left' style='color:red;font-size:1.2em;'><b>Error while generationg monthly report on Marriage Leave </b></td></tr></table><br/>");
		}
		return leaveReportBuffer;
	}
	private StringBuffer getCompassionateLeaveYearlyReport(Employee employee){
		StringBuffer leaveReportBuffer = new StringBuffer();
		try{
		// Now time for Compassionate Leaves
		List<YearlyEntitlement>  compassionateEntitlementList =	entitlementRepository.findByEmployeeIdAndEmployeeTypeAndLeaveTypeName(employee.getId(), Leave.COMPASSIONATE.toString());
		if(compassionateEntitlementList!=null && compassionateEntitlementList.size()>0){
			YearlyEntitlement	compassionateYearlyEntitlementBean = compassionateEntitlementList.get(0);
			// preparation of Table headers with styles
			leaveReportBuffer.append("<table border='0px' width='800px' class='bolder'><tr><td align='left' style='color:darkorange;font-size:1.2em;'><b>Compassionate Leave </b></td><td align='right'><b> Entitlement </b> = "+compassionateYearlyEntitlementBean.getEntitlement()+" days</td></tr></table>");
			leaveReportBuffer.append("<table border='1cm' width='800px' cellpadding='1px' cellspacing='0px' class='bolder'><tr><td align='center'>Month</td><td align='center'>Compassionate Leave Taken</td><td align='center'>Yearly Leave Balance</td></tr>");
			// prepartation of Table data of Compassionate Leaves of employee
			List<MonthlyLeaveReport>  compassionateLeaveReportList =	 monthlyLeaveRepository.findAllLeaveReportsOfYearByIdAndLeaveId(employee.getId(), compassionateYearlyEntitlementBean.getLeaveType().getId(), Calendar.getInstance().get(Calendar.YEAR));
				if(compassionateLeaveReportList!=null && compassionateLeaveReportList.size()>0){
					for (MonthlyLeaveReport monthlyLeaveReport : compassionateLeaveReportList) {
						leaveReportBuffer.append("<tr><td align='right'>"+(monthlyLeaveReport.getMonthOfYear()!=null?monthlyLeaveReport.getMonthOfYear():"-"));
						leaveReportBuffer.append("</td><td align='center'>"+(monthlyLeaveReport.getLeavesTaken()!=null?monthlyLeaveReport.getLeavesTaken():"-"));
						leaveReportBuffer.append("</td><td align='center'>"+(monthlyLeaveReport.getYearlyLeaveBalance()!=null?monthlyLeaveReport.getYearlyLeaveBalance():"-"+"</td></tr>"));
					}
				}
				leaveReportBuffer.append("</table><br/>");
		}
		}catch(Exception e){
			e.printStackTrace();
			leaveReportBuffer = new StringBuffer("<table border='0px' width='800px'><tr><td align='left' style='color:red;font-size:1.2em;'><b>Error while generationg monthly report on Compassionate Leave </b></td></tr></table><br/>");
		}
		return leaveReportBuffer;
	}
	
	private StringBuffer getPaternityLeaveYearlyReport(Employee employee){
		StringBuffer leaveReportBuffer = new StringBuffer();
		try{
		// Now time for Paternity Leaves
		List<YearlyEntitlement>  paternityEntitlementList =	entitlementRepository.findByEmployeeIdAndEmployeeTypeAndLeaveTypeName(employee.getId(), Leave.PATERNITY.toString());
		if(paternityEntitlementList!=null && paternityEntitlementList.size()>0){
			YearlyEntitlement	paternityYearlyEntitlementBean = paternityEntitlementList.get(0);
			// preparation of Table headers with styles
			leaveReportBuffer.append("<table border='0px' width='800px' class='bolder'><tr><td align='left' style='color:darkorange;font-size:1.2em;'><b>Paternity Leave </b></td><td align='right'><b> Entitlement </b> = "+paternityYearlyEntitlementBean.getEntitlement()+" days</td></tr></table>");
			leaveReportBuffer.append("<table border='1cm' width='800px' cellpadding='1px' cellspacing='0px' class='bolder'><tr><td align='center'>Month</td><td align='center'>Paternity Leave Taken</td><td align='center'>Yearly Leave Balance</td></tr>");
			// prepartation of Table data of Paternity Leaves of employee
			List<MonthlyLeaveReport>  paternityLeaveReportList =	 monthlyLeaveRepository.findAllLeaveReportsOfYearByIdAndLeaveId(employee.getId(), paternityYearlyEntitlementBean.getLeaveType().getId(), Calendar.getInstance().get(Calendar.YEAR));
				if(paternityLeaveReportList!=null && paternityLeaveReportList.size()>0){
					for (MonthlyLeaveReport monthlyLeaveReport : paternityLeaveReportList) {
						leaveReportBuffer.append("<tr><td align='right'>"+(monthlyLeaveReport.getMonthOfYear()!=null?monthlyLeaveReport.getMonthOfYear():"-"));
						leaveReportBuffer.append("</td><td align='center'>"+(monthlyLeaveReport.getLeavesTaken()!=null?monthlyLeaveReport.getLeavesTaken():"-"));
						leaveReportBuffer.append("</td><td align='center'>"+(monthlyLeaveReport.getYearlyLeaveBalance()!=null?monthlyLeaveReport.getYearlyLeaveBalance():"-"+"</td></tr>"));
					}
				}
				leaveReportBuffer.append("</table><br/>");
		}
		}catch(Exception e){
			e.printStackTrace();
			leaveReportBuffer = new StringBuffer("<table border='0px' width='800px'><tr><td align='left' style='color:red;font-size:1.2em;'><b>Error while generationg monthly report on Paternity Leave </b></td></tr></table><br/>");
		}
		return leaveReportBuffer;
	}
	private StringBuffer getMaternityLeaveYearlyReport(Employee employee){
		StringBuffer leaveReportBuffer = new StringBuffer();
		try{
		// Now time for Maternity Leaves
		List<YearlyEntitlement>  maternityEntitlementList =	entitlementRepository.findByEmployeeIdAndEmployeeTypeAndLeaveTypeName(employee.getId(), Leave.MATERNITY.toString());
		if(maternityEntitlementList!=null && maternityEntitlementList.size()>0){
			YearlyEntitlement	maternityYearlyEntitlementBean = maternityEntitlementList.get(0);
			// preparation of Table headers with styles
			leaveReportBuffer.append("<table border='0px' width='800px' class='bolder'><tr><td align='left' style='color:darkorange;font-size:1.2em;'><b>Maternity Leave </b></td><td align='right'><b> Entitlement </b> = "+maternityYearlyEntitlementBean.getEntitlement()+" days</td></tr></table>");
			leaveReportBuffer.append("<table border='1cm' width='800px' cellpadding='1px' cellspacing='0px' class='bolder'><tr><td align='center'>Month</td><td align='center'>Maternity Leave Taken</td><td align='center'>Yearly Leave Balance</td></tr>");
			// prepartation of Table data of Maternity Leaves of employee
			List<MonthlyLeaveReport>  maternityLeaveReportList =	 monthlyLeaveRepository.findAllLeaveReportsOfYearByIdAndLeaveId(employee.getId(), maternityYearlyEntitlementBean.getLeaveType().getId(), Calendar.getInstance().get(Calendar.YEAR));
				if(maternityLeaveReportList!=null && maternityLeaveReportList.size()>0){
					for (MonthlyLeaveReport monthlyLeaveReport : maternityLeaveReportList) {
						leaveReportBuffer.append("<tr><td align='right'>"+(monthlyLeaveReport.getMonthOfYear()!=null?monthlyLeaveReport.getMonthOfYear():"-"));
						leaveReportBuffer.append("</td><td align='center'>"+(monthlyLeaveReport.getLeavesTaken()!=null?monthlyLeaveReport.getLeavesTaken():"-"));
						leaveReportBuffer.append("</td><td align='center'>"+(monthlyLeaveReport.getYearlyLeaveBalance()!=null?monthlyLeaveReport.getYearlyLeaveBalance():"-"+"</td></tr>"));
					}
				}
				leaveReportBuffer.append("</table><br/>");
		}
		}catch(Exception e){
			e.printStackTrace();
			leaveReportBuffer = new StringBuffer("<table border='0px' width='800px'><tr><td align='left' style='color:red;font-size:1.2em;'><b>Error while generationg monthly report on Maternity Leave </b></td></tr></table><br/>");
		}
		return leaveReportBuffer;
	}
	
	private StringBuffer getUnpaidLeaveYearlyReport(Employee employee){
		StringBuffer leaveReportBuffer = new StringBuffer();
		try{
		// Now time for Maternity Leaves
		List<YearlyEntitlement>  unpaidEntitlementList =	entitlementRepository.findByEmployeeIdAndEmployeeTypeAndLeaveTypeName(employee.getId(), Leave.UNPAID.toString());
		if(unpaidEntitlementList!=null && unpaidEntitlementList.size()>0){
			YearlyEntitlement	unpaidYearlyEntitlementBean = unpaidEntitlementList.get(0);
			// preparation of Table headers with styles
			leaveReportBuffer.append("<table border='0px' width='800px' class='bolder'><tr><td align='left' style='color:darkorange;font-size:1.2em;'><b>Unpaid Leave </b></td></tr></table>");
			leaveReportBuffer.append("<table border='1cm' width='800px' cellpadding='1px' cellspacing='0px' class='bolder'><tr><td align='center'>Month</td><td align='center'>Unpaid Leave Taken</td></tr>");
			// prepartation of Table data of Maternity Leaves of employee
			List<MonthlyLeaveReport>  unpaidLeaveReportList =	 monthlyLeaveRepository.findAllLeaveReportsOfYearByIdAndLeaveId(employee.getId(), unpaidYearlyEntitlementBean.getLeaveType().getId(), Calendar.getInstance().get(Calendar.YEAR));
				if(unpaidLeaveReportList!=null && unpaidLeaveReportList.size()>0){
					for (MonthlyLeaveReport monthlyLeaveReport : unpaidLeaveReportList) {
						leaveReportBuffer.append("<tr><td align='right'>"+(monthlyLeaveReport.getMonthOfYear()!=null?monthlyLeaveReport.getMonthOfYear():"-"));
						leaveReportBuffer.append("</td><td align='center'>"+(monthlyLeaveReport.getLeavesTaken()!=null?monthlyLeaveReport.getLeavesTaken():"-"+"</td></tr>"));
					}
				}
				leaveReportBuffer.append("</table><br/>");
		}
		}catch(Exception e){
			e.printStackTrace();
			leaveReportBuffer = new StringBuffer("<table border='0px' width='800px'><tr><td align='left' style='color:red;font-size:1.2em;'><b>Error while generationg monthly report on Unpaid Leave </b></td></tr></table><br/>");
		}
		return leaveReportBuffer;
	}
	
	private StringBuffer getAllAppliedAndApprovedLeavesList(Employee employee){
			StringBuffer leaveReportBuffer = new StringBuffer();
			try{
				Calendar firstDayOfYear = Calendar.getInstance();
				firstDayOfYear.set(Calendar.MONTH, 0);
				firstDayOfYear.set(Calendar.DATE,1);
				firstDayOfYear.set(Calendar.HOUR_OF_DAY,1);
				
			List<LeaveTransaction>  allAppliedLeavesList =	leaveRepository.findAllLeavesAppliedByEmployee(employee.getId(),new java.sql.Date(firstDayOfYear.getTime().getTime()),new java.sql.Date(new Date().getTime()));
			// preparation of Table headers with styles
			leaveReportBuffer.append("<table border='0px' width='800px' class='bolder'><tr><td align='center' style='color:black;font-size:1.2em;'><b>List of Leave Applied and Approved up to "+getPresentMonth(Calendar.getInstance().get(Calendar.MONTH))+" </b></td></tr></table>");
			leaveReportBuffer.append("<table border='1cm' width='800px' cellpadding='1px' cellspacing='0px' class='bolder'><tr><td align='center'>Leave Type</td><td align='center'>Applied Date</td><td align='center'>Start Date</td><td align='center'>End Date</td><td align='center'>Number of Days</td><td  align='center'>Status </td></tr>");
			// prepartation of Table data of All Leaves applied by employee from January to Current month.
			if(allAppliedLeavesList!=null && allAppliedLeavesList.size()>0){
						for (LeaveTransaction appliedLeaveBean : allAppliedLeavesList) {
							leaveReportBuffer.append("<tr><td align='center'>"+appliedLeaveBean.getLeaveType().getName());
							leaveReportBuffer.append("</td><td align='center'>"+formatDate(appliedLeaveBean.getApplicationDate()));
							leaveReportBuffer.append("</td><td align='center'>"+formatDate(appliedLeaveBean.getStartDateTime()));
							leaveReportBuffer.append("</td><td align='center'>"+formatDate(appliedLeaveBean.getEndDateTime()));
							leaveReportBuffer.append("</td><td align='center'>"+appliedLeaveBean.getNumberOfDays());
							leaveReportBuffer.append("</td><td align='center'>"+appliedLeaveBean.getStatus()+"</td></tr>");
						}
					leaveReportBuffer.append("</table><br/>");
			}
			else
				leaveReportBuffer.append("<tr><td colspan='6' align='center'><b>No records found!!!</b></td></tr></table><br/>");
			}catch(Exception e){
				e.printStackTrace();
				leaveReportBuffer = new StringBuffer("<table border='0px' width='800px'><tr><td align='left' style='color:red;font-size:1.2em;'><b>Error while generationg monthly report on Applied and Approved Leaves of Employee </b></td></tr></table><br/>");
			}
			return leaveReportBuffer;
		
	}
	
	private StringBuffer getAllAppliedAndApprovedTimeInLieuList(Employee employee){
		StringBuffer leaveReportBuffer = new StringBuffer();
		try{
			Calendar firstDayOfYear = Calendar.getInstance();
			firstDayOfYear.set(Calendar.MONTH, 0);
			firstDayOfYear.set(Calendar.DATE,1);
			firstDayOfYear.set(Calendar.HOUR_OF_DAY,1);
			
		List<LeaveTransaction>  allAppliedLeavesList =	leaveRepository.findAllTimeInLieuLeavesAppliedByEmployee(employee.getId(),new java.sql.Date(firstDayOfYear.getTime().getTime()),new java.sql.Date(new Date().getTime()));
		// preparation of Table headers with styles
		leaveReportBuffer.append("<table border='0px' width='800px' class='bolder'><tr><td align='center' style='color:black;font-size:1.2em;'><b>List of Time-In-Lieu Applied and Approved up to "+getPresentMonth(Calendar.getInstance().get(Calendar.MONTH))+" </b></td></tr></table>");
		leaveReportBuffer.append("<table border='1cm' width='800px' cellpadding='1px' cellspacing='0px' class='bolder'><tr><td align='center'>Applied Date</td><td align='center'>Start Date</td><td align='center'>End Date</td><td align='center'>Number of Days</td><td align='center'>Status</td></tr>");
		// prepartation of Table data of All Leaves applied by employee from January to Current month.
		if(allAppliedLeavesList!=null && allAppliedLeavesList.size()>0){
					for (LeaveTransaction appliedLeaveBean : allAppliedLeavesList) {
						leaveReportBuffer.append("<tr><td align='center'>"+formatDate(appliedLeaveBean.getApplicationDate()));
						leaveReportBuffer.append("</td><td align='center'>"+formatDate(appliedLeaveBean.getStartDateTime()));
						leaveReportBuffer.append("</td><td align='center'>"+formatDate(appliedLeaveBean.getEndDateTime()));
						leaveReportBuffer.append("</td><td align='center'>"+appliedLeaveBean.getNumberOfDays());
						leaveReportBuffer.append("</td><td align='center'>"+appliedLeaveBean.getStatus()+"</td></tr>");
					}
				leaveReportBuffer.append("</table><br/><br/>");
		}
		else
			leaveReportBuffer.append("<tr><td colspan='5' align='center'><b>No records found!!!</td></td></tr></table><br/>");
		}catch(Exception e){
			e.printStackTrace();
			leaveReportBuffer = new StringBuffer("<table border='0px' width='800px'><tr><td align='left' style='color:red;font-size:1.2em;'><b>Error while generationg monthly report on Applied and Approved Leaves of Employee </b></td></tr></table><br/>");
		}
		return leaveReportBuffer;
	
}
	
	private void calculateAndUpdateLeavesDeduction(Employee employee){
		double deductedLeaves=0;
		try{
		//AnnualLeaveReport lastDecAnnualRecord =	annualLeaveRepository.getCurrentMonthAnnualLeaveRecord(employee.getId(), 12, Calendar.getInstance().get(Calendar.YEAR)-1);
		List<AnnualLeaveReport> janAndFebAnnualRecordList = annualLeaveRepository.getRecordsOfCurrentYearJanAndFeb(employee.getId(), Calendar.getInstance().get(Calendar.YEAR));
		if(janAndFebAnnualRecordList!=null && janAndFebAnnualRecordList.size()==2){
			AnnualLeaveReport janReport = janAndFebAnnualRecordList.get(0);
			AnnualLeaveReport febReport = janAndFebAnnualRecordList.get(1);
			deductedLeaves =   janReport.getBalanceBroughtForward();
			deductedLeaves = deductedLeaves-janReport.getLeavesTaken()-febReport.getLeavesTaken()-5;
			if(deductedLeaves>=1){
			// deduct this many leaves from yearly entitlement table as well as AnnualLeaveReport table.
			List<YearlyEntitlement>	annualEntitlementList =	entitlementRepository.findByEmployeeIdAndEmployeeTypeAndLeaveTypeName(employee.getId(),Leave.ANNUAL.toString());
			if(annualEntitlementList!=null && annualEntitlementList.size()==1){
				YearlyEntitlement entitlementBeanToBeUpdated = annualEntitlementList.get(0);
				entitlementBeanToBeUpdated.setcurrentLeaveBalance(entitlementBeanToBeUpdated.getCurrentLeaveBalance()-deductedLeaves);
				entitlementBeanToBeUpdated.setYearlyLeaveBalance(entitlementBeanToBeUpdated.getYearlyLeaveBalance()-deductedLeaves);
				entitlementRepository.save(entitlementBeanToBeUpdated);
			}
			// Updating in AnnualLeaveReport table
			febReport.setCurrentLeaveBalance(febReport.getCurrentLeaveBalance()-deductedLeaves);
			febReport.setYearlyLeaveBalance(febReport.getYearlyLeaveBalance()-deductedLeaves);
			annualLeaveRepository.save(febReport);
			
			// Updating in LeaveDeductHistory table
			LeaveDeductHistory leaveDeductBean = new LeaveDeductHistory();
			leaveDeductBean.setEmployee(employee);
			leaveDeductBean.setFinancialYear(Calendar.getInstance().get(Calendar.YEAR));
			leaveDeductBean.setNumberOfLeavesDeducted(deductedLeaves);
			leaveDeductRepository.save(leaveDeductBean);
			}
		}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	private StringBuffer getLeaveDeductionMessage(Employee employee){
		StringBuffer leaveReportBuffer= new StringBuffer();
		LeaveDeductHistory leaveDeductBean = leaveDeductRepository.getLeavesDeductionByIdAndYear(employee.getId(), Calendar.getInstance().get(Calendar.YEAR));
		if(leaveDeductBean!=null && leaveDeductBean.getNumberOfLeavesDeducted()>0)
				leaveReportBuffer.append("<table border='0px' width='800px' class='bolder'><tr><td align='left' style='color:red;font-size:1.2em;'><b>Brought Forward Remainder Leave Deducted: "+leaveDeductBean.getNumberOfLeavesDeducted()+" Days</u></b></td></tr></table>");
			else
				leaveReportBuffer.append("<table border='0px' width='800px' class='bolder'><tr><td align='left' style='color:red;font-size:1.2em;'><b>Brought Forward Remainder Leave Deducted: 0 Days</u></b></td></tr></table>");
		return leaveReportBuffer;
	}
	
	private StringBuffer getRemainderMessageInJanuary(){
		StringBuffer leaveReportBuffer = new StringBuffer();
		leaveReportBuffer.append("<table border='0px' width='800px' class='bolder'><tr><td align='center' style='color:red;font-size:1.2em;'><b><u>REMINDER </u></b></td></tr></table>");
		leaveReportBuffer.append("<table border='1cm' width='800px' cellpadding='1px' cellspacing='0px' class='bolder'><tr><td style='color:red;'>You are only allowed to carry forward 5 days to this year. If your Leave To Brought Forward is more than 5, please use up the remainder days before End of February or the remainder days will be deducted.</td></tr></table><br/>");
		return leaveReportBuffer;
	}
	
	private StringBuffer getRemainderMessageInDecember(){
		StringBuffer leaveReportBuffer = new StringBuffer();
		leaveReportBuffer.append("<table border='0px' width='800px' class='bolder'><tr><td align='center' style='color:red;font-size:1.2em;'><b><u>REMINDER </u></b></td></tr></table>");
		leaveReportBuffer.append("<table border='1cm' width='800px' cellpadding='1px' cellspacing='0px' class='bolder'><tr><td style='color:red;'>You are only allowed to carry forward 5 days to next year. If your Leave To Brought Forward is more than 5, please use up the remainder days before End of February or the remainder days will be deducted.</td></tr></table><br/>");
		return leaveReportBuffer;
	}
	
	private String formatDate(Date givenDate){
		return new SimpleDateFormat("dd/MM/yyyy").format(givenDate);
	}
	
	private StringBuffer getTimeInLieuYearlyReportForInternship(Employee employee){
		StringBuffer leaveReportBuffer = new StringBuffer();
		try{
		// Firstly prepare TimeinLieu Leaves
			// preparation of Table headers with styles
			leaveReportBuffer.append("<table border='0px' width='800px'><tr><td align='left' style='color:darkorange;font-size:1.2em;'><b>Time-In-Lieu Leave </b></td></tr></table>");
			leaveReportBuffer.append("<table border='1cm' width='800px' cellpadding='1px' cellspacing='0px' class='bolder'><tr><td>Month</td><td>Time-In-Lieu Credited</td><td>Leave Taken</td><td>Leave Balance</td></tr>");
			// prepartation of Table data of Annual Leaves of employee
			List<AnnualLeaveReport>	 annualLeaveList = annualLeaveRepository.getAnnualLeaveDataOfEmployee(employee.getId(), Calendar.getInstance().get(Calendar.YEAR));
			if(annualLeaveList!=null && annualLeaveList.size()>0){
				
				for (AnnualLeaveReport annualLeaveReport : annualLeaveList) {
					leaveReportBuffer.append("<tr><td align='center'>"+(annualLeaveReport.getMonthOfYear()!=null?annualLeaveReport.getMonthOfYear():"-"));
					leaveReportBuffer.append("</td><td align='center'>"+(annualLeaveReport.getTimeInLieuCredited()!=null?annualLeaveReport.getTimeInLieuCredited():"-"));
					leaveReportBuffer.append("</td><td align='center'>"+(annualLeaveReport.getLeavesTaken()!=null?annualLeaveReport.getLeavesTaken():"-"));
					leaveReportBuffer.append("</td><td align='center'>"+(annualLeaveReport.getYearlyLeaveBalance()!=null?annualLeaveReport.getCurrentLeaveBalance():"-"+"</td></tr>"));
				}
				
			}
			leaveReportBuffer.append("</table><br/>");
		}catch(Exception e){
			e.printStackTrace();
			leaveReportBuffer = new StringBuffer("<table border='0px' width='800px'><tr><td align='left' style='color:red;font-size:1.2em;'><b>Error while generationg monthly report on Time-In-Lieu Leave </b></td></tr></table><br/>");
		}
		return leaveReportBuffer;
	}

	@Override
	public void updateEmployeeLeavesAfterLeaveApproval(LeaveTransaction leaveTransaction,Date applicationDate) {
		// Update Annual leaves of employee of application dated month and total also
		try{
		Calendar leaveApplicationDate = Calendar.getInstance();
		leaveApplicationDate.setTime(applicationDate);
		
		if(Leave.ANNUAL.equalsName(leaveTransaction.getLeaveType().getName())){
			List<AnnualLeaveReport>	 annualLeaveList=null;
			if(leaveApplicationDate.get(Calendar.MONTH)+1 == Calendar.getInstance().get(Calendar.MONTH)+1)
				annualLeaveList = annualLeaveRepository.getEmployeeMonthlyLeaveReportData(leaveTransaction.getEmployee().getId(), leaveApplicationDate.get(Calendar.MONTH)+1, leaveApplicationDate.get(Calendar.YEAR));
			else
				annualLeaveList = annualLeaveRepository.getEmployeeMonthlyLeaveReportData(leaveTransaction.getEmployee().getId(), Calendar.getInstance().get(Calendar.MONTH)+1, Calendar.getInstance().get(Calendar.YEAR));
				
				if(annualLeaveList!=null && annualLeaveList.size()==2){
				
				ArrayList<AnnualLeaveReport> annualLeaveToBeUpdatedList = new ArrayList<AnnualLeaveReport>();
					
				AnnualLeaveReport annualLeaveCurrentMonth =	annualLeaveList.get(0);
				if(annualLeaveCurrentMonth.getCurrentLeaveBalance()!=null)
					annualLeaveCurrentMonth.setCurrentLeaveBalance(annualLeaveCurrentMonth.getCurrentLeaveBalance()-leaveTransaction.getNumberOfDays());
				else
					annualLeaveCurrentMonth.setCurrentLeaveBalance(0-leaveTransaction.getNumberOfDays());
				
				if(annualLeaveCurrentMonth.getYearlyLeaveBalance()!=null)
					annualLeaveCurrentMonth.setYearlyLeaveBalance(annualLeaveCurrentMonth.getYearlyLeaveBalance()-leaveTransaction.getNumberOfDays());
				else
					annualLeaveCurrentMonth.setYearlyLeaveBalance(leaveTransaction.getYearlyLeaveBalance()-leaveTransaction.getNumberOfDays());
				
				if(annualLeaveCurrentMonth.getLeavesTaken()==null)
					annualLeaveCurrentMonth.setLeavesTaken(leaveTransaction.getNumberOfDays());
				else
					annualLeaveCurrentMonth.setLeavesTaken(leaveTransaction.getNumberOfDays()+annualLeaveCurrentMonth.getLeavesTaken());
				
				annualLeaveToBeUpdatedList.add(annualLeaveCurrentMonth);
				
				AnnualLeaveReport annualLeaveYearlyTotal =	annualLeaveList.get(1);
				if(annualLeaveYearlyTotal.getLeavesTaken()!=null)
					annualLeaveYearlyTotal.setLeavesTaken(annualLeaveYearlyTotal.getLeavesTaken()+leaveTransaction.getNumberOfDays());
				else
					annualLeaveYearlyTotal.setLeavesTaken(leaveTransaction.getNumberOfDays());
				annualLeaveToBeUpdatedList.add(annualLeaveYearlyTotal);
				annualLeaveRepository.save(annualLeaveToBeUpdatedList);
			}
		}
		else{
			
			List<MonthlyLeaveReport>  leaveReportList =	monthlyLeaveRepository.getEmployeeMonthlyLeaveReportData(leaveTransaction.getEmployee().getId(),  leaveApplicationDate.get(Calendar.MONTH)+1, leaveTransaction.getLeaveType().getId(), leaveApplicationDate.get(Calendar.YEAR));
			if(leaveReportList!=null && leaveReportList.size()==2){
				List<MonthlyLeaveReport> leaveTypeToBeUpdatedList = new ArrayList<MonthlyLeaveReport>();
				if(Leave.UNPAID.equalsName(leaveTransaction.getLeaveType().getName())){
					
					MonthlyLeaveReport leaveTypeCurrentMonth =	leaveReportList.get(0);
					if(leaveTypeCurrentMonth.getLeavesTaken()==null)
						leaveTypeCurrentMonth.setLeavesTaken(leaveTransaction.getNumberOfDays());
					else
						leaveTypeCurrentMonth.setLeavesTaken(leaveTransaction.getNumberOfDays()+leaveTypeCurrentMonth.getLeavesTaken());
					leaveTypeToBeUpdatedList.add(leaveTypeCurrentMonth);
					
					MonthlyLeaveReport leaveTypeYearlyTotal =	leaveReportList.get(1);
					if(leaveTypeYearlyTotal.getLeavesTaken()!=null)
						leaveTypeYearlyTotal.setLeavesTaken(leaveTransaction.getNumberOfDays()+leaveTypeYearlyTotal.getLeavesTaken());
					else
						leaveTypeYearlyTotal.setLeavesTaken(leaveTransaction.getNumberOfDays());
					leaveTypeToBeUpdatedList.add(leaveTypeYearlyTotal);
				}
				else{
					MonthlyLeaveReport leaveTypeCurrentMonth =	leaveReportList.get(0);
					if(leaveTypeCurrentMonth.getLeavesTaken()==null)
						leaveTypeCurrentMonth.setLeavesTaken(leaveTransaction.getNumberOfDays());
					else
						leaveTypeCurrentMonth.setLeavesTaken(leaveTransaction.getNumberOfDays()+leaveTypeCurrentMonth.getLeavesTaken());
					leaveTypeCurrentMonth.setYearlyLeaveBalance(leaveTypeCurrentMonth.getYearlyLeaveBalance()-leaveTransaction.getNumberOfDays());
					leaveTypeToBeUpdatedList.add(leaveTypeCurrentMonth);
					
					MonthlyLeaveReport leaveTypeYearlyTotal =	leaveReportList.get(1);
					if(leaveTypeYearlyTotal.getLeavesTaken()!=null)
						leaveTypeYearlyTotal.setLeavesTaken(leaveTransaction.getNumberOfDays()+leaveTypeYearlyTotal.getLeavesTaken());
					else
						leaveTypeYearlyTotal.setLeavesTaken(leaveTransaction.getNumberOfDays());
					if(leaveTypeYearlyTotal.getYearlyLeaveBalance()!=null)
						leaveTypeYearlyTotal.setYearlyLeaveBalance(leaveTypeYearlyTotal.getYearlyLeaveBalance()-leaveTransaction.getNumberOfDays());
						
					leaveTypeToBeUpdatedList.add(leaveTypeYearlyTotal);
				}
				monthlyLeaveRepository.save(leaveTypeToBeUpdatedList);
			}
		 }
		}catch(Exception e){
			System.out.println("Error while updating leaves balance after approval for employee : "+leaveTransaction.getEmployee().getName());
			e.printStackTrace();
		}
		
	}
	
	// Update Annual leaves of employee when Time-In-Lieu leave application is approved
	@Override
	public void updateEmployeeAnnualLeavesAfterLeaveApproval(LeaveTransaction leaveTransaction,Date applicationDate) {
		try{
		Calendar leaveApplicationDate = Calendar.getInstance();
		leaveApplicationDate.setTime(applicationDate);
		AnnualLeaveReport annualLeaveCurrentMonth=null;
			if(leaveApplicationDate.get(Calendar.MONTH)+1 == Calendar.getInstance().get(Calendar.MONTH)+1)
				annualLeaveCurrentMonth = annualLeaveRepository.getCurrentMonthAnnualLeaveRecord(leaveTransaction.getEmployee().getId(), leaveApplicationDate.get(Calendar.MONTH)+1, leaveApplicationDate.get(Calendar.YEAR));
			else
				annualLeaveCurrentMonth = annualLeaveRepository.getCurrentMonthAnnualLeaveRecord(leaveTransaction.getEmployee().getId(), Calendar.getInstance().get(Calendar.MONTH)+1, Calendar.getInstance().get(Calendar.YEAR));
				
				if(annualLeaveCurrentMonth!=null){
				ArrayList<AnnualLeaveReport> annualLeaveToBeUpdatedList = new ArrayList<AnnualLeaveReport>();
				if(annualLeaveCurrentMonth.getCurrentLeaveBalance()!=null)
					annualLeaveCurrentMonth.setCurrentLeaveBalance(annualLeaveCurrentMonth.getCurrentLeaveBalance()+leaveTransaction.getNumberOfDays());
				if(annualLeaveCurrentMonth.getYearlyLeaveBalance()!=null)
					annualLeaveCurrentMonth.setYearlyLeaveBalance(annualLeaveCurrentMonth.getYearlyLeaveBalance()+leaveTransaction.getNumberOfDays());
				if(annualLeaveCurrentMonth.getTimeInLieuCredited()!=null)
					annualLeaveCurrentMonth.setTimeInLieuCredited(annualLeaveCurrentMonth.getTimeInLieuCredited()+leaveTransaction.getNumberOfDays());
				annualLeaveToBeUpdatedList.add(annualLeaveCurrentMonth);
				annualLeaveRepository.save(annualLeaveToBeUpdatedList);
			}
		
		}catch(Exception e){
			System.out.println("Error while updating leaves balance after approval for employee : "+leaveTransaction.getEmployee().getName());
			e.printStackTrace();
		}
		
	}
	
	

	@Override
	public void initializeMonthlyLeaveReportWithDefaultValues() {
		
		try{
			// Selecting all employee who are PERM , CONT and INT which excludes roles with ROLE_ADMIN and ROLE_OPERDIR
			List<Employee> employeeList = employeeRepository.findAllEmployeesForSendingMonthlyLeaveReport();
			if(employeeList!=null && employeeList.size()>0){
				for (Employee employee : employeeList) {
				// Firstly initialize Annual Leaves of present month of all employees
				Calendar currentMonth = Calendar.getInstance();
				List<Integer> sortingMonthList = new ArrayList<Integer>();
				sortingMonthList.add(currentMonth.get(Calendar.MONTH));
				sortingMonthList.add(currentMonth.get(Calendar.MONTH)+1);	
				if(currentMonth.get(Calendar.MONTH)==0){
					List<YearlyEntitlement>	 entitlmentList =	entitlementRepository.findByEmployeeIdAndEmployeeTypeAndLeaveTypeName(employee.getId(), Leave.ANNUAL.toString());
					if(entitlmentList!=null && entitlmentList.size()>0){
					YearlyEntitlement newEntitlementBean =	entitlmentList.get(0);
					List<AnnualLeaveReport> twoRecordList =	annualLeaveRepository.getEmployeeMonthlyLeaveReportData(employee.getId(), 1, currentMonth.get(Calendar.YEAR));
					if(twoRecordList!=null && twoRecordList.size()==2){
					List<AnnualLeaveReport> listToBeUpdated = new ArrayList<AnnualLeaveReport>();
					AnnualLeaveReport januaryMonthRecord =	twoRecordList.get(0);
					januaryMonthRecord.setCurrentLeaveBalance(newEntitlementBean.getCurrentLeaveBalance());
					januaryMonthRecord.setYearlyLeaveBalance(newEntitlementBean.getYearlyLeaveBalance());
					januaryMonthRecord.setBalanceBroughtForward(new Double(0));
					januaryMonthRecord.setLeavesCredited(new Double(0));
					januaryMonthRecord.setLeavesTaken(new Double(0));
					januaryMonthRecord.setTimeInLieuCredited(new Double(0));
					listToBeUpdated.add(januaryMonthRecord);
					
					AnnualLeaveReport totalCountRecord =	twoRecordList.get(1);
					totalCountRecord.setLeavesCredited(new Double(0));
					totalCountRecord.setLeavesTaken(new Double(0));
					totalCountRecord.setTimeInLieuCredited(new Double(0));
					listToBeUpdated.add(totalCountRecord);
					
					annualLeaveRepository.save(listToBeUpdated);
					}
					}
				}
				else {
				List<AnnualLeaveReport>	 annualLeaveList = annualLeaveRepository.getEmployeeMonthlyLeaveReportDataForInitialization(employee.getId(), sortingMonthList, currentMonth.get(Calendar.YEAR));
				if(annualLeaveList!=null && annualLeaveList.size()==2){
					AnnualLeaveReport previousMonthRecord = annualLeaveList.get(0);	
					AnnualLeaveReport currentMonthRecord = annualLeaveList.get(1);
					currentMonthRecord.setBalanceBroughtForward(new Double(0));
					currentMonthRecord.setCurrentLeaveBalance(previousMonthRecord.getCurrentLeaveBalance());
					currentMonthRecord.setLeavesCredited(new Double(0));
					currentMonthRecord.setLeavesTaken(new Double(0));
					currentMonthRecord.setTimeInLieuCredited(new Double(0));
					currentMonthRecord.setYearlyLeaveBalance(previousMonthRecord.getYearlyLeaveBalance());
					// setting default values and updating the current month record only
					annualLeaveRepository.save(currentMonthRecord);
				 }
				}
				// Secondly initialize except Annual Leaves of present month of all employees
				List<LeaveType>		leaveTypeList =	leaveTypeRepository.getAllLeaveTypesGivenByEmployeeType(employee.getId());
				if(leaveTypeList!=null && leaveTypeList.size()>0){
					for (LeaveType leaveType : leaveTypeList) {
						if(currentMonth.get(Calendar.MONTH)==0){
						//MonthlyLeaveReport decemberMonthRecord = monthlyLeaveRepository.getCurrentMonthAnnualLeaveRecord(employee.getId(), 12, leaveType.getId(), currentMonth.get(Calendar.YEAR)-1);
						MonthlyLeaveReport januaryMonthRecord = monthlyLeaveRepository.getCurrentMonthAnnualLeaveRecord(employee.getId(), 1, leaveType.getId(), currentMonth.get(Calendar.YEAR));
						if(januaryMonthRecord != null){
						januaryMonthRecord.setLeavesTaken(new Double(0));
						januaryMonthRecord.setYearlyLeaveBalance(leaveType.getEntitlement());
						
						monthlyLeaveRepository.save(januaryMonthRecord);
						}
						}
						else {
							List<MonthlyLeaveReport>	monthlyLeaveList = monthlyLeaveRepository.getEmployeeMonthlyLeaveReportDataForInitialization(employee.getId(), sortingMonthList, leaveType.getId(), currentMonth.get(Calendar.YEAR));
							if(monthlyLeaveList!=null && monthlyLeaveList.size()==2){
							MonthlyLeaveReport previousMonthRecord =	monthlyLeaveList.get(0);
							MonthlyLeaveReport currentMonthRecord =	monthlyLeaveList.get(1);
							currentMonthRecord.setLeavesTaken(new Double(0));
							currentMonthRecord.setYearlyLeaveBalance(previousMonthRecord.getYearlyLeaveBalance());
							// setting default values and updating the current month record only
							monthlyLeaveRepository.save(currentMonthRecord);
							}
						}
				   }
				}
				ApplLogger.getLogger().info("Initialized Employee monthly report row with previous month for Employee : "+employee.getName());
			 }
			}
			}catch(Exception e){
				System.out.println("Error while initializing with current month leaves fields.");
				e.printStackTrace();
			}
	}

	@Override
	public void initializeMonthlyLeaveReportForNewEmployee(Employee employee) {
		
		try{
				// Firstly initialize Annual Leaves of present month of new employee
				Calendar currentMonth = Calendar.getInstance();
				currentMonth.setTime(employee.getJoinDate());
				List<AnnualLeaveReport>	 annualLeaveList = annualLeaveRepository.getEmployeeMonthlyLeaveReportData(employee.getId(), currentMonth.get(Calendar.MONTH)+1, currentMonth.get(Calendar.YEAR));
				if(annualLeaveList!=null && annualLeaveList.size()==2){
					AnnualLeaveReport totalRecord = annualLeaveList.get(1);	
					AnnualLeaveReport currentMonthRecord = annualLeaveList.get(0);
					YearlyEntitlement annualEntitlement =null;
					List<YearlyEntitlement> entitlementList =	entitlementRepository.findByEmployeeIdAndEmployeeTypeAndLeaveTypeName(employee.getId(), Leave.ANNUAL.toString());
					if(entitlementList!=null && entitlementList.size()>0){
						annualEntitlement =	entitlementList.get(0);
						currentMonthRecord.setCurrentLeaveBalance(annualEntitlement.getCurrentLeaveBalance());
						currentMonthRecord.setYearlyLeaveBalance(annualEntitlement.getYearlyLeaveBalance());
					}
					currentMonthRecord.setBalanceBroughtForward(new Double(0));
					currentMonthRecord.setLeavesCredited(new Double(0));
					currentMonthRecord.setLeavesTaken(new Double(0));
					currentMonthRecord.setTimeInLieuCredited(new Double(0));
					annualLeaveRepository.save(currentMonthRecord);
					// setting default values and updating the current month record only
					totalRecord.setBalanceBroughtForward(new Double(0));
					totalRecord.setLeavesCredited(new Double(0));
					totalRecord.setLeavesTaken(new Double(0));
					annualLeaveRepository.save(totalRecord);
				 }
				// Secondly initialize except Annual Leaves of present month of newly joined employee
				List<LeaveType>		leaveTypeList =	leaveTypeRepository.getAllLeaveTypesGivenByEmployeeType(employee.getId());
				if(leaveTypeList!=null && leaveTypeList.size()>0){
					for (LeaveType leaveType : leaveTypeList) {
							List<MonthlyLeaveReport>	monthlyLeaveList = monthlyLeaveRepository.getEmployeeMonthlyLeaveReportData(employee.getId(),  currentMonth.get(Calendar.MONTH)+1, leaveType.getId(), currentMonth.get(Calendar.YEAR));
							if(monthlyLeaveList!=null && monthlyLeaveList.size()==2){
							MonthlyLeaveReport currentMonthRecord =	monthlyLeaveList.get(0);
							MonthlyLeaveReport totalRecord =	monthlyLeaveList.get(1);
							YearlyEntitlement		leaveTypeEntitlement =		entitlementRepository.findByEmployeeAndLeaveTypeId(employee.getId(), leaveType.getId());
							if(leaveTypeEntitlement!=null){
							currentMonthRecord.setLeavesTaken(new Double(0));
							currentMonthRecord.setYearlyLeaveBalance(leaveTypeEntitlement.getYearlyLeaveBalance());
							// setting default values and updating the current month record only
							monthlyLeaveRepository.save(currentMonthRecord);
							totalRecord.setYearlyLeaveBalance(leaveTypeEntitlement.getYearlyLeaveBalance());
							totalRecord.setLeavesTaken(new Double(0));
							monthlyLeaveRepository.save(totalRecord);
							}
							
							}
						}
				}
			}catch(Exception e){
				System.out.println("Error while initializing with current month leaves fields.");
				e.printStackTrace();
				throw new RuntimeException("Can't process request, kindly add employee again.");
			}
	}

	
	
	@Override
	public void updateLeaveBalanceAfterCancelled(LeaveTransaction leaveTransaction) {
		
		try{
			Calendar leaveApplicationDate = Calendar.getInstance();
			leaveApplicationDate.setTime(leaveTransaction.getApplicationDate());
			
			if(Leave.ANNUAL.equalsName(leaveTransaction.getLeaveType().getName())){
					List<AnnualLeaveReport>	 annualLeaveList = annualLeaveRepository.getEmployeeMonthlyLeaveReportData(leaveTransaction.getEmployee().getId(), leaveApplicationDate.get(Calendar.MONTH)+1, leaveApplicationDate.get(Calendar.YEAR));
					if(annualLeaveList!=null && annualLeaveList.size()==2){
					
					ArrayList<AnnualLeaveReport> annualLeaveToBeUpdatedList = new ArrayList<AnnualLeaveReport>();
						
					AnnualLeaveReport annualLeaveCurrentMonth =	annualLeaveList.get(0);
					if(annualLeaveCurrentMonth.getCurrentLeaveBalance()!=null)
						annualLeaveCurrentMonth.setCurrentLeaveBalance(annualLeaveCurrentMonth.getCurrentLeaveBalance()+leaveTransaction.getNumberOfDays());
					
					if(annualLeaveCurrentMonth.getYearlyLeaveBalance()!=null)
						annualLeaveCurrentMonth.setYearlyLeaveBalance(annualLeaveCurrentMonth.getYearlyLeaveBalance()+leaveTransaction.getNumberOfDays());
					
					if(annualLeaveCurrentMonth.getLeavesTaken()!=null)
						annualLeaveCurrentMonth.setLeavesTaken(annualLeaveCurrentMonth.getLeavesTaken()-leaveTransaction.getNumberOfDays());

					if(annualLeaveCurrentMonth.getLeavesCredited()!=null)
						annualLeaveCurrentMonth.setLeavesCredited(annualLeaveCurrentMonth.getLeavesCredited()+leaveTransaction.getNumberOfDays());
				
					annualLeaveToBeUpdatedList.add(annualLeaveCurrentMonth);
					
					AnnualLeaveReport annualLeaveYearlyTotal =	annualLeaveList.get(1);
					if(annualLeaveYearlyTotal.getLeavesTaken()!=null)
						annualLeaveYearlyTotal.setLeavesTaken(annualLeaveYearlyTotal.getLeavesTaken()-leaveTransaction.getNumberOfDays());
					if(annualLeaveYearlyTotal.getLeavesCredited()!=null)
						annualLeaveYearlyTotal.setLeavesCredited(annualLeaveYearlyTotal.getLeavesCredited()+leaveTransaction.getNumberOfDays());
					annualLeaveToBeUpdatedList.add(annualLeaveYearlyTotal);
					annualLeaveRepository.save(annualLeaveToBeUpdatedList);
				}
			}
			else{
				
				List<MonthlyLeaveReport>  leaveReportList =	monthlyLeaveRepository.getEmployeeMonthlyLeaveReportData(leaveTransaction.getEmployee().getId(),  leaveApplicationDate.get(Calendar.MONTH)+1, leaveTransaction.getLeaveType().getId(), leaveApplicationDate.get(Calendar.YEAR));
				if(leaveReportList!=null && leaveReportList.size()==2){
					List<MonthlyLeaveReport> leaveTypeToBeUpdatedList = new ArrayList<MonthlyLeaveReport>();
					if(Leave.UNPAID.equalsName(leaveTransaction.getLeaveType().getName())){
						
						MonthlyLeaveReport leaveTypeCurrentMonth =	leaveReportList.get(0);
						if(leaveTypeCurrentMonth.getLeavesTaken()!=null)
							leaveTypeCurrentMonth.setLeavesTaken(leaveTypeCurrentMonth.getLeavesTaken()-leaveTransaction.getNumberOfDays());
						leaveTypeToBeUpdatedList.add(leaveTypeCurrentMonth);
						
						MonthlyLeaveReport leaveTypeYearlyTotal =	leaveReportList.get(1);
						if(leaveTypeYearlyTotal.getLeavesTaken()!=null)
							leaveTypeYearlyTotal.setLeavesTaken(leaveTypeYearlyTotal.getLeavesTaken()-leaveTransaction.getNumberOfDays());
						
						leaveTypeToBeUpdatedList.add(leaveTypeYearlyTotal);
					}
					else{
						MonthlyLeaveReport leaveTypeCurrentMonth =	leaveReportList.get(0);
						if(leaveTypeCurrentMonth.getLeavesTaken()!=null)
							leaveTypeCurrentMonth.setLeavesTaken(leaveTypeCurrentMonth.getLeavesTaken()-leaveTransaction.getNumberOfDays());
						leaveTypeCurrentMonth.setYearlyLeaveBalance(leaveTypeCurrentMonth.getYearlyLeaveBalance()-leaveTransaction.getNumberOfDays());
						leaveTypeToBeUpdatedList.add(leaveTypeCurrentMonth);
						
						MonthlyLeaveReport leaveTypeYearlyTotal =	leaveReportList.get(1);
						if(leaveTypeYearlyTotal.getLeavesTaken()!=null)
							leaveTypeYearlyTotal.setLeavesTaken(leaveTransaction.getNumberOfDays()+leaveTypeYearlyTotal.getLeavesTaken());
						if(leaveTypeYearlyTotal.getYearlyLeaveBalance()!=null)
							leaveTypeYearlyTotal.setYearlyLeaveBalance(leaveTypeYearlyTotal.getYearlyLeaveBalance()-leaveTransaction.getNumberOfDays());
							
						leaveTypeToBeUpdatedList.add(leaveTypeYearlyTotal);
					}
					monthlyLeaveRepository.save(leaveTypeToBeUpdatedList);
				}
			 }
			}catch(Exception e){
				System.out.println("Error while updating leaves balance after approval for employee : "+leaveTransaction.getEmployee().getName());
				e.printStackTrace();
			}
			

	}
	
}
