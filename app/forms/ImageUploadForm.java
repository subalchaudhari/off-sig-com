package forms;

import java.io.Serializable;

import play.data.validation.Constraints.Required;

public class ImageUploadForm implements Serializable{
	
	@Required
	public String image;
	
	public String accountNo;

}
