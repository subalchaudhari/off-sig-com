package forms;

import java.io.Serializable;

import play.data.validation.Constraints.Required;

import models.Employee;

public class LoginForm implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Required
	public String username;
	
	@Required
	public String password;
	
	private Employee employee;
	
	public String validate(){
		Employee emp=Employee.authenticate(username, password);
		if(emp==null){
			return "Invalid username and password";
		}
		employee=emp;
		return null;
	}
	
	public Employee getEmployee(){
		return employee;
	}

}
