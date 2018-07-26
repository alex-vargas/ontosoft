package org.ontosoft.shared.classes.entities;

import org.ontosoft.shared.classes.permission.Permission;
import org.ontosoft.shared.classes.provenance.Provenance;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Model extends ComplexEntity {
	Provenance provenance;
	Permission permission;
	String modelName;

	@JsonIgnore
	boolean dirty;

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public Provenance getProvenance() {
		return provenance;
	}

	public void setProvenance(Provenance provenance) {
		this.provenance = provenance;
	}

	public Permission getPermission() {
		return permission;
	}

	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
	public String getModelName() {
		return modelName;
	}
}
