<?php
    if(isset($_GET)) {
        
        $vehicle = $_GET['Vehicle'];
        
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
        $filter = ["Vehicle" => $vehicle, "Timestamp" => $time - 1];
        $result = $db -> VehicleData -> find($filter);
        $data = array();
        $servertime = 0;
        $error = TRUE;
        foreach ($result as $row) {
            foreach ($keys as $key) {
                $data[$key] = $row[$key];
            }
            $servertime = $row["Timestamp"];
            $error = FALSE;
        }
        if(!$error) {
            $data["Date"] = date("Y-m-d", $servertime);
            $data["Time"] = date("h:i:s A", $servertime);
            $data["error"] = FALSE;
        } else {
            $data["error"] = TRUE;
        }
        echo json_encode($data);
    }
?>