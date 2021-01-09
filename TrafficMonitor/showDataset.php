<?php

	require 'vendor/autoload.php';
	
	$mclient = new MongoDB\Client("mongodb://localhost:27017");
	//echo "Connected to DB<br>";
	
	$coll = $mclient -> TrafficData -> VehicleData;
	$result = $coll -> find();
	foreach ($result as $entry){
		//echo $entry['_id'], "<br>";
		echo "Vehicle : ", $entry['Vehicle'], "<br>"
			."Latitude : ", $entry['Latitude'], "<br>"
			."Longitude : ", $entry['Longitude'], "<br>"
			."Speed : ", $entry['Speed'], "<br>"
			."Bearing : ", $entry['Bearing'], "<br>"
			."Date : ", $entry['Date'], "<br>"
			."Time : ", $entry['Time'], "<br><br>";
		
		/*print_r($entry);
		echo "<br><br><br>";*/
	}
?>