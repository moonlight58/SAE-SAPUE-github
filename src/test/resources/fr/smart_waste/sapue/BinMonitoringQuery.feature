# language: en
Feature: TCP server manages connections and data centralization
  As a TCP centralization server
  I need to handle concurrent client connections
  And process their requests through API or direct MongoDB access

  Background:
    Given the TCP server is listening on port 8080
    And MongoDB database is accessible
    And Node API is available at "http://localhost:3000"

  # ==========================================
  # CONNECTION MANAGEMENT
  # ==========================================

  Scenario: Server accepts new microcontroller connection
    When a microcontroller connects to the server
    Then the server creates a new thread for this client
    And the server sends "OK" response
    And the thread waits for incoming requests

  Scenario: Server handles multiple concurrent connections
    When 3 different clients connect simultaneously
    Then the server creates 3 separate threads
    And all threads run independently
    And each client receives "OK" response

  # ==========================================
  # SENSOR DATA STORAGE
  # ==========================================

  Scenario: Server receives valid sensor data
    Given a microcontroller is connected
    When the server receives "SEND_FILL_LEVEL binId=BIN001 level=75"
    Then the server parses the request parameters
    And the server forwards data to Node API via POST "/api/bins/data"
    And the server waits for API response
    And the server sends "OK" to the microcontroller

  Scenario: Server handles malformed request
    Given a microcontroller is connected
    When the server receives "SEND_FILL_LEVEL invalid_data"
    Then the server detects format error
    And the server sends "ERR_INVALID_FORMAT" to the microcontroller

  Scenario: Server handles missing parameters
    Given a microcontroller is connected
    When the server receives "SEND_FILL_LEVEL binId=BIN001"
    Then the server detects missing "level" parameter
    And the server sends "ERR_MISSING_PARAMS" to the microcontroller

  # ==========================================
  # FALLBACK TO DIRECT MONGODB ACCESS
  # ==========================================

  Scenario: Server uses direct MongoDB when API is unavailable
    Given a microcontroller is connected
    And Node API is not responding
    When the server receives "SEND_WEIGHT binId=BIN001 weight=45.5"
    Then the server attempts to contact Node API
    And the attempt fails
    And the server switches to direct MongoDB access
    And the server stores data using POJO objects
    And the server sends "OK" to the microcontroller

  # ==========================================
  # DATA RETRIEVAL
  # ==========================================

  Scenario: Server retrieves configuration from database
    Given a microcontroller is connected
    When the server receives "GET_CONFIG binId=BIN001"
    Then the server queries Node API via GET "/api/bins/BIN001/config"
    And the server receives configuration data
    And the server sends "OK interval=300 threshold=80" to the microcontroller

  Scenario: Server handles non-existent device query
    Given a microcontroller is connected
    When the server receives "GET_CONFIG binId=UNKNOWN"
    Then the server queries Node API
    And API returns 404 error
    And the server sends "ERR_DEVICE_NOT_FOUND" to the microcontroller

  # ==========================================
  # MULTIMEDIA ANALYSIS SERVER RESULTS
  # ==========================================

  Scenario: Server receives analysis results from multimedia server
    Given the multimedia analysis server is connected
    When the server receives "ANALYSIS_RESULT reportId=REP001 wasteType=encombrant confidence=0.92"
    Then the server forwards results to Node API via POST "/api/reports/analysis"
    And the server sends "OK" to the analysis server

  # ==========================================
  # ERROR HANDLING
  # ==========================================

  Scenario: Server handles database error during storage
    Given a microcontroller is connected
    And MongoDB is temporarily unavailable
    When the server receives "SEND_AIR_QUALITY binId=BIN001 co2=450"
    Then the server attempts both API and direct access
    And both attempts fail
    And the server sends "ERR_DATABASE_ERROR" to the microcontroller

  Scenario: Server detects invalid sensor value
    Given a microcontroller is connected
    When the server receives "SEND_FILL_LEVEL binId=BIN001 level=150"
    Then the server validates the data
    And detects level > 100 is invalid
    And the server sends "ERR_INVALID_VALUE" to the microcontroller