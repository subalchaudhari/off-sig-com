package controllers;

import java.util.List;

import models.Customer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import forms.CustomerForm;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.customer.create;
import views.html.customer.index;
import views.html.customer.update;

public class CustomerController extends Controller {
	
	final static Form<CustomerForm> customerForm = form(CustomerForm.class);
	public static int min=1000;
	public static int max=9999;
	
	public static Result index(){
		return ok(index.render());
	}

	public static Result create_new(){		
		return ok(create.render(customerForm));
	}

	public static Result create_save(){
		Form<CustomerForm> filledForm = customerForm.bindFromRequest();

		if(filledForm.hasErrors()){			
			return badRequest(create.render(filledForm));
		}
					
		CustomerForm customerForm=filledForm.get();
		if(customerForm==null){						
			return badRequest(create.render(filledForm));
		}

		

		Customer customer=new Customer();
		formToModel(customer,customerForm);
				
		customer.save();
								
		return  redirect(controllers.routes.CustomerController.index());
	}

	
	public static Result update_get(Long id){		
		Customer customer=Customer.get(id);

		CustomerForm csForm=new CustomerForm();
		csForm.fname=customer.fname;
		//id=customer.id;		
		csForm.lname=customer.lname;
		csForm.address=customer.address;
		csForm.mobile=customer.mobile;
		csForm.email=customer.email;		
		
		return ok(update.render(customerForm.fill(csForm),id));
	}

	public static Result update_save(Long id){
		Form<CustomerForm> filledForm = customerForm.bindFromRequest();

		if(filledForm.hasErrors()){			
			return badRequest(update.render(filledForm,id));
		}
					
		CustomerForm customerForm=filledForm.get();
		if(customerForm==null){						
			return badRequest(update.render(filledForm,id));
		}		

		Customer customer=Customer.get(id);

		if(customer==null){			
			filledForm.reject(Messages.get("id.not.exists",id));
			return badRequest(update.render(filledForm,id));	
		}

		

		formToModel(customer,customerForm);	
		customer.update();
								
		return  redirect(controllers.routes.CustomerController.index());
	}

	private static void formToModel(Customer customer,CustomerForm customerForm){
		
		customer.fname=customerForm.fname;
		customer.lname=customerForm.lname;
		customer.address=customerForm.address;
		customer.mobile=customerForm.mobile;
		customer.email=customerForm.email;
		customer.image=customerForm.image;
		int accno=min + (int)(Math.random() * ((max - min) + 1));
		customer.accountNumber=customerForm.fname.substring(0, 3).toUpperCase()+String.valueOf(accno);	
	}

	public static Result delete(Long id){		
		Customer customer=Customer.get(id);
		if(customer==null){
			return notFound(Messages.get("id.not.exists",id));
		}
			Customer.delete(id);
			return ok(Messages.get("record.deleted.succ"));
			
	}	

	/**
	 * Return List of all Customers in JSON format.
	 * 
	 * @return
	 */
	public static Result all() {
		List<Customer> customers = Customer.all();
		return ok(Json.toJson(customers));
	}		

	/**
	 * Return Customer by id.
	 * 
	 * @return
	 */
	public static Result get(Long id) {
		if (id == null) {
			return badRequest(Messages.get("id.expecting"));
		}

		Customer customer = Customer.get(id);

		if (customer == null) {
			return notFound(Messages.get("id.not.exists",id));
		}
		JsonNode result = Json.toJson(customer);
		return ok(result);
	}

	/**
	 * @return
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public static Result create() {
		JsonNode jsonNode = request().body().asJson();
		if (jsonNode == null) {
			return badRequest(Messages.get("json.expecting"));
		}
		Customer customer = Json.fromJson(jsonNode, Customer.class);
		if (customer == null) {
			return badRequest(Messages.get("json.expecting"));
		}
		customer.save();
		ObjectNode result = Json.newObject();
		result.put("id", customer.id);
		return created(result);
	}

	/**
	 * @param id
	 * @return
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public static Result update(Long id) {
		if (id == null) {
			return badRequest(Messages.get("id.expecting"));
		}

		JsonNode jsonNode = request().body().asJson();
		if (jsonNode == null) {
			return badRequest(Messages.get("json.expecting"));
		}
		Customer customer = Json.fromJson(jsonNode, Customer.class);
		if (customer == null) {
			return badRequest(Messages.get("json.expecting"));
		}
		customer.id = id;
		customer.update();
		return ok();
	}
	
	


}
