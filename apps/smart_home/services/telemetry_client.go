package services

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

// TelemetryClient communicates with the Telemetry microservice
type TelemetryClient struct {
	BaseURL    string
	HTTPClient *http.Client
}

// TelemetryReadingRequest represents a telemetry data point to send
type TelemetryReadingRequest struct {
	DeviceID   int     `json:"device_id"`
	MetricName string  `json:"metric_name"`
	Value      float64 `json:"value"`
	Unit       string  `json:"unit,omitempty"`
}

// TelemetryReadingResponse represents a telemetry reading from the service
type TelemetryReadingResponse struct {
	ID         string    `json:"id"`
	DeviceID   int       `json:"device_id"`
	MetricName string    `json:"metric_name"`
	Value      float64   `json:"value"`
	Unit       string    `json:"unit"`
	Timestamp  time.Time `json:"timestamp"`
}

// NewTelemetryClient creates a new telemetry service client
func NewTelemetryClient(baseURL string) *TelemetryClient {
	return &TelemetryClient{
		BaseURL: baseURL,
		HTTPClient: &http.Client{
			Timeout: 5 * time.Second,
		},
	}
}

// PostTelemetry sends a telemetry reading to the telemetry service
func (c *TelemetryClient) PostTelemetry(deviceID int, metricName string, value float64, unit string) error {
	reqBody := TelemetryReadingRequest{
		DeviceID:   deviceID,
		MetricName: metricName,
		Value:      value,
		Unit:       unit,
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return fmt.Errorf("error marshaling telemetry request: %w", err)
	}

	url := fmt.Sprintf("%s/api/v1/telemetry", c.BaseURL)
	resp, err := c.HTTPClient.Post(url, "application/json", bytes.NewBuffer(jsonData))
	if err != nil {
		return fmt.Errorf("error sending telemetry: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusCreated {
		return fmt.Errorf("unexpected status code from telemetry service: %d", resp.StatusCode)
	}

	return nil
}

// GetCurrentTelemetry fetches the latest telemetry reading for a device
func (c *TelemetryClient) GetCurrentTelemetry(deviceID int) (*TelemetryReadingResponse, error) {
	url := fmt.Sprintf("%s/api/v1/devices/%d/telemetry/current", c.BaseURL, deviceID)

	resp, err := c.HTTPClient.Get(url)
	if err != nil {
		return nil, fmt.Errorf("error fetching current telemetry: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("unexpected status code: %d", resp.StatusCode)
	}

	var reading TelemetryReadingResponse
	if err := json.NewDecoder(resp.Body).Decode(&reading); err != nil {
		return nil, fmt.Errorf("error decoding telemetry response: %w", err)
	}

	return &reading, nil
}
