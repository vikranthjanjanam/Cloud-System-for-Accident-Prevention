<?php

	define( 'API_ACCESS_KEY', 'AAAAmZFqNy4:APA91bGl0Z5-V2F2qs1bw4UqHpJUL_yLmvy1wUYT3IfPmoo_hY6g5FKtOyRjBVK61VLgTqN-KutDz4G3JKiv1k8-X-yE6koXRHcUtERLA0hCcN5n0J6E7Z1qwJPmMcir-wIXJURtgmNP');
	
	function send_notification($token)
	{
		echo 'Hello';
		//   $registrationIds = ;
		#prep the bundle
		$msg = array(
			'body' 	=> 'Gong Go',
			'title'	=> 'Danger',
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
		$result = curl_exec($ch );
		echo $result;
		curl_close( $ch );
	}
	
	require 'vendor/autoload.php';
	
	$mclient = new MongoDB\Client("mongodb://localhost:27017");
	//echo "Connected to DB<br>";
	
	$coll = $mclient -> TrafficData -> Users;
	$result = $coll -> find();
	$tokens = array();
	$i = 0;
	foreach ($result as $entry){
		send_notification($entry['Token']);
	}
?>