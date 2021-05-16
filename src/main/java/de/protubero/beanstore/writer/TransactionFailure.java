package de.protubero.beanstore.writer;

public final class TransactionFailure extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8068520425192668938L;

	public static enum Type {
		INSTANCE_NOT_FOUND, 		
		OPTIMISTIC_LOCKING_FAILED, 		
		VERIFICATION_FAILED, 		
		PERSISTENCE_FAILED, 		
	}
	
	private Type type;
	private String alias;
	private Long id;

	public TransactionFailure(Type type, String alias, Long id) {
		this.type = type;
		this.alias = alias;
		this.id = id;
	}

	public TransactionFailure(Type type, Exception e) {
		super(e);
		
		if (type != Type.VERIFICATION_FAILED && type != Type.PERSISTENCE_FAILED) {
			throw new AssertionError();
		}
		
		this.type = type;
	}
	

	@Override
	public String getMessage() {
		return super.getMessage();
	}

	public String getAlias() {
		return alias;
	}

	public Type getType() {
		return type;
	}

	public Long getId() {
		return id;
	}
}
