/*//session_start();
	
	require 'vendor/autoload.php';
	$client = new MongoDB\Driver\Manager("mongodb://vikranth:!1Vikranth@ds131237.mlab.com:31237/tmonitor");
	echo "Connected to DB<br>";
	var_dump($client);
	
	$bulk = new MongoDB\Driver\BulkWrite();
	
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
	
	$bulk->insert($data);
	$writeConcern = new MongoDB\Driver\WriteConcern(MongoDB\Driver\WriteConcern::MAJORITY, 100);
	$result = $client->executeBulkWrite('tmonitor.vehicle_data', $bulk, $writeConcern);
	echo $result->getInsertedCount();
	
	/*echo "<br>";
	$col = $client->traffic_data->vehicle_data;
	var_dump($col);
	echo "<br>";
	$data = $_SESSION['data'];
	var_dump($data);
	echo "<br>";
	$res = $col->insertOne($data);
	echo "Data Inserted ";//,'{$res->getInsertedId()}';*/