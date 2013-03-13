package controllers;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import models.Customer;
import models.CustomerSignature;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import forms.CustomerForm;
import forms.ImageUploadForm;
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
import views.html.customer.verify;
import play.mvc.Http.*;

public class CustomerController extends Controller {
	
	final static Form<CustomerForm> customerForm = Form.form(CustomerForm.class);
	final static Form<ImageUploadForm> imageForm = Form.form(ImageUploadForm.class);
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
		  FilePart picture1 =body.getFile("signatureOne");
		  FilePart picture2=body.getFile("signatureTwo");
		  if (picture1 != null && picture2!= null) {
		    File file1 = picture1.getFile();
		    File file2= picture2.getFile();
		    String path="public/signatureimages/"+customer.accountNumber;
		    File f=new File(path);
		    f.mkdir();
		    file1.renameTo(new File(path,customer.signatureOne));
		    file2.renameTo(new File(path,customer.signatureTwo));
		    System.out.println("File uploaded successfully...");
		  } else {
		    filledForm.reject("file","Please select the image to uplaod");    
		  }
				
		
		String inputFile="public/signatureimages/"+customer.accountNumber+"/"+customer.signatureOne;
		String smoothfilename="public/signatureimages/"+customer.accountNumber+"/signatureOne_smooth.jpg";
		String binaryfilename="public/signatureimages/"+customer.accountNumber+"/signatureOne_binary.jpg";
		String sizeNormalizeFileName="public/signatureimages/"+customer.accountNumber+"/signatureOne_normalize.jpg";
		SigImgProcessingController.smoothing(inputFile, smoothfilename);
		SigImgProcessingController.binarization(smoothfilename, binaryfilename);
		SigImgProcessingController.sizeNormalization(binaryfilename, sizeNormalizeFileName);		
		float[] sig1=SigImgProcessingController.calculateAngles(sizeNormalizeFileName);
		StringBuffer angels=new StringBuffer();
		
		for(int i=0;i<sig1.length;i++){
			angels.append(sig1[i]+"|");
		}
		System.out.println("Angles: "+angels);
		customer.signOneAngles=angels.toString();
		
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
	
	public static Result verify_get(Long id){
		Customer customer=Customer.get(id);
		ImageUploadForm imgform=new ImageUploadForm();
		imgform.accountNo=customer.accountNumber;
		String accno=customer.accountNumber;
		return ok(verify.render(imageForm.fill(imgform),accno));
	}
	
	public static Result upload_image(String accountNo){
		Form<ImageUploadForm> filledForm = imageForm.bindFromRequest();
		MultipartFormData body = request().body().asMultipartFormData();
		  FilePart picture1 = body.getFile("image");
		  if (picture1 != null) {
		    File file1 = picture1.getFile();
		    String pathToDelete="public/signatureimages/"+accountNo+"/"+accountNo+"_verify.jpg";
		    
		    File f=new File(pathToDelete);
		   boolean b= f.delete();
		    System.out.println("file deleted"+b);
		    String path="public/signatureimages/"+accountNo;
		    System.out.println("path:"+path);
		    file1.renameTo(new File(path,accountNo+"_verify.jpg"));
		    System.out.println("File uploaded for compare successfully...");
		  } else {
			  filledForm.reject("file","Please select the image to uplaod");
		        
		  }
		  response().setHeader("Cache-Control", "no-cache");
		  return ok(verify.render(imageForm,accountNo));
		
	}
	
		
	public static Result verifySignature(String accno){
		Customer cust=Customer.byAccno(accno);
		float[] src=new float[96];
		String angles=cust.signOneAngles;		
		StringTokenizer st= new StringTokenizer(angles, "|");
		int tokenCount=st.countTokens();
			for(int i=0;i<tokenCount;i++){
			String token=st.nextToken();
			float angle=Float.parseFloat(token);
			src[i]=angle;
			}
		
			String inputFile="public/signatureimages/"+accno+"/"+accno+"_verify.jpg";
			String smoothfilename="public/signatureimages/"+accno+"/"+accno+"_verify_smooth.jpg";
			String binaryfilename="public/signatureimages/"+accno+"/"+accno+"_verify_binary.jpg";
			String sizeNormalizeFileName="public/signatureimages/"+accno+"/"+accno+"_verify_normalize.jpg";
			SigImgProcessingController.smoothing(inputFile, smoothfilename);
			SigImgProcessingController.binarization(smoothfilename, binaryfilename);
			SigImgProcessingController.sizeNormalization(binaryfilename, sizeNormalizeFileName);		
			float[] trg=SigImgProcessingController.calculateAngles(sizeNormalizeFileName);
			
			if(SigImgProcessingController.compareBlockAngles(src, trg)){
				return ok("Signatures Matched");
			}else{
				return ok("Signatures Not Matched");
				
			}
			
		
	}
	
	}

