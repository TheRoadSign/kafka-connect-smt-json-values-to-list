package com.kafka.connect.transforms;

import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonValuesToListTest {

    private JsonValuesToList<SinkRecord> transformation;

    @BeforeEach
    void setUp() {
        transformation = new JsonValuesToList<>();
        transformation.configure(Map.of("field.name", "customField"));
    }

    @Test
    void testTransformWithValidField() {
        // Create a JSON-like Map with a nested object
        Map<String, Object> customFieldValue = new HashMap<>();
        customFieldValue.put("key1", "value1");
        customFieldValue.put("key2", 123);
        customFieldValue.put("key3", true);

        Map<String, Object> recordValue = new HashMap<>();
        recordValue.put("customField", customFieldValue);

        // Create a SinkRecord
        SinkRecord record = new SinkRecord(
                "test-topic",
                0,
                null,
                null,
                null,
                recordValue,
                0
        );

        // Apply the transformation
        SinkRecord transformedRecord = transformation.apply(record);

        // Validate the transformation
        assertNotNull(transformedRecord);
        assertTrue(transformedRecord.value() instanceof Map);

        // Check that "customField" was transformed into a list
        Map<String, Object> transformedValue = (Map<String, Object>) transformedRecord.value();
        assertTrue(transformedValue.get("customField") instanceof List);

        List<?> valuesList = (List<?>) transformedValue.get("customField");
        assertEquals(3, valuesList.size());
        assertTrue(valuesList.contains("value1"));
        assertTrue(valuesList.contains(123));
        assertTrue(valuesList.contains(true));
    }

    @Test
    void testTransformWithMissingField() {
        // Create a JSON-like Map without the specified field
        Map<String, Object> recordValue = new HashMap<>();
        recordValue.put("anotherField", "someValue");

        // Create a SinkRecord
        SinkRecord record = new SinkRecord(
                "test-topic",
                0,
                null,
                null,
                null,
                recordValue,
                0
        );

        // Apply the transformation
        SinkRecord transformedRecord = transformation.apply(record);

        // Validate that the record remains unchanged
        assertNotNull(transformedRecord);
        assertEquals(recordValue, transformedRecord.value());
    }

    @Test
    void testTransformWithInvalidFieldType() {
        // Create a JSON-like Map where the specified field is not a Map
        Map<String, Object> recordValue = new HashMap<>();
        recordValue.put("customField", "not-a-map");

        // Create a SinkRecord
        SinkRecord record = new SinkRecord(
                "test-topic",
                0,
                null,
                null,
                null,
                recordValue,
                0
        );

        // Expect a DataException
        Exception exception = assertThrows(DataException.class, () -> transformation.apply(record));
        assertTrue(exception.getMessage().contains("The field 'customField' is not a JSON object (Map)."));
    }
}