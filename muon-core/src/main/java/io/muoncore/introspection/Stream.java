package io.muoncore.introspection;

import io.muoncore.MuonStreamGenerator;

public class Stream extends Operation {
	private MuonStreamGenerator<?> generator;

	public Stream(String resource, Class<?> type, MuonStreamGenerator<?> generator) {
		this.resource = resource;
		this.type = type;
		this.generator = generator;
	}
	
	public MuonStreamGenerator<?> getGenerator() {
		return generator;
	}

	public void setListener(MuonStreamGenerator<?> generator) {
		this.generator = generator;
	}
}
