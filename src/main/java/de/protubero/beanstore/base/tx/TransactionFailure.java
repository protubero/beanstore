package de.protubero.beanstore.base.tx;

import de.protubero.beanstore.base.entity.BeanStoreException;
import de.protubero.beanstore.writer.TransactionElement;

public final class TransactionFailure extends BeanStoreException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8068520425192668938L;

	private TransactionFailureType type;
	private TransactionElement<?> elt;

	public TransactionFailure(TransactionFailureType type, TransactionElement<?> elt) {
		this.type = type;
		this.elt = elt;
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
		return elt.getCompanion().alias();
	}

	public TransactionFailureType getType() {
		return type;
	}

	public Long getId() {
		return elt.getId();
	}
}
