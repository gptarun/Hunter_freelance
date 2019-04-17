<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Extrace</title>
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
	</head>
	<body>
		<h1 align="center">Please enter bank account info</h1>
		<br>
		<form align="center">
			<input type="button" id = "Save" onclick="callHunterAPI();" value="Connect My Account">
			
			<input type="button" id = "Query" onclick="queryMe();" value="Query Me">
		</form>
		
		<script>
			function callHunterAPI() {
				$.ajax({
			        url: "/callHunter",
			        type: "POST",
			        headers: {
		                "Content-Type": "application/json"
		            },
			        success: function (data) {
			        	console.log("POST API RESPONSE : " + data.sessionId);
			        	console.log("URL: " + data.sessionUrl);
			        	location.reload(true);
			        	window.location.href = data.sessionUrl;
			        }, error: function (jqXHR, textStatus, errorThrown) {
			        }
				});
			}
			
			function queryMe() {
				$.ajax({
			        url: "/queryMe",
			        type: "POST",
			        headers: {
		                "Content-Type": "application/json"
		            },
			        success: function (data) {
			        	console.log("URL: " + data.baseName);
			        }, error: function (jqXHR, textStatus, errorThrown) {
			        }
				});
			}
		</script>
	</body>
</html>