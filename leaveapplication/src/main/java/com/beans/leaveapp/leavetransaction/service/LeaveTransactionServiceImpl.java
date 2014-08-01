package com.beans.leaveapp.leavetransaction.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import com.beans.leaveapp.employee.model.Employee;
import com.beans.leaveapp.employee.repository.EmployeeRepository;
import com.beans.leaveapp.leavetransaction.model.LeaveTransaction;
import com.beans.leaveapp.leavetransaction.repository.LeaveTransactionRepository;
import com.beans.leaveapp.leavetype.model.LeaveType;
import com.beans.leaveapp.leavetype.repository.LeaveTypeRepository;

public class LeaveTransactionServiceImpl implements LeaveTransactionService {

	@Resource
	LeaveTransactionRepository leaveTransactionRepository;

	@Resource
	LeaveTypeRepository leaveTypeRepository;

	@Resource
	EmployeeRepository employeeRepository;
	
	@Override
	public List<LeaveTransaction> findAll() {
		List<LeaveTransaction> ls = leaveTransactionRepository.findAll(0);
		System.out.println(ls.size());
		return ls;
	}


	@Override
	public List<String> findEmployeeNames() {
		List<String> employees = (List<String>) employeeRepository.findByNames();
				
		return employees;
	}

	@Override
	public List<String> findLeaveTypes(String name) {
		
		List<String> leaveTypeNames  = new LinkedList<String>();
		
		try{
			if(employeeRepository.findByEmployeeTypeId(name) != null){
		leaveTypeNames = leaveTypeRepository.findByLeaveTypes(this.employeeRepository.findByEmployeeTypeId(name));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return leaveTypeNames;
	}

	@Override
	public int create(LeaveTransaction leaveTransaction) {

		try {
			if (leaveTransaction != null) {
				leaveTransaction.setApplicationDate(new Date());
				LeaveTransaction leaveTransactionObj = leaveTransactionRepository.save(leaveTransaction);
				return leaveTransactionObj.getId();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void update(LeaveTransaction leaveTransaction) {
		try {
			if (leaveTransaction != null) {
				leaveTransactionRepository.save(leaveTransaction);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	

	@Override
	public void delete(int id) {
		try{
		LeaveTransaction leaveTransaction = leaveTransactionRepository
				.findOne(id);
		System.out.println(leaveTransaction.isDeleted());
		leaveTransaction.setDeleted(true);
		System.out.println(leaveTransaction.isDeleted());
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	@Override
	public Employee findByEmployee(String name) {
		Employee employee = employeeRepository.findByName(name);
		return employee;
	}


	@Override
	public LeaveType findByLeaveType(String name,int employeeId) {
		
		LeaveType leaveType = leaveTypeRepository.findByName(name,employeeId);
		return leaveType;
	}



	@Override
	public LeaveTransaction insertFromWorkflow(LeaveTransaction leaveTransaction) {
		
		return leaveTransactionRepository.save(leaveTransaction);
	}


	@Override
	public List<LeaveTransaction> findByStatus(String status) {
		String employeeStatus = "%"+status+"%";
		List<LeaveTransaction> leaveTransaction = leaveTransactionRepository.findByStatusLike(employeeStatus);
		return leaveTransaction;
	}


	@Override
	public List<LeaveTransaction> findByEmployeeORfindByLeaveTypeORLeaveDatesORStatusORAll(
			String employeeName, String leaveType, Date startDate, String status) {

		String name = "%"+ employeeName.trim()+"%";
		String leaveName = "%"+ leaveType.trim()+"%";
		String leaveStatus = "%"+ status.trim() +"%";
		java.sql.Date date = null ;
		if(startDate != null){
	     date = new java.sql.Date(startDate.getTime());
		}
		List<LeaveTransaction> leaveTransaction = null;
		
		 if(!employeeName.trim().equals("") && !leaveType.trim().equals("") && startDate != null && !status.trim().equals("")){
			 leaveTransaction =	leaveTransactionRepository.findByEmployeeAndLeaveTypeAndLeaveDatesAndStatusLike(name,leaveName,date,leaveStatus);
		    	   	
		    	
		    }else if(!employeeName.trim().equals("") && !leaveType.trim().equals("") && startDate != null){
		    	leaveTransaction = leaveTransactionRepository.findByEmployeeAndLeaveTypeAndStartDateLike(name, leaveName, date);
		    }
		 else if(!employeeName.trim().equals("") && !leaveType.trim().equals("") && !leaveStatus.trim().equals("")){
			 leaveTransaction = leaveTransactionRepository.findByEmployeeAndLeaveTypeAndStatusLike(name, leaveName,leaveStatus);
		    }
		 else if(!employeeName.trim().equals("") && !leaveType.trim().equals("")){
		  
		    	//leaveTransaction = leaveTransactionRepository.findByEmployeeLike(name,s);
		    	leaveTransaction =	leaveTransactionRepository.findByEmployeeAndLeaveTypeLike(name, leaveName);
		    	
		    }else if(!employeeName.trim().equals("") && !status.trim().equals(""))
		    {
		    	leaveTransaction = leaveTransactionRepository.findByEmployeeAndStatusLike(name, leaveStatus);
		    }else if(!leaveType.trim().equals("") && !status.trim().equals("")){
		    	leaveTransaction = leaveTransactionRepository.findByLeaveTypeAndStatusLike(leaveName, leaveStatus);
		    }else if(!employeeName.trim().equals("") && date != null){
		    	leaveTransaction = leaveTransactionRepository.findByEmployeeNameAndStartDate(name, date);
		    }else if(!leaveType.trim().equals("") && startDate != null){
		    	leaveTransaction = leaveTransactionRepository.findByLeaveTypeAndStartDate(leaveName,date);
		    }else if(startDate!= null && !status.trim().equals("")){
		    	leaveTransaction = leaveTransactionRepository.findByStartDateTimeAndStatusLike(date, leaveStatus);
		    }
		    else if(!employeeName.trim().equals("")){
		    	leaveTransaction = leaveTransactionRepository.findByEmployeeLike(name);
		    }else if(!leaveType.trim().equals("")){
		    	leaveTransaction = leaveTransactionRepository.findByLeaveTypeLike(leaveName);
		    }else if(!status.trim().equals("")){
		    	leaveTransaction = leaveTransactionRepository.findByStatusLike(leaveStatus);
		    }else if(date != null){
		    	leaveTransaction = leaveTransactionRepository.findByStartDateTime(date);
		    }
		 
			return leaveTransaction;
		
	}

	@Override
	public List<LeaveTransaction> findByEmployeeORfindByLeaveType(String employeeName, String leaveType) {

		String name = null;
		String leaveName =null;
		if(!employeeName.isEmpty())
			name = "%"+ employeeName.trim()+"%";
		if(!leaveType.isEmpty())
		leaveName =  "%"+ leaveType.trim()+"%";
		List<LeaveTransaction> leaveTransaction = null;
		
		 if(name==null && leaveName==null ){
			 leaveTransaction =	leaveTransactionRepository.findAllApprovedLeavesOfEmployees();

		 }else if(name==null && leaveName!=null){
		    	leaveTransaction = leaveTransactionRepository.findByLeaveTypeLike(leaveName);
		 }
		 else if(name!=null && leaveName==null){
			 leaveTransaction = leaveTransactionRepository.findByEmployeeLike(name);
		 }
		 else{
			 leaveTransaction=leaveTransactionRepository.findByEmployeeAndLeaveTypeLike(name, leaveName);
		 }
		 if(leaveTransaction==null)
			 leaveTransaction = new ArrayList<LeaveTransaction>();
		 
			return leaveTransaction;
		
	}

	
	@Override
	public LeaveTransaction updateLeaveApplicationStatus(LeaveTransaction leaveTransaction) {
	LeaveTransaction  leaveTransactionPersist =	leaveTransactionRepository.findOne(leaveTransaction.getId());
	leaveTransactionPersist.setStatus(leaveTransaction.getStatus());
	leaveTransactionPersist.setRejectReason(leaveTransaction.getRejectReason());
	leaveTransactionPersist.setLastModifiedBy(leaveTransaction.getLastModifiedBy());
	leaveTransactionPersist.setLastModifiedTime(leaveTransaction.getLastModifiedTime());
	leaveTransactionPersist.setYearlyLeaveBalance(leaveTransaction.getYearlyLeaveBalance());
	return leaveTransactionRepository.save(leaveTransactionPersist);
	
	}

	@Override
	public LeaveTransaction findById(int id) {
		LeaveTransaction leaveTransaction = leaveTransactionRepository.findById(id);
		return leaveTransaction;
	}
	public List<LeaveTransaction> getAllFutureLeavesAppliedByEmployee(int userId, java.sql.Date todayDate) {
		return leaveTransactionRepository.findAllFutureLeavesOfEmployee(userId, todayDate);
	}

	public List<LeaveTransaction> getAllApprovedLeavesAppliedByEmployee() {
		return leaveTransactionRepository.findAllApprovedLeavesOfEmployees();
	}


	@Override
	public List<LeaveTransaction> getAllLeavesAppliedByEmployee(int userId) {
		return leaveTransactionRepository.findAllLeavesHistoryOfEmployee(userId);
	}
	
}


