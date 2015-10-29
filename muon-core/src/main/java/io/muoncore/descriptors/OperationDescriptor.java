package io.muoncore.descriptors;

public class OperationDescriptor {

	private String resource;

    public OperationDescriptor(String resource) {
        this.resource = resource;
    }

    public String getResource() {
		return resource;
	}
}
