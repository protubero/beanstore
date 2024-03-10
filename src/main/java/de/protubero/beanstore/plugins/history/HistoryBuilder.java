package de.protubero.beanstore.plugins.history;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import de.protubero.beanstore.persistence.api.KeyValuePair;
import de.protubero.beanstore.persistence.api.PersistentInstanceTransaction;

public class HistoryBuilder implements Consumer<InstanceChange> {

	private List<InstanceState> states = new ArrayList<>();
	private Map<String, Object> instanceState = new HashMap<>();

	@Override
	public void accept(InstanceChange ic) {
		if (ic.getChangeType() == PersistentInstanceTransaction.TYPE_DELETE) {
			instanceState = null;
		} else {
			if (ic.getPropertyChanges() != null) {
				for (KeyValuePair kvp : ic.getPropertyChanges()) {
					if (kvp.getValue() == null) {
						instanceState.remove(kvp.getProperty());
					} else {
						instanceState.put(kvp.getProperty(), kvp.getValue());
					}
				}
			}
		}
		
		InstanceState state = new InstanceState(instanceState, ic);
		if (instanceState != null) {
			Map<String, Object> newInstanceState = new HashMap<>();
			newInstanceState.putAll(instanceState);
			instanceState = newInstanceState;
		}
		states.add(state);
	}

	public List<InstanceState> getStates() {
		return states;
	}
	
	

}
