<?php
	$response = array();
	
	if($_SERVER['REQUEST_METHOD'] == 'POST'){
		require 'vendor/autoload.php';
		
		$mclient = new MongoDB\Client("mongodb://localhost:27017");
		$coll = $mclient -> TrafficData -> Users;
		
		$mobile = $_POST['Mobile'];
		$pass = $_POST['Password'];
		$token = $_POST['Token'];
		
		$filter = ["Mobile" => $mobile];
		$res = $coll -> count($filter);
		
		if($res == 1){
			$filter = ['Mobile' => $mobile, 'Password' => $pass];
			$cursor = $coll -> find($filter);
			if($cursor != null){
				$res = $coll -> updateOne(
					$filter,
					['$set' => ["Token" => $token]]
				);
				if($res != null){
					foreach($cursor as $values);
					$response['error'] = false;
					$response['Name'] = $values['Name'];
					$response['Mobile'] = $values['Mobile'];
					$response['Vehicle'] = $values['Vehicle'];
				}else{
					$response['error'] = true;
					$response['message'] = "Some Error Occured. Please Try Again Later";
				}
			}else{
				$response['error'] = true;
				$response['message'] = "Invalid Password";
			}
		}else{
			$response['error'] = true;
			$response['message'] = "Invalid Mobile Number";
		}
		
	}else{
		$response['error'] = true;
		$response['message'] = "Invalid Request";
	}
	echo json_encode($response);
?>