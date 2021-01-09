<!DOCTYPE html>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->
<?php
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
    
    function getData($row){
        $data = [];
        global $keys;
        foreach ($keys as $key){
            $data[$key] = $row -> $key;
        }
        $data["Date"] = date("Y-m-d", $row -> Timestamp);
        $data["Time"] = date("h:i:s A", $row -> Timestamp);
        return $data;
    }


    require_once './dbConnection.php';
    $db = getDB();
    
    $options = ['$projection' => 'Vehicle'];
    $result = $db -> Users -> find([], $options);
    $vehicles = array();
    foreach ($result as $row){
        array_push($vehicles, $row["Vehicle"]);
    }
    
    $options = [
        'sort' => ['Timestamp' => -1],
        'limit' => 1
    ];
    $manager = new MongoDB\Driver\Manager('mongodb://localhost:27017');
    $readPreference = new MongoDB\Driver\ReadPreference(MongoDB\Driver\ReadPreference::RP_PRIMARY);
    $plots = [];
    foreach ($vehicles as $vehicle){
        $query = new MongoDB\Driver\Query(["Vehicle" => $vehicle], $options);
        $cursor = $manager->executeQuery('TrafficData.VehicleData', $query, $readPreference);
        foreach($cursor as $row) {
            $data = getData($row);
            array_push($plots, $data);
        }
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
            var response, updates, ind = 0;
            var map, infowindow, markers = [];
            var vhmkr = {};
            
            function initMap() {
                map = new google.maps.Map(document.getElementById('map'), {
                    zoom: 15,
                    center: new google.maps.LatLng(20.5937,78.9629),
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                });
                infowindow = new google.maps.InfoWindow({});
                var locations = <?=$plots?>, i;
                
                for(i = 0; i < locations.length; i++) {
                    var loc = locations[i];
                    addMarker(loc);
                    ind++;
                }
                console.log(vhmkr);
                window.setInterval(sendRequest, 1000);
            }
            
            function sendRequest() {
                
                $.get("<?=$baseURL?>trackGroup.php", function(data, status){
                    if(status === "success"){
                        data = eval('(' + data + ')');
                        if(!data["error"]) {
                            for(var loc in data){
                                if(loc === "error"){
                                    continue;
                                }
                                loc = data[loc];
                                var vh = new String(loc["Vehicle"]);
                                if(vh in vhmkr) {
                                    markers[vhmkr[vh]].setMap(null);
                                    delete vhmkr[vh];
                                }
                                /*for(var i = 0; i < markers.length; i++){
                                    var mkr=markers[i];
                                    if(vh.localeCompare(mkr.vhi) == 0){
                                        mkr.setMap(null);
                                        markers = markers.splice(markers.indexOf(mkr));
                                    }
                                }*/
                                addMarker(loc);
                                ind++;
                            }
                        }
                    }
                });
            }
            
            function addMarker(loc){
                var lati = new Number(loc["Latitude"]);
                var longi = new Number(loc["Longitude"]);
                var speed = new Number(loc["Speed"]);
                var vh = new String(loc["Vehicle"]);
                var pos = new google.maps.LatLng(lati, longi);
                map.panTo(pos);
                
                var marker = new google.maps.Marker({
                    position: pos,
                    label: speed.toString(),
                    map: map,
                    vhi: vh
                });
                //marker.setIcon('http://maps.google.com/mapfiles/ms/icons/blue-dot.png');
                var markerContext = new String();
                for(var i = 0; i < keys.length; i++)
                {
                    markerContext = markerContext.concat("<span><b>", keys[i], " : </b></span>", loc[keys[i]], "<br>");
                }
		google.maps.event.addListener(marker, 'click', (function (marker, i) {
                    return function () {
			infowindow.setContent(markerContext);
                        infowindow.open(map, marker);
                    };
                })(marker, ind));
                markers.push(marker);
                vhmkr[vh] = ind;
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
                <li><a class="active" href="#">Group</a></li>
                <li>
                    <div class = "dropbtn">
                        <a href="#">Vehicle</a>
                        <div class="dropdown-content">
                            <?php
                                foreach ($vehicles as $vehicle) {
                                    $vh = $vehicle;
                            ?>
                            <a href="<?=$baseURL?>vehicletracker.php?Vehicle=<?=htmlentities($vh);?>"><?=$vh;?></a>
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
