package io.muoncore.introspection;

import io.muoncore.crud.MuonService.MuonQueryListener;

import java.util.List;

public class Query extends Operation {
	private MuonQueryListener<?> listener;
    private List<String> parameters;

	public Query(String resource,
                 Class<?> type,
                 List<String> parameters,
                 MuonQueryListener<?> listener) {
        this.parameters = parameters;
		this.resource = resource;
		this.type = type;
		this.listener = listener;
	}

    public List<String> getParameters() {
        return parameters;
    }

    public MuonQueryListener<?> getListener() {
		return listener;
	}

	public void setListener(MuonQueryListener<?> listener) {
		this.listener = listener;
	}
}
