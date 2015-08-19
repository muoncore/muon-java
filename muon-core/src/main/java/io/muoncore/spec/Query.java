package io.muoncore.spec;

import io.muoncore.MuonService.MuonQuery;

import java.util.List;

public class Query extends Operation {
	private MuonQuery<?> listener;
    private List<String> parameters;

	public Query(String resource,
                 Class<?> type,
                 List<String> parameters,
                 MuonQuery<?> listener) {
        this.parameters = parameters;
		this.resource = resource;
		this.type = type;
		this.listener = listener;
	}

    public List<String> getParameters() {
        return parameters;
    }

    public MuonQuery<?> getListener() {
		return listener;
	}

	public void setListener(MuonQuery<?> listener) {
		this.listener = listener;
	}
}
