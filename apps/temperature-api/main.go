package main

import (
	"encoding/json"
	"fmt"
	"log"
	"math/rand"
	"net/http"
	"time"
)

// TemperatureResponse represents the temperature data returned by the API
type TemperatureResponse struct {
	Value       float64   `json:"value"`
	Unit        string    `json:"unit"`
	Timestamp   time.Time `json:"timestamp"`
	Location    string    `json:"location"`
	Status      string    `json:"status"`
	SensorID    string    `json:"sensor_id"`
	SensorType  string    `json:"sensor_type"`
	Description string    `json:"description"`
}

func main() {
	http.HandleFunc("/temperature", handleTemperature)
	http.HandleFunc("/temperature/", handleTemperatureByID)

	port := ":8081"
	log.Printf("Temperature API starting on port %s\n", port)
	if err := http.ListenAndServe(port, nil); err != nil {
		log.Fatalf("Failed to start server: %v\n", err)
	}
}

// handleTemperature handles GET /temperature?location=...&sensorId=...
func handleTemperature(w http.ResponseWriter, r *http.Request) {
	location := r.URL.Query().Get("location")
	sensorID := r.URL.Query().Get("sensorId")

	// If no location is provided, use a default based on sensor ID
	if location == "" {
		switch sensorID {
		case "1":
			location = "Living Room"
		case "2":
			location = "Bedroom"
		case "3":
			location = "Kitchen"
		default:
			location = "Unknown"
		}
	}

	// If no sensor ID is provided, generate one based on location
	if sensorID == "" {
		switch location {
		case "Living Room":
			sensorID = "1"
		case "Bedroom":
			sensorID = "2"
		case "Kitchen":
			sensorID = "3"
		default:
			sensorID = "0"
		}
	}

	resp := generateTemperatureResponse(location, sensorID)
	writeJSON(w, resp)
}

// handleTemperatureByID handles GET /temperature/{sensorId}
func handleTemperatureByID(w http.ResponseWriter, r *http.Request) {
	// Extract sensorId from path: /temperature/{sensorId}
	sensorID := r.URL.Path[len("/temperature/"):]
	if sensorID == "" {
		http.Error(w, "sensor ID is required", http.StatusBadRequest)
		return
	}

	// Map sensor ID to location
	var location string
	switch sensorID {
	case "1":
		location = "Living Room"
	case "2":
		location = "Bedroom"
	case "3":
		location = "Kitchen"
	default:
		location = "Unknown"
	}

	resp := generateTemperatureResponse(location, sensorID)
	writeJSON(w, resp)
}

// generateTemperatureResponse creates a response with a random temperature
func generateTemperatureResponse(location, sensorID string) TemperatureResponse {
	// Random temperature between 15.0 and 30.0 °C
	value := 15.0 + rand.Float64()*15.0
	value = float64(int(value*100)) / 100 // round to 2 decimal places

	return TemperatureResponse{
		Value:       value,
		Unit:        "°C",
		Timestamp:   time.Now().UTC(),
		Location:    location,
		Status:      "active",
		SensorID:    sensorID,
		SensorType:  "temperature",
		Description: fmt.Sprintf("Temperature sensor in %s", location),
	}
}

func writeJSON(w http.ResponseWriter, data interface{}) {
	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(data); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}
}
