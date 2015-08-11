package io.muoncore.spec;

import io.muoncore.MuonService.MuonQuery;

public class Query extends Operation {
	private MuonQuery<?> listener;

	public Query(String resource, Class<?> type, MuonQuery<?> listener) {
		this.resource = resource;
		this.type = type;
		this.listener = listener;
	}
	
	public MuonQuery<?> getListener() {
		return listener;
	}

	public void setListener(MuonQuery<?> listener) {
		this.listener = listener;
	}
}
