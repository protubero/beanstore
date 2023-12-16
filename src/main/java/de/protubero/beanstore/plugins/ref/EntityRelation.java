package de.protubero.beanstore.plugins.ref;

import de.protubero.beanstore.base.entity.AbstractPersistentObject;
import de.protubero.beanstore.base.entity.Entity;
import de.protubero.beanstore.plugins.tags.AbstractTaggedEntity;

@Entity(alias="relation")
public class EntityRelation extends AbstractTaggedEntity {

	private String sourceAlias;
	private String targetAlias;
	private long sourceId;
	private long targetId;
	private AbstractPersistentObject source;
	private AbstractPersistentObject target;
	
	public String getSourceAlias() {
		return sourceAlias;
	}
	public void setSourceAlias(String sourceAlias) {
		this.sourceAlias = sourceAlias;
	}
	public String getTargetAlias() {
		return targetAlias;
	}
	public void setTargetAlias(String targetAlias) {
		this.targetAlias = targetAlias;
	}
	public AbstractPersistentObject getSource() {
		return source;
	}
	public void setSource(AbstractPersistentObject source) {
		this.source = source;
	}
	public AbstractPersistentObject getTarget() {
		return target;
	}
	public void setTarget(AbstractPersistentObject target) {
		this.target = target;
	}
	public long getSourceId() {
		return sourceId;
	}
	public void setSourceId(long sourceId) {
		this.sourceId = sourceId;
	}
	public long getTargetId() {
		return targetId;
	}
	public void setTargetId(long targetId) {
		this.targetId = targetId;
	}
	
}
