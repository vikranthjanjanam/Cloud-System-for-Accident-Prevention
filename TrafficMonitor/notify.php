<?php
	
	function send_notification($tokens, $message){
		$url = 'https://fcm.googleapis.com/fcm/send';
		$fields = array(
			'registration_ids' => $tokens,
			'data' => $message
		);
		$headers = array(
			'Authorization: key=AAAAHmvU6U4:APA91bEUaooh_Uuxkqc-vUx2m35UIHgYPhEZVIlDsoYv7TAXu8b9TBF-K-Zoeqejva_cogE3Zirxu2NRwZQ3pMUDfdaEFPYa3KYG86ytB3u56SHlxK52gJgKCFILbOPu1gIrjax21ELb',
			'Content-Type: application/json'
		);
		
		$ch = curl_init();
		curl_setopt($ch, CURLOPT_URL, $url);
		curl_setopt($ch, CURLOPT_POST, true);
		curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
		curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
		curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
		
		$result = curl_exec($ch);
		
		if($result == FALSE){
			die('Curl failed : '.curl_error($ch));
		}
		curl_close($ch);
		
		return $result;
	}
	
	require 'vendor/autoload.php';
	
	$mclient = new MongoDB\Client("mongodb://localhost:27017");
	//echo "Connected to DB<br>";
	
	$coll = $mclient -> TrafficData -> FCM_Users;
	$result = $coll -> find();
	$tokens = array();
	$i = 0;
	foreach ($result as $entry){
		$tokens[$i++] = $entry['Token'];
	}
	$message = array(
		"message" => "FCM Push Notification Test Message`	`",
		"title" => "FCM Test"
	);
	$status = send_notification($tokens, $message);
	echo $status;
	
?>