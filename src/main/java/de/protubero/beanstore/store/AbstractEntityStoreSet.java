package de.protubero.beanstore.store;

import java.util.ArrayList;
import java.util.List;

import de.protubero.beanstore.entity.AbstractPersistentObject;
import de.protubero.beanstore.linksandlabels.Link;
import de.protubero.beanstore.linksandlabels.LinkValue;
import de.protubero.beanstore.linksandlabels.Links;
import de.protubero.beanstore.linksandlabels.LinksImpl;

public abstract class AbstractEntityStoreSet<E extends EntityStore<?>> implements EntityStoreSet<E>  {

	private LinksImpl links = new LinksImpl();
	
	
	public Links links() {
		return links;
	}
	
	public void reloadLinks() {
		List<Link<?, ?>> linkList = new ArrayList<>();
		
		for (E store : this) {
			for (AbstractPersistentObject apo : store) {
				if (apo.getLinks() != null) {
					apo.getLinks().forEach(linkValue -> {
						linkList.add(linkValue2Link(apo, linkValue));
					});
				}
			}
		}
		
		links = new LinksImpl(linkList);
	}

	private Link<?, ?> linkValue2Link(AbstractPersistentObject source, LinkValue linkValue) {
		AbstractPersistentObject target = get(linkValue.getKey());
		if (target == null)  {
			throw new RuntimeException("Invalid link target: " + linkValue.getKey());
		}
		return new Link<>(source, target, linkValue.getType());
	}
	
	
	
}
