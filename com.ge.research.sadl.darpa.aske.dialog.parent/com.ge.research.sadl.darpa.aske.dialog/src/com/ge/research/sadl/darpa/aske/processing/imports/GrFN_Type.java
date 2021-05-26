/**
 * 
 */
package com.ge.research.sadl.darpa.aske.processing.imports;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alfredo
 *
 */
public class GrFN_Type extends GrFN_Node {

	/**
	 * 
	 */
	private String name;
	private String data_type;
	private String metatype;
	private List<Field> fields = new ArrayList<Field>(); 

	
	public class Field {
		private String name;
		private String type;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}
	
	public GrFN_Type() {
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getData_type() {
		return data_type;
	}

	public void setData_type(String data_type) {
		this.data_type = data_type;
	}

	public String getMetatype() {
		return metatype;
	}

	public void setMetatype(String metatype) {
		this.metatype = metatype;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

}
