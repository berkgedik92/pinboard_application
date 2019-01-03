<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8"/>
  <meta property="hostAddr" url="${hostAddr}"/>
  <meta property="redirectURL" url="${redirectURL}"/>
  <meta property="tokenGiver" url="${tokenGiver}"/>
  <meta property="tokenValidator" url="${tokenValidator}"/>
  <meta property="redirectFrom" url="${redirectFrom}"/>

  <script src="pinboard/jquery.min.js"></script>
  <script src="pinboard/comm.js"></script>
</head>

<body>
Username <input type="text" id="username"/><br>
Password <input type="password" id="password"/><br>
<input type="button" value="Login" onclick="login()"/>
</body>
<script>

var comm;

var authErrorCallback = function(error) {
	alert("You entered wrong username or password");
}

var authSuccessCallback = function() {
	comm.goToSourcePage();
}

$(document).ready(function() {
	comm = new Communicator({
		"loginMethod"			: LoginMethod.Callback,
		"noTokenCallback"		: function() {},
		"authErrorCallback"		: authErrorCallback,
		"authSuccessCallback"	: authSuccessCallback
	});
})

function login() {
	comm.Login($("#username").val(), $("#password").val());
}

</script>
</html>