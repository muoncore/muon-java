package io.muoncore.introspection;

import io.muoncore.crud.MuonService.MuonCommandListener;
import io.muoncore.crud.MuonService.MuonQueryListener;
import io.muoncore.MuonStreamGenerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ServiceSpecification {
	private Set<Operation> ops;
	
	public ServiceSpecification() {
		ops = new HashSet<Operation>();
	}
	
	public void addStream(String resource, final Class<?> type,
			final MuonStreamGenerator<?> generator) {
		ops.add(new Stream(resource, type, generator));
	}

	public void addCommand(String resource, final Class<?> type,
			final MuonCommandListener<?> listener) {
		ops.add(new Command(resource, type, listener));
	}
	
	public void addQuery(String resource, final Class<?> type,
			final MuonQueryListener<?> listener) {
		ops.add(new Query(resource, type, Arrays.asList("fakedforgood"), listener));
	}

	public Collection<Operation> getOperations() {
		return ops;
	}
}
