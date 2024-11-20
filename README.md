# JsonValuesToList - Kafka Connect Custom SMT

JsonValuesToList is a custom Kafka Connect Single Message Transformation (SMT) that dynamically transforms the values of a specified JSON object field into a list. This is useful for use cases where a nested JSON object in a Kafka message needs to be converted into a flat list of its values.

## Features

-	Dynamic Field Specification: Allows you to configure the name of the JSON object field to transform via field.name.
-	Transforms JSON Objects to Lists: Converts the specified field’s JSON object values into a List<Object>.
-	Error Handling: Gracefully skips records with missing or null values, and throws clear exceptions for invalid field types.
-	Flexibility: Works seamlessly with both source and sink connectors in schema-less mode.


## Requirements

- Java 8 or higher
- Apache Kafka 3.x
- Kafka Connect

## Installation

1. Build the JAR file:
```bash
mvn clean package
```
2. Copy the generated JAR file (from the target/ directory) to the Kafka Connect plugin path:
```bash
cp target/custom-kafka-connect-smt-1.0-SNAPSHOT.jar /path/to/kafka-connect/plugins/
```
3.	Add the plugin path to your Kafka Connect worker configuration (if not already set):
```bash
plugin.path=/path/to/kafka-connect/plugins
```
4.	Restart Kafka Connect:
```bash
systemctl restart kafka-connect
```

##Configuration

To use the JsonValuesToList SMT in a Kafka Connect connector, add the following configuration to the connector properties:
```json
{
  "transforms": "JsonValuesToList",
  "transforms.JsonValuesToList.type": "com.example.kafka.connect.transforms.JsonValuesToList",
  "transforms.JsonValuesToList.field.name": "customField"
}
```

##Example Usage

###Input

Topic: example-topic
```json
{
  "customField": {
    "key1": "value1",
    "key2": 123,
    "key3": true
  },
  "otherField": "someValue"
}
```

###Transformation Output

The transformed record will have the following value:
```json
{
  "customField": [
    "value1",
    123,
    true
  ],
  "otherField": "someValue"
}
```

### Error Handling

1.	Missing Field:
    -	If the specified field (field.name) is missing, the record remains unchanged.
2.	Null Value:
    -	If the record value is null, the transformation is skipped.
3.	Invalid Field Type:
    -	If the specified field is not a JSON object (e.g., a string or number), the SMT throws a DataException.


### Example Configuration for a Sink Connector

Here’s an example configuration for a Kafka Connect sink connector using the JsonValuesToList transformation:

```json
{
  "name": "couchbase-source-connector",
  "connector.class": "com.couchbase.connect.kafka.CouchbaseSourceConnector",
  "tasks.max": "1",
  "couchbase.seed.nodes": "127.0.0.1",             // Replace with your Couchbase host
  "couchbase.bucket": "example_bucket",           // Your Couchbase bucket name
  "couchbase.scope": "_default",                  // Scope in Couchbase (optional)
  "couchbase.collection": "_default",             // Collection in Couchbase (optional)
  "couchbase.username": "admin",                  // Couchbase username
  "couchbase.password": "password",               // Couchbase password
  "topics": "example-topic",                      // Kafka topic to write the documents to
  "key.converter": "org.apache.kafka.connect.json.JsonConverter",
  "value.converter": "org.apache.kafka.connect.json.JsonConverter",
  "transforms": "JsonValuesToList",
  "transforms.JsonValuesToList.type": "com.example.kafka.connect.transforms.JsonValuesToList",
  "transforms.JsonValuesToList.field.name": "customField"
}
```
### SMT Configuration Options

|    Configuration                    | Description  | Required|Default |Description                                                   |
|-------------------------------------|---------------|--------|--------|--------------------------------------------------------------|
transforms.JsonValuesToList.field.name|	String	      |Yes	   | None	|The name of the JSON object field to transform into a list.   |



## Testing

Run the included unit tests to verify the transformation:
```bash
mvn test
```


To test the connector and transformation:

1.	Insert a Document into Couchbase:
Use Couchbase’s Query Editor or SDK to insert a test document into your bucket:
```sql
INSERT INTO `example_bucket` (KEY, VALUE)
VALUES ("test-doc", {
  "customField": {
    "key1": "value1",
    "key2": 123,
    "key3": true
  },
  "otherField": "someValue"
});
```
2.	Check the Output in Kafka:
Consume messages from the Kafka topic (example-topic) to verify the transformation:
```bash
kafka-console-consumer --bootstrap-server <kafka-broker> --topic example-topic --from-beginning --property print.key=true
```

License

This project is licensed under the MIT License.

Contributions

Contributions are welcome! Feel free to submit pull requests or report issues.
