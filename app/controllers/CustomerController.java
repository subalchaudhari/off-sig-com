package controllers;

import java.io.File;
import java.util.List;
import java.util.Map;

import models.Customer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import forms.CustomerForm;
import play.data.DynamicForm;
import play.data.DynamicForm.Dynamic;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import views.html.customer.create;
import views.html.customer.index;
import views.html.customer.update;
import play.mvc.Http.*;

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
			System.out.println("in error");
			return badRequest(create.render(filledForm));
		}
		
		CustomerForm customerForm=filledForm.get();
		if(customerForm==null){			
			System.out.println("in form");
			return badRequest(create.render(filledForm));
		}

		

		Customer customer=new Customer();
		formToModel(customer,customerForm);
		MultipartFormData body = request().body().asMultipartFormData();
		  FilePart picture1 = body.getFile("signatureOne");
		  FilePart picture2=body.getFile("signatureTwo");
		  if (picture1 != null && picture2!=null) {
		    File file1 = picture1.getFile();
		    File file2= picture2.getFile();
		    String path="public/SignatureImages/"+customer.accountNumber;
		    File f=new File(path);
		    f.mkdir();
		    file1.renameTo(new File(path,"signatureOne.jpg"));
		    file2.renameTo(new File(path,"signatureTwo.jpg"));
		    System.out.println("File uploaded successfully...");
		  } else {
		    filledForm.reject("file","Please select the image to uplaod");    
		  }

		
		/*String path="public/SignatureImages";
		MultipartFormData body=request().body().asMultipartFormData();
		FilePart sign1=body.getFile("signatureOne");
		FilePart sign2=body.getFile("signatureTwo");
		System.out.println("file 1: "+sign1.getFilename()+" content type: "+sign1.getContentType());
		System.out.println("file 2: "+sign2.getFilename()+" content type: "+sign2.getContentType());
		File img1=sign1.getFile();
		File img2=sign2.getFile();
		img1.renameTo(new File(path,customer.signatureOne+".jpg"));
		img2.renameTo(new File(path,customer.signatureTwo+".jpg"));
		System.out.println("file uploded...successfully");
		*/		
		customer.save();
		String inputFile="public/SignatureImages/"+customer.accountNumber+"/signatureOne.jpg";
		String smoothfilename="public/SignatureImages/"+customer.accountNumber+"/signatureOne_smooth.jpg";
		String binaryfilename="public/SignatureImages/"+customer.accountNumber+"/signatureOne_binary.jpg";
		String sizeNormalizeFileName="public/SignatureImages/"+customer.accountNumber+"/signatureOne_normalize.jpg";
		SigImgProcessingController.smoothing(inputFile, smoothfilename);
		SigImgProcessingController.binarization(smoothfilename, binaryfilename);
		SigImgProcessingController.sizeNormalization(binaryfilename, sizeNormalizeFileName);		
		float[] sig1=SigImgProcessingController.calculateAngles(sizeNormalizeFileName);
		sig1.toString();
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
		
		//to genrate random account number
		int accno=min + (int)(Math.random() * ((max - min) + 1));
		String accountNumber=customerForm.fname.substring(0, 3).toUpperCase()+String.valueOf(accno);
		customer.accountNumber=accountNumber;
		customer.signatureOne=accountNumber+"_1.jpg";
		customer.signatureTwo=accountNumber+"_2.jpg";
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
