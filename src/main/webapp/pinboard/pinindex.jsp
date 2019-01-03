<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html ng-app="pinboard">
<head>
	<meta charset="utf-8"/>
	<meta property="hostAddr" url="${hostAddr}"/>
	<meta property="redirectURL" url="${redirectURL}"/>
	<meta property="tokenGiver" url="${tokenGiver}"/>
	<meta property="tokenValidator" url="${tokenValidator}"/>
	<meta property="redirectFrom" url="${redirectFrom}"/>
	<link href="pinboard/userdisplay.css" rel="stylesheet"/>
    <link href="pinboard/ripple.min.css" rel="stylesheet"/>
    <link href="pinboard/select2.min.css" rel="stylesheet" />

	<style>

	body {
        padding-left: 10%;
        padding-right: 10%;
        padding-top: 5%;
        padding-bottom: 5%;
	}

	.pinboard {
        display: inline-block;
        border: 2px solid black;
        border-radius: 25px;
        -webkit-border-radius: 25px;
        height: 160px;
        overflow: hidden;
        width: 100%;
        background-color: rgb(232, 222, 103);
        box-sizing: border-box;
        text-align: center;
	}

	.title {
        text-align: center;
        font-size: 30px;
        line-height: 60px;
        text-transform: uppercase;
        font-weight: 500;
        vertical-align: middle;
	}

    .usershower {
        display: inline-block;
    }

    #addbutton {
        position: fixed;
        top:-50px;
        right:5%;
        background: pink;
        border: 0px solid pink;
        border-radius: 50px;
        -webkit-border-radius: 50px;

    }

    .material-ripple > span {
        position: relative;
        display: block;
        text-align: center;
        vertical-align: middle;
        height: 50px;
        width: 50px;
        line-height: 50px;
        font-size:30px;
    }

    .pinboardcontainer {
        position: relative;
        display: inline-block;
        width: 320px;
        margin-right: 20px;
        margin-bottom: 20px;
    }

    .pinboardiconcontainer {
        display: inline-block;
        height:32px;
        width: 100%;
        padding-top: 3px;
        padding-bottom: 3px;
        text-align: center;
        -webkit-transition: opacity 0.5s;
        transition: opacity 0.5s;
        opacity: 0;
    }

    .pinboardcontainer:hover .pinboardiconcontainer {
        opacity: 1;
    }

    .pinboardicon {
        position: relative;
        display: inline-block;
        border: 0px solid black;
        border-radius: 10px;
        -webkit-border-radius: 10px;
        height: 34px;
        width: 32px;
        background-size: 16px 16px;
        background-position: 8px 0px;
        background-repeat: no-repeat;
        margin-right: 16px;
    }

    .iconexp {
        position: absolute;
        display: inline-block;
        bottom: 0;
        left: 0;
        width: 100%;
        line-height: 18px;
        font-size: 12px;
        font-style: italic;
        opacity: 0;
    }

    .pinboardicon:hover .iconexp {
        opacity: 1;
    }

    .userdiv {
        margin-bottom:10px;
    }

    .sharediv {
        display: inline-block;
        height:46px;
        width:100%;
        margin: 0 auto;
    }

    .userselector {
        height: 46px;
        width: 230px;
    }

    .userselectorsave {
        width: 50px;
        height: 32px;
    }

	</style>
</head>

<body ng-controller="pinboardctrl">

<div id="addbutton" class="material-ripple" data-ripple-color="#2ecc71">
    <span>+</span>
</div>

<div id="maincontainer" style="opacity:0;">
    <div class="pinboardcontainer" ng-repeat="pinboard in pinboards">
        <div class="pinboardiconcontainer">
            <div>
                <div class="pinboardicon" ng-click="ChangeTitle($event)" style="background-image: url('pinboard/titlechange.png');"><div class="iconexp">Title</div></div>
                <div class="pinboardicon" ng-click="OpenUserTab($event, pinboard.usernames)" style="background-image: url('pinboard/userchange.png');"><div class="iconexp">Users</div></div>
                <div class="pinboardicon" ng-click="Delete($event, pinboard.id)" style="background-image: url('pinboard/delete.png');"><div class="iconexp">Delete</div></div>
            </div>
        </div>
        <div class="pinboard" ng-dblclick="OpenPinboard(pinboard.id)">
            <div class="title" ng-focus="titleFocus($event)" ng-blur="titleBlur($event, pinboard.id)">{{pinboard.name}}</div>
            <div class="userdiv">
                <div usershower ng-repeat="username in pinboard.usernames" username="username" users="users" class="usershower"></div>
            </div>
            <div class="sharediv" sharediv usernamesloaded="usernamesloaded" usernames="usernames" style="display:none;">
                <select class="userselector" multiple="multiple">
                </select>
                <input type="button" class="userselectorsave" value="Save" ng-click="SaveShareUser($event, pinboard)"/>
            </div>
        </div>
    </div>
</div>
</body>

<script src="pinboard/jquery.min.js"></script>
<script src="pinboard/comm.js"></script>
<script src="pinboard/angular.min.js"></script>
<script src="pinboard/select2.min.js"></script>
<script src="pinboard/ripple.min.js"></script>

<script>

var comm;
var app = angular.module('pinboard', []);
var addfinished = false;

var authErrorCallback = function(error) {
	alert("Authentication error");
	comm.redirectToLogin();
}

var authSuccessCallback = function() {
    comm.makeRequest({
		"url" : "api/pinboard/getpinboards",
		"type" : RequestMethod.GET,
		"success" : function(response) {
			var scope = GetScope();
			scope.pinboards = response;
			scope.$apply();
            $('#maincontainer').animate({opacity: 1}, 1000);
            $('#addbutton').animate({top: "5%"}, {duration: 1000});
            setTimeout(function() {
                addfinished = true;
            }, 1200);
		}
	});

    //Load users
    comm.makeRequest({
        "url" : "api/pinboard/usernames",
        "type" : RequestMethod.GET,
        "success" : function(response) {
            var scope = GetScope();
            scope.usernames = response;
            scope.usernamesloaded = true;
            scope.$apply();
        }
    });
}

$("#addbutton").click(function() {
    if (!addfinished) return;
    addfinished = false;

    comm.makeRequest({
        "url" : "api/pinboard/createpinboard",
        "type" : RequestMethod.GET,
        "success" : function(response) {
            var scope = GetScope();
            scope.pinboards.push(response);
            scope.$apply();
            addfinished = true;
        }
    });
});

$(document).ready(function() {

	comm = new Communicator({
		"loginMethod"     	  : LoginMethod.Redirect,
		"authErrorCallback"   : authErrorCallback,
		"authSuccessCallback" : authSuccessCallback
	});
});

app.controller('pinboardctrl', function($scope) {
    $scope.pinboards = [];

    $scope.users = {};
    $scope.usernames = [];

    $scope.title = "";
    $scope.usernamesloaded = false;

    $scope.titleFocus = function($event) {
        var element = $event.currentTarget || $event.srcElement;
        $scope.title = $(element).html().trim();
    }

    $scope.titleBlur = function($event, id) {
        var element = $event.currentTarget || $event.srcElement;
        var currentTitle = $(element).html().trim();
        if (!angular.equals(currentTitle, $scope.title)) {

            var data = {
                "id" : id,
                "name" : currentTitle
            };

            comm.makeRequest({
                "url" : "api/pinboard/changepinboardname",
                "type" : RequestMethod.POST,
                "data" : JSON.stringify(data)
            });
        }
    }

    $scope.ChangeTitle = function($event) {
        var element = $event.currentTarget || $event.srcElement;
        var titleDiv = $(element).parent().parent().parent()
                       .children(".pinboard").eq(0).children(".title").eq(0);
        titleDiv.attr("contenteditable", "true");
        titleDiv.focus();
    }

    $scope.OpenUserTab = function($event, users) {
        if (!$scope.usernamesloaded) return;
        var element = $event.currentTarget || $event.srcElement;
        var sharediv = $(element).parent().parent().parent()
                       .children(".pinboard").eq(0).children(".sharediv").eq(0);

        var selector = sharediv.children(".userselector").eq(0).select2();
        selector.val(users).trigger("change");

        sharediv.css("display", "inline-block");
    }

    $scope.SaveShareUser = function($event, pinboard) {
        var element = $event.currentTarget || $event.srcElement;
        var sharediv = $(element).parent().parent().parent()
                       .children(".pinboard").eq(0).children(".sharediv").eq(0);

        var selector = sharediv.children(".userselector").eq(0).select2();

        sharediv.css("display", "none");

        var usernames = selector.val();

        var data = {
            "id": pinboard.id,
            "users": usernames
        };

        comm.makeRequest({
            "url" : "api/pinboard/changeusers",
            "type" : RequestMethod.POST,
            "data" : JSON.stringify(data),
            "success" : function(response) {
                var scope = GetScope();
                pinboard.usernames = usernames;
                scope.$apply();
            }
        });
    }

    $scope.Delete = function($event, id) {
        var element = $event.currentTarget || $event.srcElement;
        var sharediv = $(element).parent().parent().parent();

        comm.makeRequest({
            "url" : "api/pinboard/deletepinboard",
            "type" : RequestMethod.POST,
            "data" : id,
            "success" : function(response) {
                sharediv.css("display", "none");
            }
        });
    }

    $scope.OpenPinboard = function(id) {
        window.location.href = "pinboard2?id=" + id;
    }
});


app.directive('sharediv', function() {

    function link(scope,element,attr) {

        scope.$watch("usernamesloaded", function() {
            if (scope.usernamesloaded) {
                var data = [];
                for (var i=0; i<scope.usernames.length; i++)
                    data.push({"id": scope.usernames[i].username, "text": scope.usernames[i].realName});

                element.children(".userselector").eq(0).select2({
                    data: data
                });
            }
        });
    }

    return {
        scope : {
            usernamesloaded : '=',
            usernames : '='
        },
        link: link
    };
});

app.directive('usershower', function() {

    function PopulateOwner(scope) {
        var data = scope.users[scope.username];
        scope.ownericon.css("background-image", "url('" + data.pictureURL + "')");
    }

    function renderer(element, scope) {

        if (scope.users[scope.username])
            PopulateOwner(scope);
        else {
            comm.makeRequest({
                "url" : "api/pinboard/userdata",
                "type" : RequestMethod.POST,
                "data" : scope.username,
                "success" : function(response) {
                    scope.users[scope.username] = response;
                    PopulateOwner(scope);
                }
            });
        }
    }

    function link(scope,element,attr) {

        scope.ownericon = $("<div/>", {class: "ownericon"});

        element.append(scope.ownericon);

        renderer(element, scope);

        scope.$watch("username", function() {
            renderer(element, scope);
        });
    }

    return {
        scope : {
            username : '=',
            users: '='
        },
        link: link
    };
});

function GetScope() {
    return angular.element($("body")).scope();
}

</script>
</html>