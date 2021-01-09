<?php

    function getDB(){
	require 'vendor/autoload.php';	
	$mclient = new MongoDB\Client("mongodb://localhost:27017");
	return $mclient -> TrafficData;
    }
    
    function getManager(){
        $manager = new MongoDB\Driver\Manager('mongodb://localhost:27017');
    }
    $baseURL = "http://localhost:8081/Trafficmonitor/";