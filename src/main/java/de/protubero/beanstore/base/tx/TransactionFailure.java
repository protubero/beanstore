package de.protubero.beanstore.base.tx;

import de.protubero.beanstore.base.entity.BeanStoreException;

public final class TransactionFailure extends BeanStoreException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8068520425192668938L;

	private TransactionFailureType type;
	private String alias;
	private Long id;

	public TransactionFailure(TransactionFailureType type, String alias, Long id) {
		this.type = type;
		this.alias = alias;
		this.id = id;
	}

	public TransactionFailure(TransactionFailureType type, Exception e) {
		super(e);
		
		if (type != TransactionFailureType.VERIFICATION_FAILED && type != TransactionFailureType.PERSISTENCE_FAILED) {
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

	public TransactionFailureType getType() {
		return type;
	}

	public Long getId() {
		return id;
	}
}
