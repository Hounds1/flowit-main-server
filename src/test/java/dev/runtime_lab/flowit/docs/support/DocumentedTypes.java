package dev.runtime_lab.flowit.docs.support;

import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.RequestPartDescriptor;
import org.springframework.restdocs.snippet.Attributes.Attribute;

import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.snippet.Attributes.key;

public final class DocumentedTypes {

	private static final String BOOLEAN = "Boolean";
	private static final String FILE = "File";
	private static final String NUMBER = "Number";
	private static final String STRING = "String";

	private DocumentedTypes() {
	}

	public static ParameterDescriptor booleanParameter(String name) {
		return parameter(name, BOOLEAN);
	}

	public static RequestPartDescriptor filePart(String name) {
		return partWithName(name).attributes(type(FILE));
	}

	public static ParameterDescriptor numberParameter(String name) {
		return parameter(name, NUMBER);
	}

	public static ParameterDescriptor stringParameter(String name) {
		return parameter(name, STRING);
	}

	public static Attribute[] stringArrayElements() {
		return arrayElements(STRING);
	}

	private static Attribute[] arrayElements(String elementType) {
		return new Attribute[] {
			key("elementType").value(elementType)
		};
	}

	private static ParameterDescriptor parameter(String name, String type) {
		return parameterWithName(name).attributes(type(type));
	}

	private static Attribute[] type(String type) {
		return new Attribute[] {
			key("type").value(type)
		};
	}
}
