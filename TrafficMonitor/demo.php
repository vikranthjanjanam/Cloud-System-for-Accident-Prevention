<?php

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
    require_once 'dbConnection.php';
    $vehicle = $_POST['Vehicle'];
    $timestamp = intval($_POST["Timestamp"]);
    
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
    
    $db = getDB();
    $filter = [
        "Vehicle" => $vehicle,
        "Timestamp" => $timestamp
    ];
    $res = $db -> VehicleData -> find($filter);
    $data = [];
    foreach ($res as $row) {
        foreach ($keys as $key){
            $data[$key] = strval($row[$key]);
            $_POST[$key] = $data[$key];
        }
    }
    
    //echo json_encode($data);
    
    require_once 'dataset.php';