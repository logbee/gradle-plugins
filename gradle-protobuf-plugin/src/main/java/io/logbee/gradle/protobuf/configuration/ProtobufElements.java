package io.logbee.gradle.protobuf.configuration;

import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

public interface ProtobufElements extends Named {

    Attribute<ProtobufElements> PROTOBUF_ATTRIBUTE = Attribute.of("io.logbee.gradle.protobuf.configuration.ProtobufElements", ProtobufElements.class);

    String PROTOBUF_SOURCES = "protobuf_sources";
}
