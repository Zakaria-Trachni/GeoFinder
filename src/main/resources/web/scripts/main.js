// Declare layer variables
let routeLayer = null;
let routeCategoryLayer = null;
let searchCircle = null;
let categoryCircles = [];
var map, currentLayer;
var layers = {};
// OpenRouteService API Key:
const ORS_API_KEY = "5b3ce3597851110001cf6248083212a018354d429d86e9ccd0168016";
// OpenWeatherMap API key:
const OWM_API_KEY = '7d54cc1eb3429b786803832102a7d68f';

// Global variables for waypoint management
let waypoints = []; // To store all waypoints [lng, lat] pairs
let waypointMarkers = []; // To store waypoint markers

// Global array to track all markers on the map (not just waypoints)
let allMarkers = [];
let allCategoryMarkers = [];

// markers icon:
var icons = {
    hospital: L.icon({
        iconUrl: '../assets/images/hospital.png',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    }),
    pharmacy: L.icon({
        iconUrl: '../assets/images/pharmacy.png',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    }),
    hotel: L.icon({
        iconUrl: '../assets/images/hotel.png',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    }),
    bank: L.icon({
        iconUrl: '../assets/images/bank.png',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    }),
    restaurant: L.icon({
        iconUrl: '../assets/images/restaurant.png',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    }),
    parking: L.icon({
        iconUrl: '../assets/images/parking.png',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    }),
    cafe: L.icon({
        iconUrl: '../assets/images/cafe.png',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    }),
    supermarket: L.icon({
        iconUrl: '../assets/images/supermarket.png',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    }),
    school: L.icon({
        iconUrl: '../assets/images/school.png',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    }),
    mosque: L.icon({
        iconUrl: '../assets/images/mosque.png',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    })
};


// Initialize map when page loads
window.onload = initMap;

// Initialize map
function initMap() {
    // Create the map with default view
    map = L.map('map', {
        zoomControl: false
    }).setView([31.7917, -7.0926], 6); // Default view of Morocco

    // Initialize the map click handler once the page is loaded
    map.on('click', function(e) {
        javaApp.handleMapClick(e.latlng.lat, e.latlng.lng);
    });

    // Define base layers
    layers = {
        'OpenStreetMap': L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }),

        'Satellite': L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
            attribution: 'Esri, Maxar, Earthstar Geographics, and the GIS User Community'
        }),

        'Topographic': L.tileLayer('https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenTopoMap contributors'
        }),

        'Dark Mode': L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
            attribution: '© CARTO'
        }),

        'Terrain': L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}', {
            attribution: 'Esri, DeLorme, USGS, NPS'
        })
    };

    // Set the default layer
    setLayer('OpenStreetMap');

    // Add CSS for waypoint markers
    addWaypointStyles();
}

// Function to switch layers
function setLayer(layerName) {
    if (currentLayer) {
        map.removeLayer(currentLayer);
    }

    if (layers[layerName]) {
        currentLayer = layers[layerName];
        map.addLayer(currentLayer);
        console.log('Switched to ' + layerName + ' layer');
    } else {
        console.error('Layer not found: ' + layerName);
    }
}

// Function to add a marker and track it
function addMarkerToMap(lat, lng, popupText = '') {
    marker = L.marker([lat, lng]).addTo(map);
    if (popupText) {
        marker.bindPopup(popupText);
    }

    marker.on('click', function () {
        map.removeLayer(marker);
        marker = null;
        if (typeof javaApp !== 'undefined' && javaApp.markerRemoved) {
            javaApp.markerRemoved();
        }
    });

    // Add to our tracking array
    allMarkers.push(marker);

    return marker;
}

// Specific function to add categories marker:
function addMarkerCategory(category, markerLat, markerLon, popup, categoryRadius) {
    categoryCircle = L.circle([markerLat, markerLon], {
        radius: categoryRadius,
        color: '#1E90FF',
        fillColor: '#1E90FF',
        fillOpacity: 0.1
    }).addTo(map);

    categoryCircles.push(categoryCircle);

    const categoryMarker = L.marker([markerLat, markerLon], {icon: icons[category]}).addTo(map).bindPopup(popup);

    // When a category marker is clicked, show route from user's location to this marker
    categoryMarker.on('click', function () {
        // First, open the popup to show information about the location
        categoryMarker.openPopup();

        // Then after a small delay, call Java method to get current user marker position:
        setTimeout(() => {
            javaApp.getUserMarkerPosition(markerLat, markerLon);
        }, 300);
    });

    // Add to our tracking array
    allCategoryMarkers.push(categoryMarker);

    return categoryMarker;
}

function clearAllCategoryCircles() {
    categoryCircles.forEach(circle => {
        map.removeLayer(circle);
    });
    categoryCircles = [];
}

// This function shows the search radius:
function showSearchRadius(searchRadius, lat, lon) {
    if (searchCircle) {
        map.removeLayer(searchCircle);
    }

    // Create a new circle
    searchCircle = L.circle([lat, lon], {
        radius: searchRadius,
        color: '#FF8F6C',
        fillColor: '#FF8F6C',
        fillOpacity: 0.1
    }).addTo(map);

    // Fit the map to the circle's bounds
    map.fitBounds(searchCircle.getBounds(), { padding: [10, 10] });
}

function clearSearchCircle() {
    if(searchCircle){
        map.removeLayer(searchCircle);
        searchCircle = null;
    }
}

// This function shows the route from the marker to the location position:
function showMarkerToCategoryLocationRoute(fromLat, fromLng, toLat, toLng) {
    const coordinates = [
        [fromLng, fromLat],  // [longitude, latitude]
        [toLng, toLat]
    ];

    fetch("https://api.openrouteservice.org/v2/directions/driving-car/geojson", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": ORS_API_KEY
        },
        body: JSON.stringify({ coordinates: coordinates })
    })
        .then(response => response.json())
        .then(data => {
            // Remove existing route if any
            if (routeCategoryLayer) {
                map.removeLayer(routeCategoryLayer);
            }

            // Draw the route
            routeCategoryLayer = L.geoJSON(data, {
                style: {
                    color: "#FF8F6C",  // Orange color
                    weight: 8,
                    opacity: 0.9
                }
            }).addTo(map);

            // Fit map view to route:
            map.fitBounds(routeCategoryLayer.getBounds(), { padding: [50, 50] });
        })
        .catch(error => {
            console.error("Error fetching route:", error);
            alert("Failed to fetch route");
        });
}

function calculateAndDisplayRoute(mode = "driving") {
    if (routeLayer) {
        map.removeLayer(routeLayer);
    }

    // Map the mode to OpenRouteService profile
    const transportMode = mode === "driving" ? "driving-car" :
        mode === "cycling" ? "cycling-regular" : "foot-walking";

    // Create the request body for OpenRouteService Directions API
    const body = {
        coordinates: waypoints,
        format: "geojson",
        instructions: true,
        preference: "recommended"
    };

    // Make the API request
    fetch(`https://api.openrouteservice.org/v2/directions/${transportMode}/geojson`, {
        method: 'POST',
        headers: {
            'Accept': 'application/json, application/geo+json, application/gpx+xml',
            'Content-Type': 'application/json',
            'Authorization': ORS_API_KEY
        },
        body: JSON.stringify(body)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            // Process the route data
            const routeFeature = data.features[0];
            const geometry = routeFeature.geometry;
            const properties = routeFeature.properties;

            // Get distance and duration
            const distanceKm = properties.summary.distance / 1000; // Convert to km
            const durationMin = Math.round(properties.summary.duration / 60); // Convert to minutes

            // Set line style based on transportation mode - all using orange (#FF8F6C) base color
            let lineStyle = {
                color: "#FF8F6C",
                weight: 8,
                opacity: 0.9
            };

            // Add different effects based on mode
            if (mode === "cycling" || mode === "walking") {
                // Dashed line for cycling
                lineStyle.dashArray = "10, 10";
            }

            // Create GeoJSON layer with route
            routeLayer = L.geoJSON(geometry, {
                style: lineStyle
            }).addTo(map);

            // Make the route interactive
            makeRouteInteractive(routeLayer, geometry, mode);

            // Fit the map to show the route
            map.fitBounds(routeLayer.getBounds(), {padding: [50, 50]});

            // Create popup with route info
            const popupContent = `
            <div class="route-info">
                <strong>${mode.charAt(0).toUpperCase() + mode.slice(1)}</strong><br>
                Distance: ${distanceKm.toFixed(2)} km<br>
                Duration: ${durationMin} min
                <div class="route-instructions">
                    <em>Click on the route to add waypoints</em>
                </div>
            </div>
        `;

            routeLayer.bindPopup(popupContent).openPopup();

            // Call Java method if available to pass route information
            if (typeof javaApp !== "undefined" && javaApp.receiveRouteInfo) {
                javaApp.receiveRouteInfo(properties.summary.distance, properties.summary.duration, mode);
            }

            console.log(`Route found: ${distanceKm.toFixed(2)} km, ${durationMin} min via ${mode}`);

        })
        .catch(error => {
            console.error("Error fetching route:", error);

            // Hide loading indicator if available
            if (typeof javaApp !== "undefined" && javaApp.showLoading) {
                javaApp.showLoading(false);
            }

            alert("Error fetching route: " + error.message);

            // Notify Java application of the error
            if (typeof javaApp !== "undefined" && javaApp.routeError) {
                javaApp.routeError(error.message);
            }
        });
}

// Function to remove all markers from the map
function removeAllMarkers() {
    for (let i = 0; i < allMarkers.length; i++) {
        map.removeLayer(allMarkers[i]);
    }

    // Reset the markers array
    allMarkers = [];

    console.log("All markers removed from map");
}
function removeAllCategoryMarkers() {
    for (let i = 0; i < allCategoryMarkers.length; i++) {
        map.removeLayer(allCategoryMarkers[i]);
    }

    // Reset the markers array
    allCategoryMarkers = [];
}


// Add the current position marker to the map;
function addCurrPositionMarker(lat, lng) {
    if (window.currentMarker) {
        // Remove the marker:
        map.removeLayer(window.currentMarker);
        // Remove the marker's categories:
        clearAllCategories();
    }

    window.currentMarker = L.marker([lat, lng]).addTo(map);
    window.currentMarker.bindPopup('Selected location<br><small>(Click to remove)</small>').openPopup();

    window.currentMarker.on('click', function () {
        // Remove the marker:
        map.removeLayer(window.currentMarker);
        window.currentMarker = null;
        if (typeof javaApp !== 'undefined' && javaApp.markerRemoved) {
            javaApp.markerRemoved();
        }

        // Remove the marker's categories:
        clearAllCategories();
    });
}




// IP Geolocation function:
function locateUser() {
    fetch('http://ip-api.com/json/')
        .then(response => response.json())
        .then(data => {
            const lat = data.lat;
            const lng = data.lon;

            // Move map to user location
            map.setView([lat, lng], 13);

            // Create a marker and track it
            addMarkerToMap(lat, lng, "Your approximate location").openPopup();
            javaApp.setMarkerPosition(lat, lng)

            // Notify Java application
            javaApp.sendLocation(lat, lng);
        })
        .catch(error => {
            console.error("IP Geolocation error:", error);
        });
}

// Search for an address:
function geocodeAddress(address) {
    if (!address || address.trim() === "") {
        javaApp.searchError("Please enter a location to search");
        return;
    }

    const encodedAddress = encodeURIComponent(address.trim());

    fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodedAddress}&limit=1`)
        .then(response => response.json())
        .then(data => {
            if (data && data.length > 0) {
                const result = data[0];
                const lat = parseFloat(result.lat);
                const lon = parseFloat(result.lon);

                // Center map on result
                map.setView([lat, lon], 15);

                // Add a marker and track it
                marker = addMarkerToMap(lat, lon, `<b>${result.display_name}</b>`);
                marker.openPopup();
                allMarkers.push(marker);
                javaApp.setMarkerPosition(lat, lon);

                // Notify Java application
                javaApp.searchResultFound(lat, lon, result.display_name);
            } else {
                // Location not found
                javaApp.searchError(`No results found for "${address}"`);
            }
        })
        .catch(error => {
            javaApp.searchError("Geocoding error: " + error.message);
        });
}

// This function shows route with specific transportation mode:
function showRouteWithMode(fromLat, fromLng, toLat, toLng, mode) {
    showRoute(fromLat, fromLng, toLat, toLng, mode);
}

// Show the route between two locations with transportation mode:
function showRoute(fromLat, fromLng, toLat, toLng, mode = "driving") {
    // Initialize the waypoints array with start and end points
    waypoints = [
        [fromLng, fromLat],  // Note: ORS uses [longitude, latitude] order
        [toLng, toLat]
    ];

    // Clear any existing waypoint markers
    clearWaypointMarkers();

    // Call the function to calculate and display the route
    calculateAndDisplayRoute(mode);
}

// Function to calculate and display the route based on current waypoints
function calculateAndDisplayRoute(mode = "driving") {
    if (routeLayer) {
        map.removeLayer(routeLayer);
    }

    // Map the mode to OpenRouteService profile
    const transportMode = mode === "driving" ? "driving-car" :
        mode === "cycling" ? "cycling-regular" : "foot-walking";

    // Create the request body for OpenRouteService Directions API
    const body = {
        coordinates: waypoints,
        format: "geojson",
        instructions: true,
        preference: "recommended"
    };

    // Make the API request
    fetch(`https://api.openrouteservice.org/v2/directions/${transportMode}/geojson`, {
        method: 'POST',
        headers: {
            'Accept': 'application/json, application/geo+json, application/gpx+xml',
            'Content-Type': 'application/json',
            'Authorization': ORS_API_KEY
        },
        body: JSON.stringify(body)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            // Process the route data
            const routeFeature = data.features[0];
            const geometry = routeFeature.geometry;
            const properties = routeFeature.properties;

            // Get distance and duration
            const distanceKm = properties.summary.distance / 1000; // Convert to km
            const durationMin = Math.round(properties.summary.duration / 60); // Convert to minutes

            // Set line style based on transportation mode - all using orange (#FF8F6C) base color
            let lineStyle = {
                color: "#FF8F6C",
                weight: 8,
                opacity: 0.9
            };

            // Add different effects based on mode
            if (mode === "cycling" || mode === "walking") {
                // Dashed line for cycling
                lineStyle.dashArray = "10, 10";
            }

            // Create GeoJSON layer with route
            routeLayer = L.geoJSON(geometry, {
                style: lineStyle
            }).addTo(map);

            // Make the route interactive
            makeRouteInteractive(routeLayer, geometry, mode);

            // Fit the map to show the route
            map.fitBounds(routeLayer.getBounds(), {padding: [50, 50]});

            // Create popup with route info
            const popupContent = `
            <div class="route-info">
                <strong>${mode.charAt(0).toUpperCase() + mode.slice(1)}</strong><br>
                Distance: ${distanceKm.toFixed(2)} km<br>
                Duration: ${durationMin} min
                <div class="route-instructions">
                    <em>Click on the route to add waypoints</em>
                </div>
            </div>
        `;

            routeLayer.bindPopup(popupContent).openPopup();

            // Call Java method if available to pass route information
            if (typeof javaApp !== "undefined" && javaApp.receiveRouteInfo) {
                javaApp.receiveRouteInfo(properties.summary.distance, properties.summary.duration, mode);
            }

            console.log(`Route found: ${distanceKm.toFixed(2)} km, ${durationMin} min via ${mode}`);

        })
        .catch(error => {
            console.error("Error fetching route:", error);

            // Hide loading indicator if available
            if (typeof javaApp !== "undefined" && javaApp.showLoading) {
                javaApp.showLoading(false);
            }

            alert("Error fetching route: " + error.message);

            // Notify Java application of the error
            if (typeof javaApp !== "undefined" && javaApp.routeError) {
                javaApp.routeError(error.message);
            }
        });
}

// Function to make the route interactive for adding waypoints
function makeRouteInteractive(routeLayer, geometry, mode) {
    // Add markers for all waypoints
    for (let i = 0; i < waypoints.length; i++) {
        addWaypointMarker(waypoints[i][1], waypoints[i][0], i, mode); // Convert from [lng, lat] to [lat, lng]
    }

    // Make the route clickable to add new waypoints
    routeLayer.on('click', function(e) {
        // Find the closest point on the route to the clicked point
        const closestPoint = findClosestPointOnRoute(e.latlng, geometry.coordinates);

        // Add a new waypoint at this position
        addNewWaypoint(closestPoint, mode);
    });
}

// Function to clear all waypoint markers
function clearWaypointMarkers() {
    // Remove all waypoint markers from the map
    for (let i = 0; i < waypointMarkers.length; i++) {
        if (waypointMarkers[i]) {
            map.removeLayer(waypointMarkers[i]);
        }
    }
    waypointMarkers = [];
}

// Function to add a waypoint marker
function addWaypointMarker(lat, lng, index, mode) {
    // Create a marker for the waypoint
    const markerIcon = L.divIcon({
        className: 'waypoint-marker',
        html: `<div class="waypoint-dot ${index === 0 ? 'start' : (index === waypoints.length-1 ? 'end' : 'middle')}"></div>`,
        iconSize: [15, 15],
        iconAnchor: [7, 7]
    });

    const marker = L.marker([lat, lng], {
        icon: markerIcon,
        draggable: index > 0 && index < waypoints.length - 1, // Only intermediate points are draggable
        zIndexOffset: 1000
    }).addTo(map);

    // Store the index in the marker
    marker.waypointIndex = index;

    // Add marker to the array
    if (index < waypointMarkers.length) {
        // Replace existing marker
        if (waypointMarkers[index]) {
            map.removeLayer(waypointMarkers[index]);
        }
        waypointMarkers[index] = marker;
    } else {
        // Add new marker
        waypointMarkers.push(marker);
    }

    // Handle marker drag events for intermediate waypoints
    if (index > 0 && index < waypoints.length - 1) {
        marker.on('dragstart', function() {
            console.log("Waypoint drag started");
        });

        marker.on('drag', function() {
            // Update is handled on dragend
        });

        marker.on('dragend', function(e) {
            console.log("Waypoint drag ended");

            // Update the waypoint coordinates
            const newPos = e.target.getLatLng();
            waypoints[index] = [newPos.lng, newPos.lat]; // ORS uses [lng, lat] order

            // Recalculate the route
            calculateAndDisplayRoute(mode);
        });

        // Add a popup menu for the waypoint
        marker.bindPopup(`
            <div class="waypoint-popup">
                <button onclick="removeWaypoint(${index}, '${mode}')">Remove waypoint</button>
            </div>
        `);
    }

    return marker;
}

// Function to add a new waypoint to the route
function addNewWaypoint(point, mode) {
    // Find the appropriate position to insert this point in the waypoints array
    let insertIndex = 1; // Default to inserting after the start point
    let minDistance = Infinity;

    for (let i = 0; i < waypoints.length - 1; i++) {
        const segmentStart = L.latLng(waypoints[i][1], waypoints[i][0]); // Convert from [lng, lat] to [lat, lng]
        const segmentEnd = L.latLng(waypoints[i+1][1], waypoints[i+1][0]);

        // Calculate distance from point to this segment
        const distance = distanceToSegment(L.latLng(point[1], point[0]), segmentStart, segmentEnd);

        if (distance < minDistance) {
            minDistance = distance;
            insertIndex = i + 1;
        }
    }

    // Insert the new waypoint
    waypoints.splice(insertIndex, 0, [point[0], point[1]]); // point is already in [lng, lat] format

    // Recalculate the route
    calculateAndDisplayRoute(mode);
}

// Function to remove a waypoint
function removeWaypoint(index, mode) {
    // Remove the waypoint from the array
    if (index > 0 && index < waypoints.length - 1) {
        waypoints.splice(index, 1);

        // Recalculate the route
        calculateAndDisplayRoute(mode);
    }
}

// Utility function to find the closest point on a route to a given point
function findClosestPointOnRoute(clickedPoint, routeCoordinates) {
    let closestPoint = null;
    let minDistance = Infinity;

    // Loop through all points in the route
    for (let i = 0; i < routeCoordinates.length; i++) {
        const routePoint = routeCoordinates[i];
        const distance = L.latLng(clickedPoint.lat, clickedPoint.lng)
            .distanceTo(L.latLng(routePoint[1], routePoint[0])); // Convert from [lng, lat] to [lat, lng]

        if (distance < minDistance) {
            minDistance = distance;
            closestPoint = routePoint;
        }
    }

    return closestPoint;
}

// Utility function to calculate distance from a point to a line segment
function distanceToSegment(point, segmentStart, segmentEnd) {
    const x = point.lng;
    const y = point.lat;
    const x1 = segmentStart.lng;
    const y1 = segmentStart.lat;
    const x2 = segmentEnd.lng;
    const y2 = segmentEnd.lat;

    const A = x - x1;
    const B = y - y1;
    const C = x2 - x1;
    const D = y2 - y1;

    const dot = A * C + B * D;
    const len_sq = C * C + D * D;
    let param = -1;

    if (len_sq !== 0) {
        param = dot / len_sq;
    }

    let xx, yy;

    if (param < 0) {
        xx = x1;
        yy = y1;
    } else if (param > 1) {
        xx = x2;
        yy = y2;
    } else {
        xx = x1 + param * C;
        yy = y1 + param * D;
    }

    const dx = x - xx;
    const dy = y - yy;

    return Math.sqrt(dx * dx + dy * dy);
}

// Function to add CSS styles for waypoint markers
function addWaypointStyles() {
    if (!document.getElementById('waypoint-styles')) {
        const waypointStyles = document.createElement('style');
        waypointStyles.id = 'waypoint-styles';
        waypointStyles.innerHTML = `
            .waypoint-dot {
                width: 15px;
                height: 15px;
                border-radius: 50%;
                border: 2px solid #FFFFFF;
                box-shadow: 0 0 5px rgba(0,0,0,0.5);
            }
            .waypoint-dot.start {
                background-color: #2ECC71; /* Green */
            }
            .waypoint-dot.end {
                background-color: #E74C3C; /* Red */
            }
            .waypoint-dot.middle {
                background-color: #FF8F6C; /* Orange - same as route color */
                cursor: move;
            }
            .waypoint-popup button {
                background-color: #E74C3C;
                color: white;
                border: none;
                padding: 5px 10px;
                border-radius: 3px;
                cursor: pointer;
            }
            .route-instructions {
                margin-top: 5px;
                font-size: 0.9em;
                color: #555;
            }
        `;
        document.head.appendChild(waypointStyles);
    }
}

// Function to clear the current route:
function clearRoute() {
    if (routeLayer) {
        map.removeLayer(routeLayer);
        routeLayer = null;
        clearWaypointMarkers();
        waypoints = [];
        console.log("Route cleared.");
    }
}

function clearCategoryRoute() {
    if (routeCategoryLayer) {
        map.removeLayer(routeCategoryLayer);
        routeCategoryLayer = null;
    }
}

// Function to clear all circles:
function clearAllCategories() {
    // Clear search and category radius :
    clearSearchCircle();
    clearAllCategoryCircles();

    // Clear all category markers:
    removeAllCategoryMarkers();
    // Clear all regular markers
    removeAllMarkers();

    // Clear category route
    clearCategoryRoute();
}

// Function to clear all markers and routes
function clearAll() {
    // Clear category and search circles:
    clearSearchCircle();
    clearAllCategoryCircles();

    // Clear all category markers:
    removeAllCategoryMarkers();
    // Clear all regular markers
    removeAllMarkers();

    // Clear route and waypoints
    clearRoute();
    // Clear category route
    clearCategoryRoute();

    if (window.currentMarker) {
        // Remove the current position marker:
        map.removeLayer(window.currentMarker);
    }
}

// Expose functions to window so they can be called from HTML
window.removeWaypoint = removeWaypoint;
window.removeAllMarkers = removeAllMarkers;
window.clearAll = clearAll;


// >> Function to get the weather infos:
function getWeatherData(cityName, whichLocation) {
    // Build the API URL with the city name and your API key
    const url = `https://api.openweathermap.org/data/2.5/weather?q=${encodeURIComponent(cityName)}&units=metric&appid=${OWM_API_KEY}`;

    // Make the API request
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('City not found or API error');
            }
            return response.json();
        })
        .then(data => {
            // Extract the weather information
            let city = data.name;
            let minTemp = Math.round(data.main.temp_min);
            let maxTemp = Math.round(data.main.temp_max);
            let humidity = data.main.humidity + "%";
            let windSpeed = data.wind.speed + " m/s";
            let pressure = data.main.pressure + " hPa";

            // Call the Java function to update the UI
            javaApp.receiveWeatherInfos(city, minTemp, maxTemp, humidity, windSpeed, pressure, whichLocation);
        })
        .catch(error => {
            // Handle errors by passing the error message to Java
            javaApp.updateWeatherError(error.message, whichLocation);
        });
}