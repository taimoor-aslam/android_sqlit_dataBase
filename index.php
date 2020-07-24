<?php

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/*
*	Server Connection String 
*/

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "db_notes";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
  die("Connection failed: " . $conn->connect_error);
}

if(isset($_POST['ACTION']) && $_POST['ACTION']=='add_note'){
	$sql = "INSERT INTO notes (`note`)
	VALUES ('".$_POST["note"]."')";

	if ($conn->query($sql) === TRUE) {
		$sql = "SELECT `id`, `note`, `timestamp` FROM `notes`";

		$result = $conn -> query($sql);

		// Fetch all
		$resultSet=$result -> fetch_all(MYSQLI_ASSOC);

		// Free result set
		//$result -> free_result();
	
		echo json_encode($resultSet);
	} else {
	  echo "Error: " . $sql . "<br>" . $conn->error;
	}

	$conn->close();

}

if(isset($_POST['ACTION']) && $_POST['ACTION']=='get_all_notes'){
	$sql = "SELECT `id`, `note`, `timestamp` FROM `notes`";

	$result = $conn -> query($sql);

	// Fetch all
	$resultSet=$result -> fetch_all(MYSQLI_ASSOC);

	// Free result set
	//$result -> free_result();
	
	echo json_encode($resultSet);

	$conn -> close();

}


if(isset($_POST['ACTION']) && $_POST['ACTION']=='delete_note'){
	
	$id = $_POST["id"];
	$sql = "DELETE FROM `notes` WHERE `id`='$id'";
	
	if ($conn->query($sql) === TRUE) {
		$sql = "SELECT `id`, `note`, `timestamp` FROM `notes`";

		$result = $conn -> query($sql);

		// Fetch all
		$resultSet=$result -> fetch_all(MYSQLI_ASSOC);

		echo json_encode($resultSet);
	} 
	else {
	  echo "Error: " . $sql . "<br>" . $conn->error;
	}
	$conn -> close();
	
}

if(isset($_POST['ACTION']) && $_POST['ACTION']=='update_note'){
	$id = $_POST["id"];
	$newVal = $_POST["note"];
	
	$sql = "UPDATE `notes` SET `note`='$newVal' WHERE `id`='$id'";
	
	if ($conn->query($sql) === TRUE) {
		$sql = "SELECT `id`, `note`, `timestamp` FROM `notes`";

		$result = $conn -> query($sql);

		// Fetch all
		$resultSet=$result -> fetch_all(MYSQLI_ASSOC);

		echo json_encode($resultSet);
	} 
	else {
	  echo "Error: " . $sql . "<br>" . $conn->error;
	}
	$conn -> close();
	
}

// if(isset($_POST['ACTION']) && $_POST['ACTION']=='update_note'){
// 	$sql = "UPDATE `notes` SET `note`='".$_POST["note"]."' WHERE `id`='".$_POST["id]."'";

// 	$result = $conn -> query($sql);

// 	// Fetch all
// 	$resultSet=$result -> fetch_all(MYSQLI_ASSOC);

// 	// Free result set
// 	//$result -> free_result();
	
// 	echo json_encode($resultSet);

// 	$conn -> close();
// }

// if(isset($_POST['ACTION']) && $_POST['ACTION']=='delete_note')
// {
// 	$id = $_POST["id"];
// 	$sql = "DELETE FROM `notes` WHERE `id`='$id'";

// 	$result = $conn -> query($sql);

// 	// Fetch all
// 	$resultSet=$result -> fetch_all(MYSQLI_ASSOC);

// 	// Free result set
// 	//$result -> free_result();
	
// 	echo json_encode($resultSet);

// 	$conn -> close();
// }

?>