package com.modusgo.ubi;

import java.io.Serializable;

public class DiagnosticsTroubleCode implements Serializable{
	private static final long serialVersionUID = -8572329924127262115L;
	
	String code;
	String conditions;
	String created_at;
	String description;
	String details;
	String full_description;
	String importance;
    String labor_cost;
	String labor_hours;
	String parts;
    String parts_cost;
	String total_cost;
	
	public DiagnosticsTroubleCode(String code, String conditions,
			String created_at, String description, String details,
			String full_description, String importance, String labor_cost,
			String labor_hours, String parts, String parts_cost,
			String total_cost) {
		super();
		this.code = code;
		this.conditions = conditions;
		this.created_at = created_at;
		this.description = description;
		this.details = details;
		this.full_description = full_description;
		this.importance = importance;
		this.labor_cost = labor_cost;
		this.labor_hours = labor_hours;
		this.parts = parts;
		this.parts_cost = parts_cost;
		this.total_cost = total_cost;
	}
	
}
