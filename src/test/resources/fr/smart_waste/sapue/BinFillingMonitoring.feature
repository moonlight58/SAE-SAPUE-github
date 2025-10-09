# language: en
Feature: Waste bin filling level monitoring
  As a microcontroller with proximity sensor and weight sensor (μC-2)
  I want to transmit the filling level and weight
  So that collection routes can be optimized

  Background:
    Given the TCP server is running on port 8080
    And the MongoDB database is accessible
    And the Node API is available

  Scenario: Successfully receive filling level data
    Given a microcontroller "MC-002" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-002 distance 45"
    Then the server should respond with "OK"
    And the filling level should be calculated as "55%"
    And the data should be stored in the database

  Scenario: Receive weight measurement
    Given a microcontroller "MC-002" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-002 weight 12.5"
    Then the server should respond with "OK"
    And the data should contain value "12.5"
    And the data should be stored in the database

  Scenario: Detect bin almost full
    Given a microcontroller "MC-002" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-002 distance 15"
    Then the server should respond with "OK"
    And the filling level should be calculated as "85%"
    And an alert "BIN_ALMOST_FULL" should be generated
    And the bin status should be updated to "needs_collection"

  Scenario: Detect bin completely full
    Given a microcontroller "MC-002" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-002 distance 5"
    Then the server should respond with "OK"
    And the filling level should be calculated as "95%"
    And an alert "BIN_FULL" should be generated
    And the bin should be marked as "urgent"

  Scenario: Detect lid opening event
    Given a microcontroller "MC-002" is connected to the server
    When the microcontroller sends "SEND_EVENT MC-002 lid_opened"
    Then the server should respond with "OK"
    And an event "lid_opened" should be recorded with timestamp
    And the event should be stored in the database

  Scenario: Detect lid closing event
    Given a microcontroller "MC-002" is connected to the server
    When the microcontroller sends "SEND_EVENT MC-002 lid_closed"
    Then the server should respond with "OK"
    And an event "lid_closed" should be recorded with timestamp

  Scenario: Calculate filling rate from multiple measurements
    Given a microcontroller "MC-002" is connected to the server
    And the bin was "50%" full 1 hour ago
    When the microcontroller sends "SEND_SENSOR MC-002 distance 25"
    Then the server should respond with "OK"
    And the current filling level should be "75%"
    And the filling rate should be calculated as "25% per hour"

  Scenario: Correlate distance and weight sensors
    Given a microcontroller "MC-002" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-002 distance 30"
    And the microcontroller sends "SEND_SENSOR MC-002 weight 15.8"
    Then the server should respond with "OK" for each request
    And the filling level should be "70%"
    And the density should be calculated
    And both measurements should be correlated in database

  Scenario: Detect anomaly - low distance but low weight
    Given a microcontroller "MC-002" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-002 distance 10"
    And the microcontroller sends "SEND_SENSOR MC-002 weight 2.0"
    Then the server should respond with "OK" for each request
    And an alert "ANOMALY_DETECTED" should be generated
    And the anomaly type should be "potential_blockage"

  Scenario: Handle negative distance value
    Given a microcontroller "MC-002" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-002 distance -5"
    Then the server should respond with "ERR INVALID_VALUE"
    And no data should be stored in the database

  Scenario: Handle weight exceeding maximum capacity
    Given a microcontroller "MC-002" is connected to the server
    When the microcontroller sends "SEND_SENSOR MC-002 weight 150.0"
    Then the server should respond with "OK"
    And an alert "OVERWEIGHT" should be generated
    And the data should be flagged as "requires_inspection"