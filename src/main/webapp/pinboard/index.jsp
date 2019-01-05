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
	<script src="pinboard/jquery.min.js"></script>
	<script src="pinboard/sockjs-0.3.4.js"></script>
    <script src="pinboard/stomp.js"></script>
	<script src="pinboard/comm.js"></script>
	<script src="pinboard/angular.min.js"></script>
	<script src="pinboard/jquery-ui.min.js"></script>
	<link href="pinboard/jquery-ui.min.css" rel="stylesheet"/>
	<link href="pinboard/jquery-ui.structure.css" rel="stylesheet"/>
	<link href="pinboard/style.css" rel="stylesheet"/>
	<link href="pinboard/userdisplay.css" rel="stylesheet"/>
	<link href="pinboard/modal.css" rel="stylesheet"/>
</head>

<body ng-controller="pinboardctrl">

<div id="board">
	<div postit ng-repeat="(id, postit) in postits" class="postit" data="postit" users="users">
	</div>
</div>

<div id="myModal" class="modal">
	<div class="modal-content">
		<div class="modal-header">
			<span class="close" onclick="Close();">×</span>
			<h2>{{title}}</h2>
		</div>
		<div class="modal-body">
			<div id="editarea"></div>
			<div id="filearea"></div>
		</div>
		<div class="modal-footer">
			<div id="saveButton">Save</div>
		</div>
	</div>
</div>

</body>

<script>

var pinboardID = "${pinboardID}";

var modal;
var comm;
var saveProcess = false;
var modalOpened = false;

var pictures = {};
var files = {};

var pictureIndex = 0;
var fileIndex = 0;

var editarea = $("#editarea");
var filearea = $("#filearea");

editarea.dblclick(function(e) {
	var readonly = GetScope().readonly;

	if (readonly) 
		return;
	
	var newText = {
    	"text" : "enter a new text",
    	"left" : e.offsetX,
    	"top" : e.offsetY,
    	"width" : 100,
    	"height" : 100
    };
    
    AddText(newText);
});

function Reseteditarea() {
	pictures = {};
	files = {};
	pictureIndex = 0;
	fileIndex = 0;

	var elements = editarea.children("div");
	for (var i = 0; i < elements.length; i++)
		$(elements[i]).remove();

	elements = filearea.children("div");
	for (var i = 0; i < elements.length; i++)
		$(elements[i]).remove();
}

function DivCreator(data) {
	var myDiv = $("<div/>");
	myDiv.css("position", "absolute");
	myDiv.css("width", data.width + "px");
	myDiv.css("height", data.height + "px");
	myDiv.css("top", data.top + "px");
	myDiv.css("left", data.left + "px");

	var closer = $("<div/>", {class: "closer"});
	closer.html("X");
	myDiv.append(closer);

	return {
		div: myDiv,
		closer: closer
	};
}

function OpenNote(data) {
	Reseteditarea();

	for (var i = 0; i < data.texts.length; i++) {
		AddText(data.texts[i]);
	}

	for (var i = 0; i < data.pictures.length; i++) {
		var im = AddImage(data.pictures[i].url, data.pictures[i]);
		im.addClass("picDivOld");
	}

	for (var i = 0; i < data.files.length; i++) {
		var fi = AddFile(data.files[i]);
		fi.addClass("fileDivOld");
	}
}

function AddFile(data) {

	var fileDiv = $("<div/>");
	fileDiv.css("display", "inline-block");
	fileDiv.css("position", "relative");
	fileDiv.css("padding", "5px 15px 5px 5px");
	fileDiv.css("background", "white");
	fileDiv.css("box-sizing", "border-box");
	fileDiv.css("border", "1px solid darkblue");
	fileDiv.css("border-radius", "10px");
	fileDiv.css("-webkit-border-radius", "10px");

	var closer = $("<div/>", {class: "closer"});
	closer.html("X");
	fileDiv.append(closer);

	var link = $("<a/>", {href: data.url});
	link.html(data.name);

	fileDiv.append(link);

	var readonly = GetScope().readonly;
	var editmode = GetScope().editmode;

	link.attr("download", data.name);

	if (!readonly) {

		if (!editmode)
			link.attr("contenteditable", "true");

		fileDiv.hover(
			function() {
				closer.css("visibility", "visible");
			}, function() {
				closer.css("visibility", "hidden");
			}
		);

		closer.click(function() {
			fileDiv.remove();
		});
	}

	filearea.append(fileDiv);
	return fileDiv;
}

function AddText(data) {
	var created = DivCreator(data)
	var textDiv = created.div;
	var closer = created.closer;
	textDiv.addClass("textDiv");
	textDiv.css("padding", "15px");

	var inner = $("<textarea/>")
	inner.css("width", "100%");
	inner.css("border", "0");
	inner.css("height", "100%");
	inner.css("resize", "none");
	inner.attr("disabled", "true");
	inner.val(data.text);
	inner.prop("disabled", true);

	var readonly = GetScope().readonly;

	if (!readonly) {
		inner.prop("disabled", false);

		textDiv.dblclick(function(e) {
			return false;
		});

		inner.focus(function() {
			textDiv.css("background", "pink");
		}).blur(function(){
			textDiv.css("background", "");
		});

		textDiv.resizable({
			containment: "parent",
			handles: "all"
		});

		textDiv.draggable({
			containment: "parent"
		});

		textDiv.hover(
			function() {
				closer.css("visibility", "visible");
			}, function() {
				closer.css("visibility", "hidden");
			}
		);

		closer.click(function() {
			textDiv.remove();
		});
	}

	textDiv.append(inner);
	editarea.append(textDiv);
}

/*image: base64 or URL, 
layout: map contains left,top,width,height info*/
function AddImage(image, layout) {
	var created = DivCreator(layout);
	var picDiv = created.div;
	var closer = created.closer;
	picDiv.css("background-image", "url('" + image + "')");
	picDiv.css("background-size", "100% 100%");
	picDiv.css("background-repeat", "no-repeat");

	var readonly = GetScope().readonly;

	if (!readonly) {
		picDiv.resizable({
			containment: "parent",
			handles: "all"
		});

		picDiv.draggable({
			containment: "parent"
		});

		picDiv.hover(
			function() {
				closer.css("visibility", "visible");
			}, function() {
				closer.css("visibility", "hidden");
			}
		);

		closer.click(function() {
			picDiv.remove();
		});
	}

	editarea.append(picDiv);
	return picDiv;
}

var app = angular.module('pinboard', []);

app.controller('pinboardctrl', function($scope) {
	$scope.postits = {};

	$scope.users = {};

	$scope.title = "Add Note";
	$scope.readonly = false;
	$scope.editmode = false;

	$scope.current = {};

	$scope.removeById = function(id) {
		delete $scope.postits[id];
	}
});

app.directive('postit', function() {

	var animSpeed = 1; //1px each milisecond

	function PopulateOwner(scope) {
		var data = scope.users[scope.data.ownerName];
		scope.ownericon.css("background-image", "url('" + data.pictureURL + "')");
		scope.ownername.html(data.realName);
		scope.ownerdate.html(scope.data.date);
	}

	function renderer(element, scope) {

		element.css("top", scope.data.top + "px");
        element.css("left", scope.data.left + "px");

        if (scope.users[scope.data.ownerName]) {
			PopulateOwner(scope);
        }
        else {
        	comm.makeRequest({
				"url" : "api/pinboard/userdata",
				"type" : RequestMethod.POST,
				"data" : scope.data.ownerName,
				"success" : function(response) {
					scope.users[scope.data.ownerName] = response;
					PopulateOwner(scope);
				}
			});
        }

        var text = "";
        for (var i = 0; i < scope.data.texts.length; i++)
        	text += scope.data.texts[i].text  + "<br>";

        text = text.split("\n").join("<br>");

        scope.textdiv.html(text);
        scope.detaildiv.html(scope.data.pictures.length + " adet resim, " + scope.data.files.length + " adet dosya");
	}

    function link(scope,element,attr) {
    	scope.dropActivated = false;	//it will be true when paper will be dropped

        element.dblclick(function(e) {
        	if (modalOpened) return;
        	modalOpened = true;

    		for (var i = 0; i < scope.data.pictures.length; i++)
    			scope.data.pictures[i].url = scope.data.pictures[i].url.replace("!{id}", scope.data.id);

    		for (var i = 0; i < scope.data.files.length; i++)
    			scope.data.files[i].url = scope.data.files[i].url.replace("!{id}", scope.data.id);

    		var username = comm.getUserData().username;
    		var ownername = scope.data.ownerName;
    		var parentscope = GetScope();

    		if (angular.equals(username, ownername)) {
    			parentscope.title = "Edit Note";
    			parentscope.readonly = false;
    			parentscope.editmode = true;
    			$("#saveButton").css("display", "inline-block");
    		}
    		else {
    			parentscope.title = "Read Note";
    			parentscope.readonly = true;
    			parentscope.editmode = false;
    			$("#saveButton").css("display", "none");
    		}

    		parentscope.current = scope.data;
    		parentscope.$apply();

    		OpenNote(scope.data);

    		modal.css("display", "block");
    		return false;
        });

        scope.pinpic = $("<i/>", {class: "pin"});
        scope.notediv = $("<div/>", {class: "note"});

        scope.textdiv = $("<div/>", {class: "text"});
        scope.ownerdiv = $("<div/>", {class: "owner"});
        scope.detaildiv = $("<div/>", {class: "detail"});

        scope.ownericon = $("<div/>", {class: "ownericon"});
        scope.ownername = $("<span/>", {class: "ownername"});
        scope.ownerdate = $("<span/>", {class: "ownerdate"});

        scope.ownerdiv.append(scope.ownericon);
        scope.ownerdiv.append(scope.ownername);
        scope.ownerdiv.append(scope.ownerdate);

        scope.notediv.append(scope.ownerdiv);
        scope.notediv.append(scope.textdiv);
        scope.notediv.append(scope.detaildiv);

        element.append(scope.pinpic);
        element.append(scope.notediv);

        scope.pinpic.draggable({
        	containment: $("body"),
        	revert: "valid",
        	start: function(event, ui) {
        		if (scope.dropActivated)
        			return false;
        	},
        	revert: function(droppableObj)
			{
				//if false then no revert, so paper will fall down (but check if this user has the right to do so)
				if(droppableObj === false)
				{
					if (angular.equals(scope.data.ownerName, comm.getUserData().username)) {
						scope.dropActivated = true;
				    	return false;
					}
					else {
						scope.dropActivated = false;
					    return true;
					}
				}
				//in this case we revert the object, so paper will stay at the same position
				else
				{
					scope.dropActivated = false;
				    return true;
				}
  			},
  			stop: function(event, ui) {
        		if (scope.dropActivated) {

        			var mainScope = GetScope();
					var id = scope.data.id;

					if(!angular.equals(scope.data.ownerName, comm.getUserData().username))
						return;

        			comm.makeRequest({
						"url" : "api/pinboard/delete",
						"type" : RequestMethod.POST,
						"data" : id
					});

        			var pin = ui.helper;
        			var paper = pin.parent();
        			var pinPos = pin.offset();
        			var height = parseInt($("body").css("height"));
        			pin.css("position","fixed");
        			pin.css("top", pinPos.top + "px");
        			pin.css("left", pinPos.left + "px");
        			pin.css("z-index", 9);
        			paper.css("z-index", 8);
        			
        			pin.detach();
					$("body").append(pin);

					pin.animate({"top": (height + 100) + "px"}, 800, function() {
						pin.remove();
					});

					paper.animate({"top": (height + 10) + "px"}, 1000, function() {
						mainScope.removeById(id);
						mainScope.$apply();
					});
        		} 
        	},
        });

        element.draggable({
        	containment: $("body"),
        	delay: 100,
        	start: function(event, ui) {
        		if (scope.dropActivated)
        			return false;
        	},
        	stop: function(event, ui) {

        		scope.data.left = parseInt(ui.position.left);
        		scope.data.top = parseInt(ui.position.top);

        		var data = {
        			"id": scope.data.id,
        			"left" : scope.data.left,
        			"top": scope.data.top
        		};

        		comm.makeRequest({
					"url" : "api/pinboard/place",
					"type" : RequestMethod.POST,
					"data" : JSON.stringify(data)
				});
        	}
        });

        element.droppable({
        	"accept": ".pin"
        });

        renderer(element, scope);

        scope.$watch("data.id", function() {
	    	renderer(element, scope);
	    });

	    scope.$watch("data.left", function(newValue, oldValue) {
	    	var oldPos = parseInt(element.css("left"));
	    	var changePos = Math.abs(oldPos - newValue);
	    	var duration = changePos / animSpeed;
	    	element.animate({"left": newValue + "px"}, duration);
	    });

	    scope.$watch("data.top", function(newValue, oldValue) {
	    	var oldPos = parseInt(element.css("top"));
	    	var changePos = Math.abs(oldPos - newValue);
	    	var duration = changePos / animSpeed;
	    	element.animate({"top": newValue + "px"}, duration);
	    });

	    scope.$watch("data.texts", function(newValue, oldValue) {
	    	if (angular.equals(newValue, oldValue)) return;
			var text = "";
	        for (var i = 0; i < scope.data.texts.length; i++)
	        	text += scope.data.texts[i].text + "<br>";

	        text = text.split("\n").join("<br>");

	        scope.textdiv.html(text);
        });

        scope.$watch("data.pictures.length", function(newValue, oldValue) {
            scope.detaildiv.html(scope.data.pictures.length + " picture(s), " + scope.data.files.length + " file(s)");
        });

        scope.$watch("data.files.length", function(newValue, oldValue) {
            scope.detaildiv.html(scope.data.pictures.length + " picture(s), " + scope.data.files.length + " file(s)");
        });

        scope.$watch("data.date", function(newValue, oldValue) {
			scope.ownerdate.html(scope.data.date); 
		});
    }

    return {
        scope : {
            data : '=',
            users: '='
        },
        link: link
    };
});

var authErrorCallback = function(error) {
	alert(error);
	comm.redirectToLogin();
}

var loadFinished = false;

var updateFunc = function(response) {

	if (!loadFinished) return;
	var data = JSON.parse(response.body);

	var scope = GetScope();
	var noteID = data.noteID;

	if (angular.equals(data.actionType, "DELETE")) {
		delete scope.postits[noteID];
	}
	else if (angular.equals(data.actionType, "CREATE")) {
		scope.postits[noteID] = data.obj;
	}
	else if (angular.equals(data.actionType, "PLACE")) {
		scope.postits[noteID].left = data.obj.left;
		scope.postits[noteID].top = data.obj.top;
	}
	else if (angular.equals(data.actionType, "EDIT")) {
		scope.postits[noteID].texts = data.obj.texts;
		scope.postits[noteID].pictures = data.obj.pictures;
		scope.postits[noteID].files = data.obj.files;

		if (angular.equals(scope.current.id, noteID))
		    OpenNote(scope.postits[noteID]);

	}

	scope.$apply();
}

function Load() {

	comm.makeRequest({
		"url" : "api/pinboard/load",
		"type" : RequestMethod.POST,
		"data" : pinboardID,
		"success" : function(response) {
			var scope = GetScope();
			scope.postits = response;
			scope.$apply();
			loadFinished = true;
		},
		"error": function(response) {
			setTimeout(function(){Load()}, 15000);
		}
	});
}

var authSuccessCallback = function() {

	modal = $("#myModal");
	modal.css("display", "none");

	$("#saveButton").click(function() {
		Save();
	});

	var topMax = 0;
	var leftMax = 0;
	var topMin = (parseInt($("#board").css("height")) - parseInt($("body").css("height"))) * (-1);
	var leftMin = (parseInt($("#board").css("width")) - parseInt($("body").css("width"))) * (-1);

	$("#board").draggable({
		 drag: function(event, ui) {
		 	var left = ui.position.left;
		 	var top = ui.position.top;

		 	if (left < leftMin)
		 		ui.position.left = leftMin;
		 	else if (left > leftMax)
		 		ui.position.left = leftMax;

		 	if (top < topMin)
		 		ui.position.top = topMin;
		 	else if (top > topMax)
		 		ui.position.top = topMax;
		}
	});

	Load();
}

$(document).ready(function() {
	comm = new Communicator({
		"loginMethod"     	  : LoginMethod.Redirect,
		"authErrorCallback"   : authErrorCallback,
		"authSuccessCallback" : authSuccessCallback,
		"wsConnURL"			  : "/wspinbconn",
		"subscriptions" 	  : [
									["/wspinbsubs/" + pinboardID + "/!username!", updateFunc]
								]
	});

    var drop = document.getElementById("editarea");
    var filearea = document.getElementById("filearea");
  	
    function cancel(e) {
		if (e.preventDefault) { e.preventDefault(); }
		return false;
    }
  
    //Tells the browser that we can drop on this target
    addEventHandler(drop, 'dragover', cancel);
    addEventHandler(drop, 'dragenter', cancel);
    addEventHandler(filearea, 'dragover', cancel);
    addEventHandler(filearea, 'dragenter', cancel);

	addEventHandler(filearea, 'drop', function (e) {
		e = e || window.event; // get window.event if e argument missing (in IE)   

		if (e.preventDefault) 
			e.preventDefault(); // stops the browser from redirecting off to the image.

		var dt = e.dataTransfer;

		for (var i=0; i<dt.files.length; i++) {
			var file = dt.files[i];
			var reader = new FileReader();
			  
			addEventHandler(reader, 'loadend', function(e, file) {

				var im = AddFile({
					url: "",
					name: file.name
				});

				var index = fileIndex;

				im.addClass("fileDivNew");
				im.attr("index", index);
				files[index] = file;

				fileIndex++;
			   
			}.bindToEventHandler(file));

			reader.readAsDataURL(file);
		}
		return false;
	});

    addEventHandler(drop, 'drop', function (e) {
		e = e || window.event; // get window.event if e argument missing (in IE)   

		if (e.preventDefault) 
			e.preventDefault(); // stops the browser from redirecting off to the image.

		var dt = e.dataTransfer;
		var files = dt.files;
		for (var i=0; i<files.length; i++) {
			var file = files[i];
			var reader = new FileReader();
			  
			addEventHandler(reader, 'loadend', function(e, file) {

				//check if it is a picture
				if (file.type.indexOf("image") != 0)
					return;

			    var layout = {
					"left" : 0,
					"top" : 0,
					"width": 50,
					"height": 50
				};

				var index = pictureIndex;

				var im = AddImage(this.result, layout);
				im.addClass("picDivNew");
				im.attr("index", index);
				pictures[index] = file;

				pictureIndex++;
			   
			}.bindToEventHandler(file));

			reader.readAsDataURL(file);
		}
		return false;
	});
});

Function.prototype.bindToEventHandler = function bindToEventHandler() {
	var handler = this;
	var boundParameters = Array.prototype.slice.call(arguments);
	//create closure
	return function(e) {
		e = e || window.event; // get window.event if e argument missing (in IE)   
		boundParameters.unshift(e);
		handler.apply(this, boundParameters);
	}
};

function addEventHandler(obj, evt, handler) {
    if(obj.addEventListener) {
        // W3C method
        obj.addEventListener(evt, handler, false);
    } else if(obj.attachEvent) {
        // IE method.
        obj.attachEvent('on'+evt, handler);
    } else {
        // Old school method.
        obj['on'+evt] = handler;
    }
}

function Close() {
	modal.css("display", "none");
	modalOpened = false;
}

$("body").dblclick(function(e) {
	if (modalOpened) return;
    modalOpened = true;
    
    var scope = GetScope();
    scope.current = {};
    scope.current.left = e.pageX;
    scope.current.top = e.pageY;
    scope.current.texts = [{
    	"text" : "enter a text",
    	"left" : 0,
    	"top" : 0,
    	"width" : 100,
    	"height" : 100
    }];
    scope.current.pictures = [];
    scope.current.files = [];
    scope.readonly = false;
    scope.editmode = false;
    scope.title = "Add Note";
    scope.$apply();
	
	OpenNote(scope.current);
    $("#saveButton").css("display", "inline-block");
    modal.css("display", "block");
});

function Save() {

	if (saveProcess) return;
	saveProcess = true;

	var scope = GetScope();
	var isEditing = scope.editmode;

	var data = new FormData();

	var json = {};

	json.left = scope.current.left;
	json.top = scope.current.top;

	/*Get texts*/
	json.texts = [];
	var texts = $("#editarea").children(".textDiv");
	for (var i = 0; i < texts.length; i++) {
		var current = $(texts[i]);
		var text = $(current.children("textarea")[0]).val();
		var item = {
			"text": text,
			"left": parseInt(current.css("left")),
			"top": parseInt(current.css("top")),
			"width": parseInt(current.css("width")),
			"height": parseInt(current.css("height"))
		};
		json.texts.push(item);
	}

	/*Get old pictures*/

	//current old pictures + deleted ones
	var oldPicAmount = scope.current.pictures.length;

	json.pictures = [];
	var oldPictures = $("#editarea").children(".picDivOld");
	for (var i = 0; i < oldPictures.length; i++) {
		var current = $(oldPictures[i]);

		var prefix = "pinboardpict/"
		var url = current.css("background-image")
		url = url.substr(url.indexOf(prefix));
		url = url.substr(0, url.indexOf("\""));

		var item = {
			"url": url,
			"left": parseInt(current.css("left")),
			"top": parseInt(current.css("top")),
			"width": parseInt(current.css("width")),
			"height": parseInt(current.css("height"))
		};
		json.pictures.push(item);
	}

	/*Get new pictures*/
	var newPictures = $("#editarea").children(".picDivNew");
	for (var i = 0; i < newPictures.length; i++) {
		var current = $(newPictures[i]);
		var item = {
			"url": "pinboardpict/!{id}" + "_pic_" + (i + oldPicAmount),
			"left": parseInt(current.css("left")),
			"top": parseInt(current.css("top")),
			"width": parseInt(current.css("width")),
			"height": parseInt(current.css("height"))
		};
		json.pictures.push(item);
		data.append("pic_" + (i + oldPicAmount), pictures[current.attr("index")]);
	}

	//current old files + deleted ones
	var oldFileAmount = scope.current.files.length;

	/*Get old files*/
	json.files = [];
	var oldFiles = $("#filearea").children(".fileDivOld");
	for (var i = 0; i < oldFiles.length; i++) {
		var current = $(oldFiles[i]);

		var link = $(current.children("a")[0]); 
		var url = link.attr("href");
		var name = link.html();

		var item = {
			"url": url,
			"name": name
		};

		json.files.push(item);
	}

	/*Get new files*/
	var newFiles = $("#filearea").children(".fileDivNew");
	for (var i = 0; i < newFiles.length; i++) {
		var current = $(newFiles[i]);

		var link = $(current.children("a")[0]); 
		var name = link.html();

		var item = {
			"url": "pinboardpict/!{id}" + "_file_" + (i + oldFileAmount),
			"name": name
		};

		json.files.push(item);
		data.append("file_" + (i + oldFileAmount), files[current.attr("index")]);
	}

	data.append("json", JSON.stringify(json));
	data.append("pictureAmount", newPictures.length);
	data.append("fileAmount", newFiles.length);
	data.append("editmode", isEditing);
	data.append("id", isEditing ? scope.current.id : null);
	data.append("pinboardID", pinboardID);

	comm.makeRequestWithFile({
		"url" : "api/pinboard/save",
		"data" : data,
		"success" : function (response) {
			json.id = response.id;
			json.date = response.date;
			json.ownerName = comm.getUserData().username;

			for (var i = 0; i < json.pictures.length; i++)
				json.pictures[i].url = json.pictures[i].url.replace("!{id}", json.id); 

			for (var i = 0; i < json.files.length; i++)
				json.files[i].url = json.files[i].url.replace("!{id}", json.id); 

			scope.postits[json.id] = json;
            scope.$apply();
            modal.css("display", "none");
            saveProcess = false;
        	modalOpened = false;
		},
		"error": function (response) {
			modal.css("display", "none");
		    alert(response.responseText);
		    saveProcess = false;
		    modalOpened = false;
		}
	});
}

function GetScope() {
    return angular.element($("body")).scope();
}

</script>
</html>