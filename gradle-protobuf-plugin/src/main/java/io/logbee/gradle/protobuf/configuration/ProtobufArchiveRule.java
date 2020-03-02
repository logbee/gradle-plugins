package io.logbee.gradle.protobuf.configuration;

import org.gradle.api.attributes.AttributeCompatibilityRule;
import org.gradle.api.attributes.CompatibilityCheckDetails;

import static io.logbee.gradle.protobuf.configuration.ProtobufElements.PROTOBUF_SOURCES;


public class ProtobufArchiveRule implements AttributeCompatibilityRule<ProtobufElements> {

    @Override
    public void execute(CompatibilityCheckDetails<ProtobufElements> details) {

        if (PROTOBUF_SOURCES.equals(details.getConsumerValue().getName()) && "jar".equals(details.getProducerValue().getName())) {
            details.compatible();
        }
    }
}
