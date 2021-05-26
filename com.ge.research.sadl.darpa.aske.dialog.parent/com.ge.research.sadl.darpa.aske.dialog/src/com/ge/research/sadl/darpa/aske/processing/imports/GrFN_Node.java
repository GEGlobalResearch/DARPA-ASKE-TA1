/**
 * 
 */
package com.ge.research.sadl.darpa.aske.processing.imports;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author alfredo
 *
 */
public class GrFN_Node {

	/**
	 * 
	 */
	
    
	@SerializedName(value = "uid", alternate="func_node_uid") 
	private String uid = null; 
	private String description = null;
	private String identifier = null;
	private List<GrFN_Metadata> metadata = new ArrayList<GrFN_Metadata>();
	
	public GrFN_Node() {
		// TODO Auto-generated constructor stub
	}

	public void setUid(String uid) {
		if (uid.length() > 0 && Character.isDigit(uid.toCharArray()[0]) ) {
			uid = "_" + uid;
		}
		this.uid = uid;
	}
	public String getUid() {
		if (uid != null && uid.length() > 0 && Character.isDigit(uid.toCharArray()[0]) ) {
			uid = "_" + uid;
		}
		return uid;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public List<GrFN_Metadata> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<GrFN_Metadata> metadata) {
		this.metadata = metadata;
	}

}
