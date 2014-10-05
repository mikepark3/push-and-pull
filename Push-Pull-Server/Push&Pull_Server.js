//Create Tcp Server for manitto game stage 1
var server = require('net').createServer();


var port = 5000;

//Server start listening
server.listen(port,function(err){
	
	console.log("Stage 1 Server is running on port " + port);
});

//socket array
var stage1_sockets = [];


//Socket connection 
server.on("connection",function(socket){
	console.log("socket connection!!");
	
	stage1_sockets.push(socket);//소켓이 커넥션되면 배열에 넣는다.
	
	socket.on('data',function(package){
		
		var data = package.toString();
		
		//if data contains user information
		if(data[0] == "u"){
			
			data = data.substring(1);
			var user = data.split("`");
			console.log(user);
			
			socket.name = user[0];//소켓에 자신의 이름을 단다.
			socket.dateName = user[1];//소켓에 상대의 이름을 단다.
			
			
			
		}
		//if data contains messages
		else if(data[0] == "m"){
			var message = data.concat("`").concat(socket.name);
			
			stage1_sockets.forEach(function(otherSocket){
				console.log("message send!!");
				
				if(otherSocket == socket){
					console.log(message);
					socket.write(message);
				}
				
				else if(otherSocket.name == socket.dateName){
					console.log(message);
					otherSocket.write(message);

				
				}
			});
		}
		
		else if(data[0] == "s"){
			
			console.log(data);
			stage1_sockets.forEach(function(otherSocket){
				console.log("point send!!");
				
				if(otherSocket == socket){
					console.log(data);
					socket.write(data);
				}
				
				else if(otherSocket.name == socket.dateName){
					console.log(data);
					otherSocket.write(data);
				}
				
			});
		}
		
		else if(data == "v`end"){
			
			
			stage1_sockets.forEach(function(otherSocket){
				
				if(otherSocket.name == socket.dateName){
					console.log(data);
					otherSocket.write(data);
				}
				
			});
		}
	});
	
	socket.on('close',function(){
		
		console.log("socket disconnection");
		var index = stage1_sockets.indexOf(socket);
		stage1_sockets.splice(index,1);
		
	});
	
	socket.on('error',function(err){
		
		console.log(err.message);
	});
	
});

/**
 * upload server
 */
var http = require('http');
var express = require('express');
var app = express();
var fs = require('fs');
var routes = require('./routes');

app.configure(function(){
	
	app.set('port',5001);
	app.use(express.bodyParser({uploadDir: __dirname + '/voice'}));
	app.use(express.limit('10mb'));
	app.use(express.methodOverride());
	app.use(app.router);
	app.use(express.static(__dirname + '/public'));
	
});

app.configure('development', function(){
	
	app.use(express.errorHandler({ dumpExceptions : true, showStack: true}));
});

app.configure('production', function(){
	
	app.use(express.errorHandler());
});

var server = http.createServer(app).listen(app.get('port'), function(){
	console.log("express server is listening on port " + app.get('port'));
});

app.post('/upload_voice',function(req,res){
	
	
	var voice_file = req.files.uploadFile;
	var path = voice_file.path;
	var newPath = __dirname + "\\voice\\"+voice_file.name;
	fs.rename(path,newPath,function(err){
		
		if(err){
			
			console.log("file rename error!!");
			throw err;
		}
		
		res.writeHead(200);
		res.end();
		console.log("voice upload success");
	});
});

app.post('/download_voice',function(req,res){
	
	
	var fileName = req.body.id;
	
	//console.log(req);
	var path = __dirname + "\\voice\\" + fileName + ".3gp";
	fs.readFile(path,function(err,data){
		
		if(err){
			console.log("file read failed!!");
			res.writeHead(400);
			res.end();
			console.log("voice download failed");
		
		}
		
		else{
		
			res.writeHead(200);
			res.end(data);
			console.log("voice download success");
			
		}
	});
});

app.post('/upload_image',function(req,res){
	
	//console.log(req);
	var image_file = req.files.uploadFile;
	var fileName = req.body.id;
	var path = image_file.path;
	var newPath = __dirname + "\\image\\" + fileName+".jpg";
	fs.rename(path,newPath,function(err){
		
		if(err){
			console.log("file rename error!!");
		}
		
		
		else{
			
			res.writeHead(200);
			res.end();
			console.log("image upload success");
		}
		
	});
	
});

app.post('/download_image',function(req,res){
	
	
	
	var fileName = req.body.id;
	
	
	var path = __dirname + "\\image\\" + fileName + ".jpg";
	
	fs.readFile(path,function(err,data){
		
		if(err){
			console.log("file read failed!!");
			res.writeHead(400);
			res.end();
			console.log("image download failed");
		
		}
		
		else{
		
			res.writeHead(200);
			res.end(data);
			console.log("image download success");
			
		}
	});
});

