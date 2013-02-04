package models;

import java.util.List;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.avaje.ebean.validation.NotNull;

import play.db.ebean.Model;

@Entity
@JsonSerialize(include = Inclusion.NON_NULL)
public class Customer extends Model {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long id;
	
	@NotNull
	public String fname;
	
	@NotNull
	public String lname;
	
	public String accountNumber;
	
	public  String address;
	
	public String mobile;
	
	public String email;	
	
	@Lob
	//@NotNull
	public byte[] image;
	
	public static Finder<Long, Customer> find=new Finder<>(Long.class, Customer.class);
	
	public static Customer get(Long id) {
		return find.byId(id);
	}

	public static List<Customer> all() {
		return find.all();
	}

	public static void delete(Long id) {
		find.byId(id).delete();
	}
	

}
