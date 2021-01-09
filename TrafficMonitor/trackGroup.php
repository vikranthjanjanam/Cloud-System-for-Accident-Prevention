<?php

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

        require_once 'dbConnection.php';
        $db = getDB();
        
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
        
        $time = time();
        $filter = ["Timestamp" => $time - 1];
        $result = $db -> VehicleData -> find($filter);
        $grpdata = array();
        //$servertime = 0;
        $error = TRUE;
        foreach ($result as $row) {
            foreach ($keys as $key) {
                $data[$key] = $row[$key];
            }
            $data["Date"] = date("Y-m-d", $row["Timestamp"]);
            $data["Time"] = date("h:i:s A", $row["Timestamp"]);
            array_push($grpdata, $data);
            //$servertime = $row["Timestamp"];
            $error = FALSE;
        }
        if(!$error) {
            $grpdata["error"] = FALSE;
        }
        echo json_encode($grpdata);
    
?>
