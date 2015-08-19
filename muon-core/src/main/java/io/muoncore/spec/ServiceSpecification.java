package io.muoncore.spec;

import io.muoncore.MuonService.MuonCommand;
import io.muoncore.MuonService.MuonQuery;
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
			final MuonCommand<?> listener) {
		ops.add(new Command(resource, type, listener));
	}
	
	public void addQuery(String resource, final Class<?> type,
			final MuonQuery<?> listener) {
		ops.add(new Query(resource, type, Arrays.asList("fakedforgood"), listener));
	}

	public Collection<Operation> getOperations() {
		return ops;
	}
}
