Feature: Data persistence management
  As a data centralization server
  I want to transmit collected data to the Node API
  So that it can be stored in MongoDB

  Background:
    Given the TCP server is running on port 8080
    And the Node API is available at "http://localhost:3000"
    And the MongoDB database is accessible

  Scenario: Successfully store sensor data via Node API
    Given a microcontroller "MC-001" sends temperature data "22.5"
    When the server receives the data
    Then the server should forward the data to the Node API endpoint "/api/sensors/data"
    And the Node API should respond with status 200
    And the server should respond to the microcontroller with "OK"
    And the data should be persisted in MongoDB collection "sensor_data"

  Scenario: Store multiple data points in batch
    Given a microcontroller "MC-002" sends multiple sensor readings
      | sensor_type | value |
      | distance    | 45    |
      | weight      | 12.5  |
      | temperature | 22.0  |
    When the server receives all readings
    Then the server should forward all data to the Node API
    And all 3 records should be stored in the database
    And the server should respond with "OK" for each reading

  Scenario: Fallback to direct MongoDB access when API is unavailable
    Given the Node API is not responding
    And direct MongoDB access is configured
    When a microcontroller "MC-001" sends temperature data "22.5"
    Then the server should detect API unavailability
    And the server should store the data directly in MongoDB using Java driver
    And the server should respond to the microcontroller with "OK"
    And a warning should be logged about API unavailability

  Scenario: Queue data when both API and MongoDB are unavailable
    Given the Node API is not responding
    And direct MongoDB access is not available
    When a microcontroller "MC-001" sends temperature data "22.5"
    Then the server should queue the data in memory
    And the server should respond to the microcontroller with "OK QUEUED"
    And the data should be marked for retry

  Scenario: Retry queued data when API becomes available
    Given 5 data points are queued due to API unavailability
    When the Node API becomes available
    Then the server should automatically retry sending queued data
    And all 5 data points should be successfully stored
    And the queue should be cleared

  Scenario: Store image metadata via Node API
    Given a microcontroller "MC-003" sends an image
    When the server receives the image
    Then the server should create metadata with device_id, timestamp, and size
    And the server should forward metadata to Node API endpoint "/api/images/metadata"
    And the image binary should be stored in MongoDB GridFS via Node API
    And the server should respond with "OK"

  Scenario: Handle API response error
    Given the Node API is available
    When a microcontroller "MC-001" sends invalid data format
    And the Node API responds with status 400 and error "INVALID_DATA_FORMAT"
    Then the server should not retry the request
    And the server should respond to the microcontroller with "ERR INVALID_FORMAT"
    And the error should be logged

  Scenario: Validate data before sending to API
    Given a microcontroller sends sensor data
    When the data contains all required fields
      | field       | value   |
      | device_id   | MC-001  |
      | sensor_type | temp    |
      | value       | 22.5    |
      | timestamp   | 1696780800 |
    Then the server should validate the data structure
    And the server should forward the data to the Node API
    And the server should respond with "OK"

  Scenario: Reject incomplete data before API call
    Given a microcontroller sends sensor data
    When the data is missing required field "sensor_type"
    Then the server should not forward the data to the Node API
    And the server should respond with "ERR MISSING_PARAMS"
    And an error should be logged

  Scenario: Store analysis results from media server
    Given the media analysis server sends analysis results
      | device_id | waste_type | confidence | detected_objects |
      | MC-003    | plastic    | 0.85       | bottle,bag       |
    When the server receives the results
    Then the server should forward results to Node API endpoint "/api/analysis/results"
    And the results should be linked to the original image
    And the server should respond to media server with "OK"

  Scenario: Use direct MongoDB access for high-frequency data
    Given direct MongoDB access is enabled for high-frequency sensors
    And device "MC-001" is configured as high-frequency
    When the microcontroller "MC-001" sends temperature data every 5 seconds
    Then the server should use direct MongoDB access instead of Node API
    And the data should be stored using POJO mapping
    And write operations should be batched for performance

  Scenario: Monitor storage performance
    Given 100 data points are received within 1 minute
    When all data is processed
    Then at least 95% should be successfully stored
    And the average response time should be less than 500ms
    And any failed storage should be logged

  Scenario: Handle MongoDB connection loss during direct access
    Given direct MongoDB access is being used
    When the MongoDB connection is lost
    Then the server should attempt to reconnect
    And pending data should be queued
    And the server should switch to API access if available
    And microcontrollers should still receive "OK QUEUED" responses