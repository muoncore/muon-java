package io.muoncore.introspection;

import io.muoncore.crud.MuonService.MuonCommandListener;

public class Command extends Operation {
	private MuonCommandListener<?> listener;

	public Command(String resource, Class<?> type, MuonCommandListener<?> listener) {
		this.resource = resource;
		this.type = type;
		this.listener = listener;
	}
	
	public MuonCommandListener<?> getListener() {
		return listener;
	}

	public void setListener(MuonCommandListener<?> listener) {
		this.listener = listener;
	}
}
