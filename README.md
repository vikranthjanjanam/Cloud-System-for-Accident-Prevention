# Cloud-System-for-Accident-Prevention

## ARCHITECTURE:  
![workflow](workflow.PNG)

### Vehicle Data: 
GPS data of moving vehicle is collected from driver’s mobile app and sent to cloud for further processing.

### Cloud: 
Mobile sensors are based on trackers device (mobile app) installed in vehicles. The data collected through sensors is stored in cloud. 

### Gong Go Tracker: 
Gong Go GTS is a full featured web-based GPS tracking system for your fleet of vehicles. It supports vehicle mapping for our project using Google Maps API. The collected stored in MongoDB TrafficData-database for visualization of traffic.

### Data Entry Server:
The geo-location GPS data fetched from driver’s mobile app is inserted into MongoDB by a PHP script.

### MongoDB: 
It is a free open source cross platform document oriented database. It is referred as a NoSQL database and uses JSON-like documents.

### Neighborhood Vehicle Detector: 
By using current vehicle location (Latitude, Longitude) and Bearing (Compass reading of vehicle movement in opposite direction), we can find extreme location of accident prone region at some distance behind the vehicle.

### Relative Speed Checker:
It can detect the relative speed difference between current vehicle by previous position and speed of nearby vehicle using GPS system and identify accident prone vehicles using Greater Circle Geodesic formula.

### Notification Sender: 
Identifying the positions of the vehicles from neighborhood when relative speed difference is identified and sending alert messages to the drivers mobile using Firebase Push Server.
