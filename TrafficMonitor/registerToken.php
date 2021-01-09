<?php
	
	if(isset($_POST['Token'])){
		
		$token = $_POST['Token'];
		require 'vendor/autoload.php';
	
		$mclient = new MongoDB\Client("mongodb://localhost:27017");
		echo "Connected to DB<br>";
	
		$coll = $mclient -> TrafficData -> FCM_Users;
		$data = array();
		$data['id'] = 1;
		$data['Token'] = $token;
		$res = $coll -> insertOne($data);
		if($res != null){
			echo "\nSuccesfully updated token\n";
		}
		else{
			echo "Token not Uploaded\n";
		}
	}
	echo "Server run without problem.\n";
	
?>