<?php	
	
	require_once 'dbConnection.php';
	
	function getData() {
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
	
		$data = array();
		foreach($keys as $key){
			$data[$key] = $_POST[$key];
		}
		
		return $data;
	}
	
	function disconnect() {
		ignore_user_abort(true);
		set_time_limit(0);
	
		ob_start();
		// do initial processing 
                if (isset($_POST['RespData'])){
                    echo $_POST['RespData'];
                }
		header('Connection: close');
		header('Content-Length: '.ob_get_length());
		ob_end_flush();
		ob_flush();
		flush();
	}
	
	function toRadians($degree){
		return $degree * 0.0174533;
	}
	
	function toDegrees($radian){
		return $radian * 57.2958;
	}
	
	define( 'API_ACCESS_KEY', 'AAAAmZFqNy4:APA91bGl0Z5-V2F2qs1bw4UqHpJUL_yLmvy1wUYT3IfPmoo_hY6g5FKtOyRjBVK61VLgTqN-KutDz4G3JKiv1k8-X-yE6koXRHcUtERLA0hCcN5n0J6E7Z1qwJPmMcir-wIXJURtgmNP');
	
	function send_notification($token)
	{
		#prep the bundle
		$msg = array(
			'title'	=> 'Gong Go',
			'body'	=> 'Danger!!!. Slow vehicle moving ahead',
			'sound' => 'default'
		);
		$fields = array(
			'to'		=> $token,
			'notification'	=> $msg,
			'data' => array(
				'key1' => 'One',
				'key2' => 'Two'
			)
		);
	
		$headers = array(
			'Authorization: key=' . API_ACCESS_KEY,
			'Content-Type: application/json'
		);
		#Send Reponse To FireBase Server	
		$ch = curl_init();
		curl_setopt( $ch, CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send' );
		curl_setopt( $ch, CURLOPT_POST, true );
		curl_setopt( $ch, CURLOPT_HTTPHEADER, $headers );
		curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );
		curl_setopt( $ch, CURLOPT_SSL_VERIFYPEER, false );
		curl_setopt( $ch, CURLOPT_POSTFIELDS, json_encode( $fields ) );
                /* @var $result type */
                $result = curl_exec($ch);
		curl_close( $ch );
	}
        
        function getMaxSpeedLimit($spd){
            if($spd < 20){
                $maxSpeedLimit = 50;
            }
            elseif($spd < 50){
                $maxSpeedLimit = 50;
            }
            elseif($spd < 80){
                $maxSpeedLimit = 100;
            }
            else{
                $maxSpeedLimit = 120;
            }
            return $maxSpeedLimit;
        }
	
	$db = getDB();
	$VehicleData = $db -> VehicleData;
	
	$data = getData();
	$vehicle = $data["Vehicle"];
        $lat = $data["Latitude"] = floatval($data["Latitude"]);
	$long = $data["Longitude"] = floatval($data["Longitude"]);
	$spd = $data["Speed"] = floatval($data["Speed"]);
	$bearing = $data["Bearing"] = floatval($data["Bearing"]);
	$data["Status"] = intval($data["Status"]);
	$time = $data["Timestamp"] = time();
	//disconnect();
	
	
	$res = $VehicleData -> insertOne($data);
	
	$angd = 1000 / 6371000;
	
	$latr = toRadians($lat);
	$longr = toRadians($long);
	$bearingr = toRadians(fmod($bearing + 180, 360));
	
	$sinlat = sin($latr);
	$coslat = cos($latr);
	
	$sinang = sin($angd);
	$cosang = cos($angd);
	
	$sinbear = sin($bearingr);
	$cosbear = cos($bearingr);
	
	$sinlatd = $sinlat * $cosang + $coslat * $sinang * $cosbear;
	$latd = asin($sinlatd);
	
	$aty = $sinbear * $sinang * $coslat;
	$atx = $cosang - $sinlat * $sinlatd;
	
	$longd = $longr + atan2($aty, $atx);
	
	$latd = toDegrees($latd);
	$longd = fmod((toDegrees($longd) + 540), 360) - 180;
	
	if($lat >= $latd){
		$gtlat = $lat;
		$ltlat = $latd;
	}
	else{
		$gtlat = $latd;
		$ltlat = $lat;
	}
	
	if($long >= $longd){
		$gtlong = $long;
		$ltlong = $longd;
	}
	else{
		$gtlong = $longd;
		$ltlong = $long;
	}
	
	$pbear = fmod($bearing + 20, 360);
	$nbear = fmod(($bearing - 20), 360);
	if($nbear < 0){
		$nbear = 360 + $nbear;
	}
	
	if($pbear >= $nbear){
		$gtbearing = $pbear;
		$ltbearing = $nbear;
	}
	else{
		$gtbearing = $nbear;
		$ltbearing = $pbear;
	}
        
	$maxSpeedLimit = getMaxSpeedLimit($spd);
	$filter = ['Timestamp' => $time];
	if($gtbearing >= 340 && $ltbearing <= 20){
		$filter['$and'] = [
                    ['Timestamp' => $time],
                    ["Latitude" => ['$gt' => $ltlat, '$lt' => $gtlat]],
                    ["Longitude" => ['$gt' => $ltlong, '$lt' => $gtlong]],
                    ['$or' => [
                        ["Bearing" => ['$gte' => 0, '$lte' => $ltbearing]],
                        ["Bearing" => ['$gte' => $gtbearing, '$lte' => 360]]
                    ]],
                    ["Speed" => ['$gte' => $maxSpeedLimit]]
                ];
        }
	else{
		$filter['$and'] = [
                    ['Latitude' => ['$gt' => $ltlat, '$lt' => $gtlat]],
                    ["Longitude" => ['$gt' => $ltlong, '$lt' => $gtlong]],
                    ["Bearing" => ['$gte' => $ltbearing, '$lte' => $gtbearing]],
                    ["Speed" => ['$gte' => $maxSpeedLimit]]
		];
	}
	
        $dangerList = array();
        $res = $VehicleData -> find($filter);
        foreach($res as $entry){
            	if($entry["Vehicle"] != $vehicle){
                        array_push($dangerList, $entry["Vehicle"]);
                }
	}
	$notification = $db -> Notification;
	$filter['$and'] = [
            ["Time" => ['$gte' => $time - 120]],
            ["Source" => $vehicle],
            ["Destination" => ['$in' => $dangerList]]
        ];
	
	$res = $notification -> find($filter);
	foreach($res as $row) {
            	if(in_array($row["Destination"], $dangerList)) {
			$dangerList = array_diff($dangerList, [$row["Destination"]]);
		}
	}   
	
	if($dangerList != []){
            $options = [
		'$projection' => "Token",
                "Vehicle" => ['$in' => $dangerList]
            ];
	
            $res = $db -> Users -> find([], $options);
            foreach($res as $row) {
		send_notification($row["Token"]);
            }
            foreach($dangerList as $vehi) {
		$nf["Source"] = $data["Vehicle"];
		$nf["Destination"] = $vehi;
		$nf["Time"] = $time;
		$notification -> insertOne($nf);
            }
        }
?>