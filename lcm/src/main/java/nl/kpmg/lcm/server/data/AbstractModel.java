package nl.kpmg.lcm.server.data;


/**
 * AbsractModel class for generic model parameters
 * @author venkateswarlub
 *
 */
public class AbstractModel { 
	
	private Integer id;
	
	private String name;
		
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	
	
}
