package de.protubero.beanstore.plugins.search;

import java.util.Objects;

public class SearchEngineAction {

	public enum Type {
		CREATE, DELETE, UPDATE;
	}

	private Type actionType;
	private String id;
	private String type;
	private String content;

	
	public SearchEngineAction(Type actionType, String id, String type) {
		this.actionType = Objects.requireNonNull(actionType);
		this.id = Objects.requireNonNull(id);
		
		this.type = Objects.requireNonNull(type);
	}
	
	public static SearchEngineAction create(String type, String id, String content) {
		SearchEngineAction action = new SearchEngineAction(SearchEngineAction.Type.CREATE, id, type);
		action.setContent(content);
		return action;
	}

	public static SearchEngineAction update(String type, String id, String content) {
		SearchEngineAction action = new SearchEngineAction(SearchEngineAction.Type.UPDATE, id, type);
		action.setContent(content);
		return action;
	}

	public static SearchEngineAction delete(String type, String id) {
		return new SearchEngineAction(SearchEngineAction.Type.DELETE, id, type);
	}

	public static SearchEngineAction create(String type, Long id, String content) {
		return create(type, String.valueOf(id), content);
	}

	public static SearchEngineAction update(String type, Long id, String content) {
		return update(type, String.valueOf(id), content);
	}

	public static SearchEngineAction delete(String type, Long id) {
		return delete(type, String.valueOf(id));
	}

	public static SearchEngineAction create(String type, long id, String content) {
		return create(type, String.valueOf(id), content);
	}

	public static SearchEngineAction update(String type, long id, String content) {
		return update(type, String.valueOf(id), content);
	}

	public static SearchEngineAction delete(String type, long id) {
		return delete(type, String.valueOf(id));
	}
	
	
	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}


	public Type getActionType() {
		return actionType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public String toString() {
		return actionType + " " + id + " (" + type + ")";
	}
	
}
