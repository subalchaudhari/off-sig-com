package forms;

import java.io.Serializable;

import javax.persistence.Lob;
import javax.validation.constraints.Pattern;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;

public class CustomerForm implements Serializable {
	
	@Required
	@MaxLength(value=25)
	public String fname;
	
	@Required
	@MaxLength(value=25)
	public String lname;
	
	@MaxLength(value=10)
	@MinLength(value=10)
	@Pattern(regexp="[0-9]*")
	@Required
	public String mobile;
	
	@Email
	public String email;
	
	@MaxLength(value=225)
	public String address;
	
	
	public String signatureOne;
	
	public String signatureTwo;
	

}
