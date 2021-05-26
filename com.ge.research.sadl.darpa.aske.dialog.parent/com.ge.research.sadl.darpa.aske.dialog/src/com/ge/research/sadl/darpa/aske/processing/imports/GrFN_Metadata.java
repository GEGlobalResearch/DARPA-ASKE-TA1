/**
 * 
 */
package com.ge.research.sadl.darpa.aske.processing.imports;

import com.google.gson.annotations.SerializedName;

/**
 * @author alfredo
 *
 */
public class GrFN_Metadata {
/**
 * 
 */
//	@SerializedName(value = "uid", alternate = "func_node_uid")
	private String type;
	private CodeSpan code_span;
//	private int[] line; 

	class CodeSpan{
		private int line_begin;
		private int line_end;
		private int col_begin;
		private int col_end;

		public int getLine_begin() {
			return line_begin;
		}
		public void setLine_begin(int line_begin) {
			this.line_begin = line_begin;
		}
		public int getLine_end() {
			return line_end;
		}
		public void setLine_end(int line_end) {
			this.line_end = line_end;
		}
		public int getCol_begin() {
			return col_begin;
		}
		public void setCol_begin(int col_begin) {
			this.col_begin = col_begin;
		}
		public int getCol_end() {
			return col_end;
		}
		public void setCol_end(int col_end) {
			this.col_end = col_end;
		}
	}
	
	public GrFN_Metadata() {
//		super();
//		this.line = new int[2];
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public CodeSpan getCode_span() {
		return code_span;
	}
	public void setCode_span(CodeSpan code_span) {
		this.code_span = code_span;
	}
//	public int[] getLine() {
//		return line;
//	}
//	public void setLine(int[] line) {
//		this.line = line;
//	}

}
