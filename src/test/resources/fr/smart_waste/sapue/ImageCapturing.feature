Feature: Visual capture for waste analysis
  As a microcontroller with camera (μC-3)
  I want to send images to the server
  So that visual analysis of bin content can be performed

  Background:
    Given the TCP server is running on port 8080
    And the media analysis server is available
    And the MongoDB database is accessible

  Scenario: Successfully send small image
    Given a microcontroller "MC-003" is connected to the server
    When the microcontroller sends "SEND_IMAGE MC-003 2048"
    And the microcontroller sends 2048 bytes of binary data
    Then the server should respond with "OK"
    And the image should be forwarded to the media analysis server
    And an image record should be created in the database

  Scenario: Send image with metadata
    Given a microcontroller "MC-003" is connected to the server
    When the microcontroller sends "SEND_IMAGE MC-003 2048 1696780800"
    And the microcontroller sends 2048 bytes of binary data
    Then the server should respond with "OK"
    And the image metadata should contain timestamp "1696780800"
    And the image metadata should contain device_id "MC-003"

  Scenario: Handle large image transfer
    Given a microcontroller "MC-003" is connected to the server
    When the microcontroller sends "SEND_IMAGE MC-003 65536"
    And the microcontroller sends 65536 bytes of binary data
    Then the server should respond with "OK"
    And the complete image should be received
    And the image should be forwarded to the media analysis server

  Scenario: Reject image exceeding maximum size
    Given a microcontroller "MC-003" is connected to the server
    And the maximum image size is 1048576 bytes
    When the microcontroller sends "SEND_IMAGE MC-003 2097152"
    Then the server should respond with "ERR IMAGE_TOO_LARGE"
    And no image data should be received

  Scenario: Handle incomplete image transfer
    Given a microcontroller "MC-003" is connected to the server
    When the microcontroller sends "SEND_IMAGE MC-003 4096"
    And the microcontroller sends only 2048 bytes of binary data
    And the connection is interrupted
    Then the server should detect incomplete transfer
    And the partial image should be discarded
    And an error should be logged

  Scenario: Handle timeout during image transfer
    Given a microcontroller "MC-003" is connected to the server
    And the transfer timeout is set to 30 seconds
    When the microcontroller sends "SEND_IMAGE MC-003 4096"
    And no data is received for 35 seconds
    Then the server should respond with "ERR TRANSFER_TIMEOUT"
    And the connection should remain open for retry

  Scenario: Send multiple images sequentially
    Given a microcontroller "MC-003" is connected to the server
    When the microcontroller sends "SEND_IMAGE MC-003 2048"
    And the microcontroller sends 2048 bytes of binary data
    And the microcontroller sends "SEND_IMAGE MC-003 3072"
    And the microcontroller sends 3072 bytes of binary data
    Then the server should respond with "OK" for each image
    And 2 image records should be created in the database

  Scenario: Handle corrupted image data
    Given a microcontroller "MC-003" is connected to the server
    When the microcontroller sends "SEND_IMAGE MC-003 2048"
    And the microcontroller sends corrupted binary data
    Then the server should respond with "OK"
    And the image should be forwarded to the media analysis server
    And the media analysis server should detect the corruption

  Scenario: Trigger scheduled image capture
    Given a microcontroller "MC-003" is connected to the server
    When the server sends "CAPTURE_IMAGE"
    Then the microcontroller should acknowledge with "OK"
    And the microcontroller should send an image within 10 seconds

  Scenario: Handle missing image size parameter
    Given a microcontroller "MC-003" is connected to the server
    When the microcontroller sends "SEND_IMAGE MC-003"
    Then the server should respond with "ERR MISSING_PARAMS"
    And no image data should be expected

  Scenario: Associate image with bin location
    Given a microcontroller "MC-003" is connected to the server
    And the device "MC-003" is registered with location "lat:47.6205,lon:6.8566"
    When the microcontroller sends "SEND_IMAGE MC-003 2048"
    And the microcontroller sends 2048 bytes of binary data
    Then the server should respond with "OK"
    And the image metadata should contain location "lat:47.6205,lon:6.8566"