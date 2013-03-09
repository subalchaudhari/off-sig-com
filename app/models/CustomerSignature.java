package models;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Size;

import play.db.ebean.Model;

@Entity
public class CustomerSignature extends Model{

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long id;
	
	public String accno;
	
	public String imagename;
	
	@Column(name = "angles", nullable = false, length = 2000)
	public String angles;
}
