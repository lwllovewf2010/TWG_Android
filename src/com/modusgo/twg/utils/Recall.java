package com.modusgo.twg.utils;

import java.io.Serializable;

public class Recall implements Serializable {
	private static final long serialVersionUID = -5821923761857270708L;
	public long id;
	public String consequence;
	public String corrective_action;
	public String created_at;
	public String defect_description;
	public String description;
	public String recall_id;
	
	public Recall(long id, String consequence, String corrective_action,
			String created_at, String defect_description,
			String description, String recall_id) {
		super();
		this.id = id;
		this.consequence = consequence;
		this.corrective_action = corrective_action;
		this.created_at = created_at;
		this.defect_description = defect_description;
		this.description = description;
		this.recall_id = recall_id;
	}
}
