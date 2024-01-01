package de.protubero.beanstore.plugins.txlog;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.protubero.beanstore.persistence.api.PersistentInstanceTransaction;
import de.protubero.beanstore.persistence.api.PersistentTransaction;
import de.protubero.beanstore.pluginapi.BeanStorePlugin;
import de.protubero.beanstore.pluginapi.PersistenceReadListener;
import de.protubero.beanstore.pluginapi.PersistenceWriteListener;

/**
 * Logs transaction read and write operations to the SLF4J logger. 
 *
 */
public class BeanStoreTransactionLogPlugin implements BeanStorePlugin, PersistenceReadListener, PersistenceWriteListener {

	public static final Logger log = LoggerFactory.getLogger(BeanStoreTransactionLogPlugin.class);
	
	@Override
	public void onReadTransaction(PersistentTransaction transaction) {
		log.info("READ TX " + oneLine(transaction));
	}

	public static String oneLine(PersistentTransaction transaction) {
		StringBuilder sb = new StringBuilder();
		
		if (transaction.getTransactionType() == PersistentTransaction.TRANSACTION_TYPE_MIGRATION) {
			sb.append("* ");
		}	
		if (transaction.getTransactionId() != null) {
			sb.append("#" + transaction.getTransactionId());
		}	

		if (transaction.getInstanceTransactions() != null) {
			int count = 0;
			for (var tx : transaction.getInstanceTransactions()) {
				if (count > 0) {
					sb.append(", ");
				}
				sb.append(typeToString(tx.getType()) + " " + tx.getAlias() + "[" + tx.getId() + "]");
				count++;
			}
		}
		
		return sb.toString();
	}

	private static String typeToString(int type) {
		switch (type) {
			case PersistentInstanceTransaction.TYPE_CREATE:
				return "create";
			case PersistentInstanceTransaction.TYPE_DELETE:
				return "delete";
			case PersistentInstanceTransaction.TYPE_UPDATE:
				return "update";
			default:	
				return "unknown";
		}	
	}

	@Override
	public void onWriteTransaction(PersistentTransaction transaction) {
		log.info("WRITE TX " + oneLine(transaction));
	}

	
}
