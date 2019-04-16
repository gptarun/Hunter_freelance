<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>User Details</title>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
</head>
<body>
	<h1 align="center">Welcome to User's Page</h1>
	<form align="center">
			<input type="button" id = "user" onclick="getUserData();" value="User Data">
			
			<input type="button" id = "account" onclick="getAccountDetails();" value="Account Details">
			
			<input type="button" id = "statement" onclick="getAccountStatement();" value="Account Statement">
		</form>
		<script>
			function getUserData() {
				$.ajax({
			        url: "/getUser",
			        type: "POST",
			        headers: {
		                "Content-Type": "application/json"
		            },
			        success: function (data) {
			        	console.log(data);
			        }, error: function (jqXHR, textStatus, errorThrown) {
			        }
				});
			}
			
			function getAccountDetails() {
				$.ajax({
			        url: "/getAccountDetails",
			        type: "POST",
			        headers: {
		                "Content-Type": "application/json"
		            },
			        success: function (data) {
			        	console.log(data);
			        }, error: function (jqXHR, textStatus, errorThrown) {
			        }
				});
			}
			
			function getAccountStatement() {
				$.ajax({
			        url: "/getAccountStatement",
			        type: "POST",
			        headers: {
		                "Content-Type": "application/json"
		            },
			        success: function (data) {
			        	console.log(data);
			        }, error: function (jqXHR, textStatus, errorThrown) {
			        }
				});
			}
		</script>
</body>
</html>