package de.protubero.beanstore.plugins.keyvalue;

import de.protubero.beanstore.entity.AbstractEntity;
import de.protubero.beanstore.entity.Entity;

@Entity(alias="keyvalue")
public class KeyValueEntity extends AbstractEntity {
	private KeyObject key;
	private Object value;

}
