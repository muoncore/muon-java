package io.muoncore.spec;

import io.muoncore.MuonService.MuonCommand;

public class Command extends Operation {
	private MuonCommand<?> listener;

	public Command(String resource, Class<?> type, MuonCommand<?> listener) {
		this.resource = resource;
		this.type = type;
		this.listener = listener;
	}
	
	public MuonCommand<?> getListener() {
		return listener;
	}

	public void setListener(MuonCommand<?> listener) {
		this.listener = listener;
	}
}
