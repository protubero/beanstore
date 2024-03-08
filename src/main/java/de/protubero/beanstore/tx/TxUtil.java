package de.protubero.beanstore.tx;

import java.util.Objects;

import de.protubero.beanstore.persistence.api.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.api.PersistentProperty;
import de.protubero.beanstore.persistence.api.PersistentTransaction;

public class TxUtil {

	public static PersistentTransaction createPersistentTransaction(Transaction transaction) {
		PersistentTransaction pt = new PersistentTransaction(transaction.getTransactionType(), transaction.getMigrationId());
		pt.setDescription(transaction.getDescription());
		
		pt.setTimestamp(Objects.requireNonNull(transaction.getTimestamp()));
		if (transaction.getTargetStateVersion() == null) {
			throw new AssertionError();
		}
		pt.setSeqNum(transaction.getTargetStateVersion().intValue());
		
		PersistentInstanceTransaction[] eventArray = new PersistentInstanceTransaction[transaction.getInstanceEvents().size()];
		int idx = 0;
		for (InstanceTransactionEvent<?> event : transaction.getInstanceEvents()) {
			PersistentInstanceTransaction pit = new PersistentInstanceTransaction();
			eventArray[idx++] = pit;
			pit.setAlias(event.entity().alias());
			switch (event.type()) {
			case Delete:
				pit.setType(PersistentInstanceTransaction.TYPE_DELETE);
				pit.setId(event.replacedInstance().id());
				pit.setVersion(event.replacedInstance().version());
				break;
			case Update:
				pit.setType(PersistentInstanceTransaction.TYPE_UPDATE);
				pit.setId(event.newInstance().id());
				pit.setPropertyUpdates((PersistentProperty[]) event.values());
				pit.setVersion(event.newInstance().version());
				
				break;
			case Create:
				pit.setType(PersistentInstanceTransaction.TYPE_CREATE);
				pit.setId(event.newInstance().id());
				pit.setPropertyUpdates((PersistentProperty[]) event.values());
				pit.setVersion(event.newInstance().version());

				break;
			}
		}
		pt.setInstanceTransactions(eventArray);
		
		return pt;
	}
	
	
}
