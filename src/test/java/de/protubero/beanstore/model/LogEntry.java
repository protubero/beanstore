package de.protubero.beanstore.model;

import de.protubero.beanstore.base.entity.AbstractEntity;
import de.protubero.beanstore.base.entity.Entity;

@Entity(alias = "logentry")
public class LogEntry extends AbstractEntity {

	private LogLevel logLevel;

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}
	
}
