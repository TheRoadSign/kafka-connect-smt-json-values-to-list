package com.kafka.connect.transforms;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.transforms.Transformation;

import java.util.*;

/**
 * JsonValuesToList SMT: Transforms the values of a specified JSON object field into a list.
 */
public class JsonValuesToList<R extends ConnectRecord<R>> implements Transformation<R> {

    private static final String PURPOSE = "Transform a specified JSON field's object values into a list";

    public static final String FIELD_NAME_CONFIG = "field.name"; // Config key for specifying the target field
    private String fieldName;

    @Override
    public void configure(Map<String, ?> configs) {
        // Retrieve the field name from the configuration
        fieldName = (String) configs.get(FIELD_NAME_CONFIG);
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new IllegalArgumentException(FIELD_NAME_CONFIG + " configuration is required and cannot be empty.");
        }
    }

    @Override
    public R apply(R record) {
        if (record.value() == null) {
            return record; // Skip null values
        }

        if (!(record.value() instanceof Map)) {
            throw new DataException("Record value is not a Map (JSON object).");
        }

        // Cast the record value to a Map
        Map<String, Object> valueMap = (Map<String, Object>) record.value();

        // Get the target field
        Object fieldValue = valueMap.get(fieldName);
        if (fieldValue == null) {
            return record; // Skip if the specified field is missing
        }

        if (!(fieldValue instanceof Map)) {
            throw new DataException("The field '" + fieldName + "' is not a JSON object (Map).");
        }

        // Transform the field's value (Map) into a list of values
        Map<String, Object> fieldValueMap = (Map<String, Object>) fieldValue;
        List<Object> valuesList = new ArrayList<>(fieldValueMap.values());

        // Replace the original field value with the transformed list
        valueMap.put(fieldName, valuesList);

        // Return the updated record
        return record.newRecord(
            record.topic(),
            record.kafkaPartition(),
            record.keySchema(),
            record.key(),
            record.valueSchema(),
            valueMap,
            record.timestamp()
        );
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
            .define(
                FIELD_NAME_CONFIG,
                ConfigDef.Type.STRING,
                ConfigDef.Importance.HIGH,
                "The name of the JSON object field to transform into a list."
            );
    }

    @Override
    public void close() {
        // No resources to close
    }
}