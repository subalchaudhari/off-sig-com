package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.validation.NotNull;

import play.db.ebean.Model;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "username" }))
public class Employee extends Model {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long id;
	
	@NotNull
	public String fname;
	
	@NotNull
	public String lname;
	
	@NotNull
	public String username;
	
	@NotNull
	public String password;
	
	@NotNull
	public String email;
	
	public int mobile;
	
	@NotNull
	public String role;
	
	public static Finder<Long, Employee> find=new Finder<>(Long.class, Employee.class);
	
	public static Employee get(Long id) {
		return find.byId(id);
	}

	public static List<Employee> all() {
		return find.all();
	}

	public static void delete(Long id) {
		find.byId(id).delete();
	}
	
	public static Employee authenticate(String username,String password){
		Employee emp=Ebean.find(Employee.class).where().eq("username", username).eq("password",	password).findUnique();
		return emp;
	}

}
