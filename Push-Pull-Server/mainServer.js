
//chatting을 위한 tcp server
//var tcp_server = require('net').createServer();

//file control module
var fs = require('fs');
//http server
var http = require('http');

//express server
var express = require("express");

//inherit http server
var app = express();

//configure routing location
var routes = require('./routes');
/*var app = module.exports = express.createServer();*/

//configuration
app.configure(function(){
	
	app.set('views', __dirname + '/views');
	//app.set('view engine', 'jade'); jade방식 engine탑재
	app.set('view engine', 'ejs');
	app.set('port',4000);
	app.use(express.bodyParser({uploadDir: __dirname + '/images'})); //사진파일 업로드를 위한 설정
	app.use(express.limit('10mb')); //용량 제한 설정
	app.use(express.methodOverride());
	app.use(express.cookieParser('manitto'));
	app.use(express.session({
		
		secret : 'manitto', //my secret String
		maxAge : 3600000
	}));
	
	app.use(app.router);
	app.use(express.static(__dirname + '/public'));
});

app.configure('development', function(){
	
	app.use(express.errorHandler({ dumpExceptions : true, showStack: true}));
});

app.configure('production', function(){
	
	app.use(express.errorHandler());
});


//creating http server with express module
var server = http.createServer(app).listen(app.get('port'), function(){
	console.log("express server is listening on port " + app.get('port'));
});

//page Routing

//log-in page
app.get('/',  function(req, res){
  res.render('index', { title: 'Express' });
  
});

//sign-in page
app.get('/sign_in',routes.sign_in);

//menu page
app.get('/menu',function(req,res){

	var id = req.session.user_id;
	
	res.render('menu', {
		
		title : 'menu',
		id    :  id
		
			
	});
		
});

//game_on_manitto page
app.get('/game_on_manitto',function(req,res){
	
	var id = req.session.user_id;
	
	res.render('game_on_manitto', {
		
		title : "game_on_manitto",
		id    : id
	});
});

//game_on_manitto image data trasfer method = post
app.get('/game_on_manitto/image',function(req,res){

	console.log(req);
	var name = req.query.room;
	var path ='images/' + name + '.jpg';
	
	fs.readFile(path,function(err,data){
		
		if(err){
			
			console.log("image trasfer error!!");
			console.log(name);
			res.end("image not found!! enter F5 or try again!!");
		}
		
		else{
			
			res.writeHead(200, {'Content-Type' : 'image/jpeg'});
			res.end(data);
		}
		
	});
	
});

//game_on_reciever page
app.get('/game_on_reciever',function(req,res){
	
	var id = req.session.user_id;
	
	res.render('game_on_reciever', {
		
		title : "game_on_reciever",
		id    : id,
		area  : '',
		score : 0
		
	});
});

//game_on_reciever image data transfer method = post
app.post('/game_on_reciever',function(req,res){
	console.log(req);
	console.log("image request");
	var imageFile = req.files.image;
	
	if(imageFile){
		
		var name = req.body.room;
		var path = imageFile.path;
	
		var type = imageFile.type;
		
		if(type.indexOf('image') != -1){
			//이미지의 경로와 이름을 수정한다.
			
			var outputPath = __dirname + '\\images\\' + name + '.jpg';
			
			var id = req.session.user_id;
			fs.rename(path,outputPath,function(err){
				
				
			});
			
			res.render('game_on_reciever', {
				
				title : "game_on_reciever",
				id    : id,
				area  : req.body.area,
				score : req.body.score
				
			});
			
			
		}
		
		else{
			//이미지가 아니면 파일이름을 제거
			fs.unlink(path,function(err){
				
				
			});
	
			res.render('game_on_reciever', {
				
				title : "game_on_reciever",
				id    : id,
				area  : req.body.area,
				score : req.body.score
				
			});
		}
	}
	
	res.render('game_on_reciever', {
		
		title : "game_on_reciever",
		id    : id,
		area  : req.body.area,
		score : req.body.score
		
	});
	

});


//session page
app.post('/session', function(req,res){
	
	req.session.user_id = req.body.chat_id; //세션정보에 요청에서 온 아이디 저장
	req.session.pw = req.body.password; //세션정보에 요청에서 온 패스워드 저장
	
	
	//요청에서 온 아이디와 패스워드를  db정보와 매칭
	login_db(req.body.chat_id,req.body.password,function(){ //성공했을 때의 콜백 함수
		
		
		connected_socket.forEach(function(otherSocket){ //연결된 함수배열을 순회하면서 소켓이름과 요청에서 넘어온 아이디가 같으면 성공이벤트 수행 
			
			if(otherSocket.name == req.body.chat_id){
				
				otherSocket.emit('login_success');
				res.redirect('/menu');
			};
	
		});
	}, 
	
	function(){ //실패했을 떄의 콜백 함수
		
		connected_socket.forEach(function(otherSocket){
			
		
			if(otherSocket.name == req.body.chat_id){
				
				otherSocket.emit('login_failed'); //db매칭이 실패하면 소켓이름과 요청에서 넘어온 아이디를 매핑하여 실패 이펜트를 수행
				res.redirect('/');
			}
		
		});
	
	});
});
	


//mysql db 생성
var mysql = require('mysql');

//데이터베이스와 연결
var client = mysql.createConnection({
	
	host : 'localhost',
	user: 'root',
	password:'mysql'
});

client.connect();

//socketio 생성
var io = require('socket.io').listen(server);
//불필요한 로그 제거
io.set('log level', 2);
//로그인을 시도하는 소켓을 담기 위한 전역배열변수
var connected_socket=[];

//로그인 된 소켓을 담기위한 전역배열변수
var login_socket=[];

//socketio를 통한 socket통신 
io.sockets.on('connection',function(socket){
	
	console.log("Socket connection Success");
	
	
	//manittodb connect
	client.query('use manittodb', function(error,result,fields){
		
		if(error){
			console.log('DB connection Failed!!');
		}
		
		else{
			console.log('DB connection Success!');
		}
	});
	
	//login Event
	socket.on('login',function(data){
		
		//삭제대기 코드
		/*var id = data[0]; //id추출
		var pw = data[1]; //pw추출
		console.log(data);
		console.log(id);
		console.log(pw);
		
		//일치하는 id를 db에서 탐색하는 method 호출
		login_db(id,pw,function(){
			
			//성공시 호출
			socket.emit('login_success');
			
			
		
		}, 
		
		function(){
			//Fail떄  호출
			socket.emit('login_failed');
		
		});
		*/
		
		socket.name = data[0];//data에서 id값을 추출하여 소켓에 이름을 붙여놓는다.
		connected_socket.push(socket); //로그인 시도하는 소켓을 배열에 담는다.
		
		
		
	});
	
	//sign in Event
	socket.on('sign', function(data){
		
		var id = data[0];
		var pw = data[1];
		
		client.query('INSERT INTO user_list(id,pwl) values(?,?)',[id,pw], function(err,rows,fields){
			
		
			sign_db(err,function(){
				//회원가입 성공시 콜백 함수
				socket.emit('sign_success');
			}, function(){
				//회원가입 실패시 콜백함수
				socket.emit('sign_failed');
			});
			
			
		});
	});
	
	//menu에서 start game버튼을 누르면 수행되는 이벤트
	socket.on('ready_game',function(id){
		

		socket.name = id;
		login_socket.push(socket);
		
	
	});
	
	//신청자로부터 받은 한줄메시지 신청을 랜덤으로 다른 사용자에게 전송하는 이벤트
	socket.on('start_game', function(message){
		
		socket.apply = "true"; //신청한 지원자는 소켓에 표시를 해서 랜덤으로 돌릴때 선택하지 않게끔 한다.
	

		var target;
		
		login_socket.forEach(function(otherSocket){ //login된 소켓들을 돌면서 함수실행
			
			if(otherSocket != socket && otherSocket.apply != "true"){
				
				target = otherSocket;
			}
		});
		
		//callback함수에 성공시와 실패시의 함수를 인자로 넣어 흐름제어를 한다.
		callback(function(){
		
		
		
			if(target != undefined && target.apply != "true" && target != socket){
			
				return "true";
			}
		
		
			else{
				
				return "failed";
			}
		
	
		
		}, function(){
			target.apply = "true"; //신청이 겹치지 않도록 플래그 설정
			target.emit('confirm_game',message,socket.name); //조건에 맞을때 상대방에게 마니또 확인 이벤트 발생
		},
	
		function(){
		
			socket.apply = "false";//다시 선택할 수 있게끔 flag를 바꿔준다.
			
			socket.emit('full_apply'); //실패시 정원초과 이벤트 발생
		});
	});
	
	
	
	//상대방이 게임을 거부했을 때 발생하는 이벤트
	socket.on('refuse_game',function(id){
		
		socket.apply = "false"; //reject했으므로 신청받을 수 있게 플래그를 바꿔준다.
		login_socket.forEach(function(target){
			
			if(target.name == id){
				
				target.apply = "false"; //거절한 상대로부터 신청자의 id를 받아서 로그인 소켓 묶음에서 탐색 후 apply속성을 false로 바꿔준다.
				target.emit('reject_game');
			}
			
		});
	});
	
	//상대방이 게임을 승인했을 때
	socket.on('start_chat',function(id){
		
		
		
		var couple = []; //tcp socket이 짝을 찾기 위해 아이디 정보를 배열에 저장
		
		couple.push(socket.name);//아이디 푸시
		couple.push(id);//아이디 푸시
		
		matching.push(couple); //마니또 매칭 정보를 담는 전역배열에 푸시
		
		var random = Math.floor(Math.random() * 2);
		
		if(random == 0){
			
			socket.emit('go_chat',"manitto");
			
			login_socket.forEach(function(otherSocket){
				
				if(otherSocket.name == id){
					
					otherSocket.emit('go_chat',"reciever");
				}
			});
		}
		
		else{
			
			socket.emit('go_chat',"reciever");
			
			login_socket.forEach(function(otherSocket){
				
				if(otherSocket.name == id){
					
					otherSocket.emit('go_chat',"manitto");
				}
			
			});
		}
		
		
	});
	
});

//매칭 정보를 저장할 전역배열
var matching = [];

//채팅을 위한 tcp 소켓을 담는 배열
var chat_socket=[];


//tcp server listening socket.io의 default가 tcp socket 서버이다.
var tcp_io = require('socket.io').listen(5000,function(err){
	
	console.log("tcp server is listening on port 5000");
});

//socket loglevel 2로 설정하여 불필요한 로그 제거
tcp_io.set('log level',2);

//tcp socket connection

tcp_io.sockets.on('connection',function(socket){
	
	console.log("tcp connection success!");
	
	
	//socket에 사용자 아이디를 등록하기 위한 이벤트
	socket.on('register_id',function(id){
		
		
		socket.name = id;
		chat_socket.push(socket); //chatting하기위해 연결된 소켓을 담아놓는다.
		
		//var message = id + " is entered!!";
		
		//socket.emit('get_message', message);
		console.log("register success");
	});
	
	//tcp socket을 매핑하는 이벤트
	socket.on('find_manitto',function(id){
		console.log("find manitto");
		var target; //target 소켓을 저장할 변수
		
		matching.forEach(function(data){
			
		    var temp1 = data[0]; //배열에서 아이디 정보를 뽑아낸다
			var temp2 = data[1];
			
			
			if(temp1 == id){
				
				target = temp2;
			}
			
			else if(temp2 == id){
				
				target = temp1;
			}
		});
		
		
		
		if(target != undefined){
			
			chat_socket.forEach(function(otherSocket){ //log in 된 tcp socket을 순회하면서 target의 아이디를 탐색하여 채팅가능하게 묶는다.
				
				if(otherSocket.name == target){
					
					
					socket.join(target);
					socket.set('room',target);//socket에 방을 연결하고 target의 아이디로 room을 설정한다.
			
				
					otherSocket.join(target);
					otherSocket.set('room',target); 
					
					
					var intro_message = socket.name + " is entered!! " + "\n" + otherSocket.name + " is entered!! " + "\n";
					//tcp_io.sockets.emit('get_message',intro_message);
					socket.get('room',function(error,room){
				
						tcp_io.sockets.in(room).emit('get_message','',target); //syntax error라고 되어있지만 io.sockets.in(room)메소드이다. room에 해당하는 소켓에 이벤트를 실행해 준다.
					});
					
					//tcp_io.sockets.emit('get_message',intro_message);
				}
			});
		}
		
	});
	
	
	
	//message가 왔을 때의 이벤트
	socket.on('message_on',function(message, room){
		console.log("message on");
		console.log(room);
		tcp_io.sockets.in(room).emit('get_message',message,room);
	});
	
	//reciever가 점수를 부여했을 때의 이벤트
	socket.on('input_score',function(score,room){
		
		console.log("input score");
		console.log(score);
		console.log(room);
		tcp_io.sockets.in(room).emit('update_score',score,room);
		
	});
	
	//reciever가 사진을 보냈을 때 이벤트

	
	//manitto가 사진을 얻을때의 점수 확인 이벤트
	socket.on('confirm_score',function(score){
		
			console.log(score);
			if(parseInt(score) >= 200){
				socket.emit('permit_photo',"true");
			
			}
			else{
				
				socket.emit('permit_photo',"false");
			}
		
	});
	

	
	socket.on('register_photo',function(flag,room){
		
		tcp_io.sockets.in(room).emit('photo_flag',flag);

	});
	//socket이 끊어 졌을때의 이벤트
	
	/*socket.on('disconnect',function(){
		
		var index = chat_socket.indexOf(socket);
		chat_socket.splice(index,1);
	});*/
	
});


//log-in event 호출시 id,password,콜백 함수들을 넘겨받아 DB단의 id,password와 비교하여 login여부를 처리하는 함수
function login_db(id,pw,success,failed){
	
	var db_id;
	var db_pw;
	var result;
	
	client.query('Select * FROM user_list WHERE id = ? AND pwl = ?' ,[id,pw], function(err, rows, fields){
		
		if(rows[0] != undefined){
		 db_id = rows[0].id;
		 db_pw = rows[0].pwl;
		}
	
		//console.log(db_id);
		//console.log(id);
		//console.log(db_id == id);
		//console.log(db_pw == pw);
		
		if(db_id == id && db_pw == pw){
			
			
			 //if log-in success
			 success();
			
		}
		
		else{
			//if log-in failed
			 failed();
		}
	});
	
	
};


//sing-in event발생시 콜백함수들을 넘겨받아 성공과 실패여부를 처리하는 함수
function sign_db(err,success,failed){
	
	if(err){
		
		//회원가입 실패시
		failed();
	}
	
	else{
		//회원가입 성공시
		success();
	}
	
};

//callback template
function callback(flag,success, failed){
	
		console.log(flag());
		
		if(flag() == "true"){
			
			success();
		
		}
		
		else if(flag() == "failed"){
			
			failed();
		}
};

