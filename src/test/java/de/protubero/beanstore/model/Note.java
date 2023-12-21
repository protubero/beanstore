package de.protubero.beanstore.model;

import de.protubero.beanstore.base.entity.Entity;
import de.protubero.beanstore.plugins.tags.AbstractTaggedEntity;

@Entity(alias = "note")
public class Note extends AbstractTaggedEntity {

	private String text;
	private Priority priority;

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
	
}
