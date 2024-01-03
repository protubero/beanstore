package de.protubero.beanstore.model;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.Entity;

@Entity(alias = "note")
public class Note extends AbstractEntity {

	private String text;
	private Priority priority;
	private transient String hint;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public String getHint() {
		return hint;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}
	
}
