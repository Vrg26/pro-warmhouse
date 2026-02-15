package services

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

// DeviceControlClient communicates with the Device Control microservice
type DeviceControlClient struct {
	BaseURL    string
	HTTPClient *http.Client
}

// CommandRequest represents a command to send to a device
type CommandRequest struct {
	Type       string                 `json:"type"`
	Parameters map[string]interface{} `json:"parameters"`
}

// CommandResponse represents the response from the device control service
type CommandResponse struct {
	ID         string `json:"id"`
	DeviceID   int    `json:"deviceId"`
	Type       string `json:"type"`
	Status     string `json:"status"`
	Parameters string `json:"parameters"`
	CreatedAt  string `json:"createdAt"`
}

// NewDeviceControlClient creates a new device control service client
func NewDeviceControlClient(baseURL string) *DeviceControlClient {
	return &DeviceControlClient{
		BaseURL: baseURL,
		HTTPClient: &http.Client{
			Timeout: 10 * time.Second,
		},
	}
}

// SendCommand sends a command to a device via the device control service
func (c *DeviceControlClient) SendCommand(deviceID int, commandType string, params map[string]interface{}) (map[string]interface{}, error) {
	reqBody := CommandRequest{
		Type:       commandType,
		Parameters: params,
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("error marshaling command request: %w", err)
	}

	url := fmt.Sprintf("%s/api/v1/devices/%d/commands", c.BaseURL, deviceID)
	resp, err := c.HTTPClient.Post(url, "application/json", bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("error sending command: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusCreated {
		return nil, fmt.Errorf("unexpected status code from device control: %d", resp.StatusCode)
	}

	var result map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil, fmt.Errorf("error decoding command response: %w", err)
	}

	return result, nil
}

// GetCommandStatus fetches the status of a command
func (c *DeviceControlClient) GetCommandStatus(commandID string) (map[string]interface{}, error) {
	url := fmt.Sprintf("%s/api/v1/commands/%s", c.BaseURL, commandID)

	resp, err := c.HTTPClient.Get(url)
	if err != nil {
		return nil, fmt.Errorf("error fetching command status: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("unexpected status code: %d", resp.StatusCode)
	}

	var result map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil, fmt.Errorf("error decoding command response: %w", err)
	}

	return result, nil
}
