<?php
	
	$response = array();
	
	if($_SERVER['REQUEST_METHOD'] == 'POST'){
		require 'vendor/autoload.php';
		
		$mclient = new MongoDB\Client("mongodb://localhost:27017");
		$coll = $mclient -> TrafficData -> Users;
		
		$keys = array(
			"Name",
			"Vehicle",
			"DOB",
			"Mobile",
			"Token",
			"Password"
		);
		
		$data = array();
		
		foreach($keys as $key){
			$data[$key] = $_POST[$key];
		}
		
		$filter['$or'] = [
                    ['Mobile' => $data['Mobile']],
                    ['Vehicle' => $data['Vehicle']]
                ];
		$res = $coll -> count($filter);
		//echo $res;
		
		if($res == 0){
			
			$res = $coll -> insertOne($data);
			
			if($res == null){
				$response['error'] = true;
				$response['message'] = "Some Error Occured. Please Try Again";
			}else{
				$response['error'] = false;
				$response['Name'] = $data['Name'];
				$response['Mobile'] = $data['Mobile'];
				$response['Vehicle'] = $data['Vehicle'];
			}
		}else{
			$response['error'] = true;
			$response['message'] = "Already Registered";
		}
	}else{
		$response['error'] = true;
		$response['message'] = "Invalid Request";
	}
	echo json_encode($response);
?>