Feature: Environmental data collection from waste bins
  As a microcontroller BME280/MQ135 (μC-1)
  I want to send temperature, humidity and air quality data
  So that abnormal conditions can be detected

  Background:
    Given the TCP server is running on port 8080
    And the MongoDB database is accessible
    And the Node API is available

  Scenario: Successfully receive valid environmental data
    Given a microcontroller "MC-001" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-001 temperature 22.5"
    Then the server should respond with "OK"
    And the data should be stored in the database
    And the data should contain device_id "MC-001"
    And the data should contain sensor_type "temperature"
    And the data should contain value "22.5"

  Scenario: Receive multiple sensor data from same microcontroller
    Given a microcontroller "MC-001" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-001 temperature 22.5"
    And the microcontroller sends "SEND_SENSOR MC-001 humidity 65.0"
    And the microcontroller sends "SEND_SENSOR MC-001 air_quality 150"
    Then the server should respond with "OK" for each request
    And 3 records should be stored in the database

  Scenario: Detect critical air quality level
    Given a microcontroller "MC-001" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-001 air_quality 450"
    Then the server should respond with "OK"
    And the data should be flagged as "critical"
    And an alert should be generated

  Scenario: Reject invalid sensor value format
    Given a microcontroller "MC-001" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-001 temperature invalid_value"
    Then the server should respond with "ERR INVALID_VALUE"
    And no data should be stored in the database

  Scenario: Handle missing parameters in sensor data
    Given a microcontroller "MC-001" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-001 temperature"
    Then the server should respond with "ERR MISSING_PARAMS"
    And no data should be stored in the database

  Scenario: Receive sensor data with timestamp
    Given a microcontroller "MC-001" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-001 temperature 22.5 1696780800"
    Then the server should respond with "OK"
    And the data should contain timestamp "1696780800"

  Scenario: Handle unregistered device sending data
    Given no microcontroller is registered
    When a device sends "SEND_SENSOR MC-999 temperature 22.5"
    Then the server should respond with "ERR DEVICE_NOT_REGISTERED"
    And no data should be stored in the database