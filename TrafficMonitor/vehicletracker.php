<!DOCTYPE html>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->

<?php
    require_once './dbConnection.php';
    $db = getDB();
    $currentVehicle = $_GET['Vehicle'];
    
    $filter = ["Vehicle" => $currentVehicle];
    
    $keys = array(
    	"Vehicle",
	"Latitude",
	"Longitude",
	"Speed",
	"Bearing",
	"Address",
	"Date",
	"Time",
	"Status"
    );
    /* @var $result type */
    $result = $db -> VehicleData -> find($filter);
    $plots = array();
    foreach ($result as $row) {
        $data = [];
        foreach($keys as $key){
            $data[$key] = $row[$key];
        }
        $data["Date"] = date("Y-m-d", $row["Timestamp"]);
        $data["Time"] = date("h:i:s A", $row["Timestamp"]);
        array_push($plots, $data);
    }
    $plots = json_encode($plots);
?>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Gong GO Tracker</title>
        <link href="./styles/tracker.css" type="text/css" rel="stylesheet">
        <style>
            #map {
                min-height: 540px;
                height: 100%;
                width: 100%;
                display: block;
            }
        </style>
        <script src="jquery3.3.1.min.js"></script>
        <script>
            var keys = <?= json_encode($keys)?>;
            var response, updates, ind;
            var map, infowindow;
            function initMap() {
                
                map = new google.maps.Map(document.getElementById('map'), {
                    zoom: 15,
                    center: new google.maps.LatLng(16.299552, 80.440485),
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                });

                infowindow = new google.maps.InfoWindow({});
                var locations = <?=$plots?>, i;
                
                for(i = 0; i < locations.length; i++) {
                    var loc = locations[i];
                    addMarker(loc);
                }
                ind = i;
                window.setInterval(sendRequest, 1000);
            }
            
            function sendRequest() {
                
                $.get("<?=$baseURL?>trackvehicle.php?Vehicle=<?=$currentVehicle?>", function(data, status){
                    if(status === "success"){
                        data = eval('(' + data + ')');
                        if(!data["error"]) {
                            addMarker(data);
                            ind++;
                        }
                    }
                });
            }
            
            function addMarker(loc){
                var lati = new Number(loc["Latitude"]);
                var longi = new Number(loc["Longitude"]);
                var speed = new Number(loc["Speed"]);
                var pos = new google.maps.LatLng(lati, longi);
                map.panTo(pos);
                
                var marker = new google.maps.Marker({
                    position: pos,
                    label: speed.toString(),
                    map: map
                });
                //marker.setIcon('http://maps.google.com/mapfiles/ms/icons/blue-dot.png');
                var markerContext = new String();
                for(var i = 0; i < keys.length; i++)
                {
                    markerContext = markerContext.concat("<span><b>", keys[i], " : </b></span>", loc[keys[i]], "<br>");
                }
		google.maps.event.addListener(marker, 'click', (function (marker, i) {
                    return function () {
			infowindow.setContent(markerContext.toString());
                        infowindow.open(map, marker);
                    };
                })(marker, ind));
            }
        </script>
    </head>
    <body>
      <div>  
        <div id = "headerdiv">
            <h2>Gong Go Tracker</h2>
        </div>
        
        <div id = "navigation">
            <ul>
                <li><a href="<?=$baseURL?>gonggotracker.php">Group</a></li>
                <li>
                    <div class = "dropbtn">
                        <a class="active" style="background-color:#4CAF50;color: white" href="#">Vehicle > <?=$currentVehicle?></a>
                        <div class="dropdown-content">
                            <?php
                                $options = ['$projection' => 'Vehicle'];
                                $result = $db -> Users -> find([], $options);
                                foreach ($result as $row) {
                                    $vh = $row["Vehicle"];
                            ?>
                            <a href="<?=($currentVehicle == $vh)? "#":$baseURL."vehicletracker.php?Vehicle=".htmlentities($vh);?>"><?=$vh;?></a>
                            <?php
                                }
                            ?>
                        </div>
                    </div>
                </li>
            </ul>
        </div>
        
        <div id="map"></div>
        <script async defer
            src="https://maps.googleapis.com/maps/api/js?key=AIzaSyBH5hEvPjxSIEKe5dIlHRG1beB7w9LW7V8&callback=initMap"></script>
      </div>
    </body>
</html>