package handlers

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"strconv"

	"smarthome/db"
	"smarthome/models"
	"smarthome/services"

	"github.com/gin-gonic/gin"
)

// SensorHandler handles sensor-related requests
type SensorHandler struct {
	DB                   *db.DB
	TemperatureService   *services.TemperatureService
	TelemetryClient      *services.TelemetryClient
	DeviceControlClient  *services.DeviceControlClient
}

// NewSensorHandler creates a new SensorHandler
func NewSensorHandler(db *db.DB, temperatureService *services.TemperatureService, telemetryClient *services.TelemetryClient, deviceControlClient *services.DeviceControlClient) *SensorHandler {
	return &SensorHandler{
		DB:                   db,
		TemperatureService:   temperatureService,
		TelemetryClient:      telemetryClient,
		DeviceControlClient:  deviceControlClient,
	}
}

// RegisterRoutes registers the sensor routes
func (h *SensorHandler) RegisterRoutes(router *gin.RouterGroup) {
	sensors := router.Group("/sensors")
	{
		sensors.GET("", h.GetSensors)
		sensors.GET("/:id", h.GetSensorByID)
		sensors.POST("", h.CreateSensor)
		sensors.PUT("/:id", h.UpdateSensor)
		sensors.DELETE("/:id", h.DeleteSensor)
		sensors.PATCH("/:id/value", h.UpdateSensorValue)
		sensors.GET("/temperature/:location", h.GetTemperatureByLocation)
		sensors.POST("/:id/command", h.SendCommand)
	}

	// Command routes (outside /sensors group)
	router.GET("/commands/:id", h.GetCommandStatus)
}

// GetSensors handles GET /api/v1/sensors
func (h *SensorHandler) GetSensors(c *gin.Context) {
	sensors, err := h.DB.GetSensors(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	// Update temperature sensors with real-time data from the external API
	for i, sensor := range sensors {
		if sensor.Type == models.Temperature {
			tempData, err := h.TemperatureService.GetTemperatureByID(fmt.Sprintf("%d", sensor.ID))
			if err == nil {
				// Update sensor with real-time data
				sensors[i].Value = tempData.Value
				sensors[i].Status = tempData.Status
				sensors[i].LastUpdated = tempData.Timestamp
				log.Printf("Updated temperature data for sensor %d from external API", sensor.ID)

				// Push telemetry reading to telemetry service
				if h.TelemetryClient != nil {
					go func(sensorID int, value float64, unit string) {
						if err := h.TelemetryClient.PostTelemetry(sensorID, "temperature", value, unit); err != nil {
							log.Printf("Failed to push telemetry for sensor %d: %v", sensorID, err)
						} else {
							log.Printf("Pushed telemetry for sensor %d to telemetry service", sensorID)
						}
					}(sensor.ID, tempData.Value, tempData.Unit)
				}
			} else {
				log.Printf("Failed to fetch temperature data for sensor %d: %v", sensor.ID, err)
			}
		}
	}

	c.JSON(http.StatusOK, sensors)
}

// GetSensorByID handles GET /api/v1/sensors/:id
func (h *SensorHandler) GetSensorByID(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid sensor ID"})
		return
	}

	sensor, err := h.DB.GetSensorByID(context.Background(), id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Sensor not found"})
		return
	}

	// If this is a temperature sensor, fetch real-time data from the temperature API
	if sensor.Type == models.Temperature {
		tempData, err := h.TemperatureService.GetTemperatureByID(fmt.Sprintf("%d", sensor.ID))
		if err == nil {
			// Update sensor with real-time data
			sensor.Value = tempData.Value
			sensor.Status = tempData.Status
			sensor.LastUpdated = tempData.Timestamp
			log.Printf("Updated temperature data for sensor %d from external API", sensor.ID)
		} else {
			log.Printf("Failed to fetch temperature data for sensor %d: %v", sensor.ID, err)
		}
	}

	c.JSON(http.StatusOK, sensor)
}

// GetTemperatureByLocation handles GET /api/v1/sensors/temperature/:location
func (h *SensorHandler) GetTemperatureByLocation(c *gin.Context) {
	location := c.Param("location")
	if location == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Location is required"})
		return
	}

	// Fetch temperature data from the external API
	tempData, err := h.TemperatureService.GetTemperature(location)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": fmt.Sprintf("Failed to fetch temperature data: %v", err),
		})
		return
	}

	// Return the temperature data
	c.JSON(http.StatusOK, gin.H{
		"location":    tempData.Location,
		"value":       tempData.Value,
		"unit":        tempData.Unit,
		"status":      tempData.Status,
		"timestamp":   tempData.Timestamp,
		"description": tempData.Description,
	})
}

// CreateSensor handles POST /api/v1/sensors
func (h *SensorHandler) CreateSensor(c *gin.Context) {
	var sensorCreate models.SensorCreate
	if err := c.ShouldBindJSON(&sensorCreate); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	sensor, err := h.DB.CreateSensor(context.Background(), sensorCreate)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, sensor)
}

// UpdateSensor handles PUT /api/v1/sensors/:id
func (h *SensorHandler) UpdateSensor(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid sensor ID"})
		return
	}

	var sensorUpdate models.SensorUpdate
	if err := c.ShouldBindJSON(&sensorUpdate); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	sensor, err := h.DB.UpdateSensor(context.Background(), id, sensorUpdate)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, sensor)
}

// DeleteSensor handles DELETE /api/v1/sensors/:id
func (h *SensorHandler) DeleteSensor(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid sensor ID"})
		return
	}

	err = h.DB.DeleteSensor(context.Background(), id)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Sensor deleted successfully"})
}

// UpdateSensorValue handles PATCH /api/v1/sensors/:id/value
func (h *SensorHandler) UpdateSensorValue(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid sensor ID"})
		return
	}

	var request struct {
		Value  float64 `json:"value" binding:"required"`
		Status string  `json:"status" binding:"required"`
	}

	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	err = h.DB.UpdateSensorValue(context.Background(), id, request.Value, request.Status)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Sensor value updated successfully"})
}

// SendCommand handles POST /api/v1/sensors/:id/command
func (h *SensorHandler) SendCommand(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid sensor ID"})
		return
	}

	if h.DeviceControlClient == nil {
		c.JSON(http.StatusServiceUnavailable, gin.H{"error": "Device control service is not available"})
		return
	}

	var request struct {
		Type       string                 `json:"type" binding:"required"`
		Parameters map[string]interface{} `json:"parameters"`
	}

	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	result, err := h.DeviceControlClient.SendCommand(id, request.Type, request.Parameters)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": fmt.Sprintf("Failed to send command: %v", err),
		})
		return
	}

	c.JSON(http.StatusCreated, result)
}

// GetCommandStatus handles GET /api/v1/commands/:id
func (h *SensorHandler) GetCommandStatus(c *gin.Context) {
	commandID := c.Param("id")
	if commandID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Command ID is required"})
		return
	}

	if h.DeviceControlClient == nil {
		c.JSON(http.StatusServiceUnavailable, gin.H{"error": "Device control service is not available"})
		return
	}

	result, err := h.DeviceControlClient.GetCommandStatus(commandID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": fmt.Sprintf("Failed to get command status: %v", err),
		})
		return
	}

	c.JSON(http.StatusOK, result)
}
