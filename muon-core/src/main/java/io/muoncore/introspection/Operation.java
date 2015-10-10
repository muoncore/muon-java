package io.muoncore.introspection;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class Operation {
	protected String resource;
	protected Class<?> type;

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}
	
	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}
	
	public static Collection<Class<?>> availableTypes() {
		List<Class<?>> l;
		
		l = new LinkedList<Class<?>>();
		l.add(Command.class);
		l.add(Query.class);
		l.add(Stream.class);
		
		return l;
	}
}
