Feature: Concurrent microcontroller connections management
  As a multi-threaded TCP server
  I want to handle multiple microcontrollers connected simultaneously
  So that data can be collected in parallel without conflicts

  Background:
    Given the TCP server is running on port 8080
    And the server supports multi-threading
    And the MongoDB database is accessible

  Scenario: Handle three microcontrollers connected simultaneously
    Given microcontroller "MC-001" is connected
    And microcontroller "MC-002" is connected
    And microcontroller "MC-003" is connected
    When all three microcontrollers are active
    Then the server should maintain 3 separate threads
    And each connection should have its own session
    And the server should handle requests from each independently

  Scenario: Receive data from multiple microcontrollers concurrently
    Given microcontroller "MC-001" is connected
    And microcontroller "MC-002" is connected
    And microcontroller "MC-003" is connected
    When "MC-001" sends "SEND_SENSOR MC-001 temperature 22.5"
    And "MC-002" sends "SEND_SENSOR MC-002 distance 45" at the same time
    And "MC-003" sends "SEND_IMAGE MC-003 2048" at the same time
    Then all three requests should be processed successfully
    And each microcontroller should receive "OK" response
    And all data should be correctly stored without conflicts

  Scenario: Handle microcontroller disconnection without affecting others
    Given microcontroller "MC-001" is connected
    And microcontroller "MC-002" is connected
    And microcontroller "MC-003" is connected
    When "MC-002" disconnects unexpectedly
    Then the server should detect the disconnection
    And the thread for "MC-002" should be cleaned up
    And "MC-001" and "MC-003" should remain connected and functional
    And the server should continue accepting new connections

  Scenario: Accept new connection after one disconnects
    Given microcontroller "MC-001" is connected
    And microcontroller "MC-002" is connected
    When "MC-002" disconnects
    And a new microcontroller "MC-004" attempts to connect
    Then the server should accept the new connection
    And "MC-004" should be assigned a new thread
    And both "MC-001" and "MC-004" should be functional

  Scenario: Handle reconnection of same microcontroller
    Given microcontroller "MC-001" was previously connected
    And "MC-001" disconnected
    When "MC-001" attempts to reconnect
    Then the server should accept the reconnection
    And a new session should be created for "MC-001"
    And previous session data should be properly closed

  Scenario: Prevent thread starvation with many connections
    Given 10 microcontrollers are attempting to connect
    When all connections are established
    Then the server should accept all connections
    And each connection should have dedicated thread resources
    And no connection should be starved of processing time
    And response times should remain under 1 second

  Scenario: Handle concurrent database writes from multiple threads
    Given microcontroller "MC-001" is connected
    And microcontroller "MC-002" is connected
    And microcontroller "MC-003" is connected
    When all three send data simultaneously requiring database writes
    Then the shared database access object should handle concurrent requests
    And no data should be lost or corrupted
    And no deadlocks should occur
    And all data should be correctly attributed to the right device

  Scenario: Maintain session isolation between connections
    Given microcontroller "MC-001" is connected with session "SESSION-001"
    And microcontroller "MC-002" is connected with session "SESSION-002"
    When "MC-001" sends data
    Then the data should be associated only with "SESSION-001"
    And "SESSION-002" should not be affected
    And session data should not leak between threads

  Scenario: Handle maximum concurrent connections limit
    Given the server maximum connections limit is 10
    And 10 microcontrollers are already connected
    When an 11th microcontroller attempts to connect
    Then the server should reject the connection gracefully
    And the server should respond with "ERR MAX_CONNECTIONS_REACHED"
    And existing connections should remain stable

  Scenario: Graceful server shutdown with active connections
    Given microcontroller "MC-001" is connected
    And microcontroller "MC-002" is connected
    And microcontroller "MC-003" is connected
    When the server receives shutdown signal
    Then the server should notify all connected microcontrollers
    And the server should wait for pending requests to complete
    And all threads should be properly terminated
    And all resources should be released

  Scenario: Handle one thread crash without affecting others
    Given microcontroller "MC-001" is connected
    And microcontroller "MC-002" is connected
    And microcontroller "MC-003" is connected
    When the thread handling "MC-002" encounters an unhandled exception
    Then the exception should be caught and logged
    And "MC-002" connection should be closed
    And "MC-001" and "MC-003" should continue functioning normally
    And the server should remain stable

  Scenario: Concurrent access to shared configuration
    Given microcontroller "MC-001" is connected
    And microcontroller "MC-002" is connected
    When "MC-001" requests "GET_CONFIG sensor_interval"
    And "MC-002" requests "GET_CONFIG sensor_interval" at the same time
    Then both requests should be handled without conflicts
    And both should receive the same configuration value
    And no race conditions should occur

  Scenario: Load balancing with high-frequency data
    Given microcontroller "MC-001" sends data every 1 second
    And microcontroller "MC-002" sends data every 1 second
    And microcontroller "MC-003" sends data every 1 second
    When all microcontrollers are actively sending
    Then the server should distribute processing evenly
    And no single thread should monopolize resources
    And all data should be processed within acceptable latency

  Scenario: Handle mixed request types concurrently
    Given microcontroller "MC-001" is connected
    And microcontroller "MC-002" is connected
    And microcontroller "MC-003" is connected
    When "MC-001" sends sensor data
    And "MC-002" sends an image at the same time
    And "MC-003" requests configuration at the same time
    Then all requests should be processed in parallel
    And each should complete successfully without interference
    And response times should be optimal for each request type