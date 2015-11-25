package io.muoncore.descriptors;

public class OperationDescriptor {

	private String resource;
    private String doc;

    public OperationDescriptor(String resource) {
        this.resource = resource;
    }

    public OperationDescriptor(String resource, String doc) {
        this.resource = resource;
        this.doc = doc;
    }

    public String getDoc() {
        return doc;
    }

    public String getResource() {
		return resource;
	}
}
