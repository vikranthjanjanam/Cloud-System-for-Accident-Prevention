<?php

	session_start();

	require 'vendor/autoload.php';
	
	$mclient = new MongoDB\Client("mongodb://localhost:27017");
	echo "Connected to DB<br>";
	
	$coll = $mclient -> TrafficData -> VehicleData;
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
	$data = $_SESSION['data'];
	
	$res = $coll -> insertOne($data);
	if($res){
		echo "Data inserted<br>";
	}
?>