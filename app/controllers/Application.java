package controllers;

import models.Employee;
import forms.LoginForm;
import play.*;
import play.data.Form;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {
	
	final static Form<LoginForm> loginForm=Form.form(LoginForm.class);
  
  public static Result index() {
    return ok(index.render(loginForm));
  }
  
  public static Result login(){
	  Form<LoginForm> filledForm=loginForm.bindFromRequest();
	  //return ok(index.re);
	 if(filledForm.hasErrors()){
			return badRequest(index.render(filledForm));
		}
					
		LoginForm loginForm=filledForm.get();
		if(loginForm==null){			
			return badRequest(index.render(filledForm));
		}
		
		Employee emp=loginForm.getEmployee();
		if(emp==null){
			return badRequest(index.render(filledForm));
		}
		
		session().put("username", emp.username);
		session().put("role", emp.role);
		return  redirect(controllers.routes.Welcome.index());
  }
  
  public static Result logout(){
		session().clear();
		return  redirect(controllers.routes.Application.index());
	}
  
}