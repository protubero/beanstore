package de.protubero.beanstore.model;

import de.protubero.beanstore.base.entity.AbstractTaggedEntity;
import de.protubero.beanstore.base.entity.Entity;

@Entity(alias = "note")
public class Note extends AbstractTaggedEntity {

	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
}
