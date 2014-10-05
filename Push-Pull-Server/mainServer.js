
//chatting�� ���� tcp server
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
	//app.set('view engine', 'jade'); jade��� enginež��
	app.set('view engine', 'ejs');
	app.set('port',4000);
	app.use(express.bodyParser({uploadDir: __dirname + '/images'})); //�������� ���ε带 ���� ����
	app.use(express.limit('10mb')); //�뷮 ���� ����
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
			//�̹����� ��ο� �̸��� �����Ѵ�.
			
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
			//�̹����� �ƴϸ� �����̸��� ����
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
	
	req.session.user_id = req.body.chat_id; //���������� ��û���� �� ���̵� ����
	req.session.pw = req.body.password; //���������� ��û���� �� �н����� ����
	
	
	//��û���� �� ���̵�� �н����带  db������ ��Ī
	login_db(req.body.chat_id,req.body.password,function(){ //�������� ���� �ݹ� �Լ�
		
		
		connected_socket.forEach(function(otherSocket){ //����� �Լ��迭�� ��ȸ�ϸ鼭 �����̸��� ��û���� �Ѿ�� ���̵� ������ �����̺�Ʈ ���� 
			
			if(otherSocket.name == req.body.chat_id){
				
				otherSocket.emit('login_success');
				res.redirect('/menu');
			};
	
		});
	}, 
	
	function(){ //�������� ���� �ݹ� �Լ�
		
		connected_socket.forEach(function(otherSocket){
			
		
			if(otherSocket.name == req.body.chat_id){
				
				otherSocket.emit('login_failed'); //db��Ī�� �����ϸ� �����̸��� ��û���� �Ѿ�� ���̵� �����Ͽ� ���� ����Ʈ�� ����
				res.redirect('/');
			}
		
		});
	
	});
});
	


//mysql db ����
var mysql = require('mysql');

//�����ͺ��̽��� ����
var client = mysql.createConnection({
	
	host : 'localhost',
	user: 'root',
	password:'mysql'
});

client.connect();

//socketio ����
var io = require('socket.io').listen(server);
//���ʿ��� �α� ����
io.set('log level', 2);
//�α����� �õ��ϴ� ������ ��� ���� �����迭����
var connected_socket=[];

//�α��� �� ������ ������� �����迭����
var login_socket=[];

//socketio�� ���� socket��� 
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
		
		//������� �ڵ�
		/*var id = data[0]; //id����
		var pw = data[1]; //pw����
		console.log(data);
		console.log(id);
		console.log(pw);
		
		//��ġ�ϴ� id�� db���� Ž���ϴ� method ȣ��
		login_db(id,pw,function(){
			
			//������ ȣ��
			socket.emit('login_success');
			
			
		
		}, 
		
		function(){
			//Fail��  ȣ��
			socket.emit('login_failed');
		
		});
		*/
		
		socket.name = data[0];//data���� id���� �����Ͽ� ���Ͽ� �̸��� �ٿ����´�.
		connected_socket.push(socket); //�α��� �õ��ϴ� ������ �迭�� ��´�.
		
		
		
	});
	
	//sign in Event
	socket.on('sign', function(data){
		
		var id = data[0];
		var pw = data[1];
		
		client.query('INSERT INTO user_list(id,pwl) values(?,?)',[id,pw], function(err,rows,fields){
			
		
			sign_db(err,function(){
				//ȸ������ ������ �ݹ� �Լ�
				socket.emit('sign_success');
			}, function(){
				//ȸ������ ���н� �ݹ��Լ�
				socket.emit('sign_failed');
			});
			
			
		});
	});
	
	//menu���� start game��ư�� ������ ����Ǵ� �̺�Ʈ
	socket.on('ready_game',function(id){
		

		socket.name = id;
		login_socket.push(socket);
		
	
	});
	
	//��û�ڷκ��� ���� ���ٸ޽��� ��û�� �������� �ٸ� ����ڿ��� �����ϴ� �̺�Ʈ
	socket.on('start_game', function(message){
		
		socket.apply = "true"; //��û�� �����ڴ� ���Ͽ� ǥ�ø� �ؼ� �������� ������ �������� �ʰԲ� �Ѵ�.
	

		var target;
		
		login_socket.forEach(function(otherSocket){ //login�� ���ϵ��� ���鼭 �Լ�����
			
			if(otherSocket != socket && otherSocket.apply != "true"){
				
				target = otherSocket;
			}
		});
		
		//callback�Լ��� �����ÿ� ���н��� �Լ��� ���ڷ� �־� �帧��� �Ѵ�.
		callback(function(){
		
		
		
			if(target != undefined && target.apply != "true" && target != socket){
			
				return "true";
			}
		
		
			else{
				
				return "failed";
			}
		
	
		
		}, function(){
			target.apply = "true"; //��û�� ��ġ�� �ʵ��� �÷��� ����
			target.emit('confirm_game',message,socket.name); //���ǿ� ������ ���濡�� ���϶� Ȯ�� �̺�Ʈ �߻�
		},
	
		function(){
		
			socket.apply = "false";//�ٽ� ������ �� �ְԲ� flag�� �ٲ��ش�.
			
			socket.emit('full_apply'); //���н� �����ʰ� �̺�Ʈ �߻�
		});
	});
	
	
	
	//������ ������ �ź����� �� �߻��ϴ� �̺�Ʈ
	socket.on('refuse_game',function(id){
		
		socket.apply = "false"; //reject�����Ƿ� ��û���� �� �ְ� �÷��׸� �ٲ��ش�.
		login_socket.forEach(function(target){
			
			if(target.name == id){
				
				target.apply = "false"; //������ ���κ��� ��û���� id�� �޾Ƽ� �α��� ���� �������� Ž�� �� apply�Ӽ��� false�� �ٲ��ش�.
				target.emit('reject_game');
			}
			
		});
	});
	
	//������ ������ �������� ��
	socket.on('start_chat',function(id){
		
		
		
		var couple = []; //tcp socket�� ¦�� ã�� ���� ���̵� ������ �迭�� ����
		
		couple.push(socket.name);//���̵� Ǫ��
		couple.push(id);//���̵� Ǫ��
		
		matching.push(couple); //���϶� ��Ī ������ ��� �����迭�� Ǫ��
		
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

//��Ī ������ ������ �����迭
var matching = [];

//ä���� ���� tcp ������ ��� �迭
var chat_socket=[];


//tcp server listening socket.io�� default�� tcp socket �����̴�.
var tcp_io = require('socket.io').listen(5000,function(err){
	
	console.log("tcp server is listening on port 5000");
});

//socket loglevel 2�� �����Ͽ� ���ʿ��� �α� ����
tcp_io.set('log level',2);

//tcp socket connection

tcp_io.sockets.on('connection',function(socket){
	
	console.log("tcp connection success!");
	
	
	//socket�� ����� ���̵� ����ϱ� ���� �̺�Ʈ
	socket.on('register_id',function(id){
		
		
		socket.name = id;
		chat_socket.push(socket); //chatting�ϱ����� ����� ������ ��Ƴ��´�.
		
		//var message = id + " is entered!!";
		
		//socket.emit('get_message', message);
		console.log("register success");
	});
	
	//tcp socket�� �����ϴ� �̺�Ʈ
	socket.on('find_manitto',function(id){
		console.log("find manitto");
		var target; //target ������ ������ ����
		
		matching.forEach(function(data){
			
		    var temp1 = data[0]; //�迭���� ���̵� ������ �̾Ƴ���
			var temp2 = data[1];
			
			
			if(temp1 == id){
				
				target = temp2;
			}
			
			else if(temp2 == id){
				
				target = temp1;
			}
		});
		
		
		
		if(target != undefined){
			
			chat_socket.forEach(function(otherSocket){ //log in �� tcp socket�� ��ȸ�ϸ鼭 target�� ���̵� Ž���Ͽ� ä�ð����ϰ� ���´�.
				
				if(otherSocket.name == target){
					
					
					socket.join(target);
					socket.set('room',target);//socket�� ���� �����ϰ� target�� ���̵�� room�� �����Ѵ�.
			
				
					otherSocket.join(target);
					otherSocket.set('room',target); 
					
					
					var intro_message = socket.name + " is entered!! " + "\n" + otherSocket.name + " is entered!! " + "\n";
					//tcp_io.sockets.emit('get_message',intro_message);
					socket.get('room',function(error,room){
				
						tcp_io.sockets.in(room).emit('get_message','',target); //syntax error��� �Ǿ������� io.sockets.in(room)�޼ҵ��̴�. room�� �ش��ϴ� ���Ͽ� �̺�Ʈ�� ������ �ش�.
					});
					
					//tcp_io.sockets.emit('get_message',intro_message);
				}
			});
		}
		
	});
	
	
	
	//message�� ���� ���� �̺�Ʈ
	socket.on('message_on',function(message, room){
		console.log("message on");
		console.log(room);
		tcp_io.sockets.in(room).emit('get_message',message,room);
	});
	
	//reciever�� ������ �ο����� ���� �̺�Ʈ
	socket.on('input_score',function(score,room){
		
		console.log("input score");
		console.log(score);
		console.log(room);
		tcp_io.sockets.in(room).emit('update_score',score,room);
		
	});
	
	//reciever�� ������ ������ �� �̺�Ʈ

	
	//manitto�� ������ �������� ���� Ȯ�� �̺�Ʈ
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
	//socket�� ���� �������� �̺�Ʈ
	
	/*socket.on('disconnect',function(){
		
		var index = chat_socket.indexOf(socket);
		chat_socket.splice(index,1);
	});*/
	
});


//log-in event ȣ��� id,password,�ݹ� �Լ����� �Ѱܹ޾� DB���� id,password�� ���Ͽ� login���θ� ó���ϴ� �Լ�
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


//sing-in event�߻��� �ݹ��Լ����� �Ѱܹ޾� ������ ���п��θ� ó���ϴ� �Լ�
function sign_db(err,success,failed){
	
	if(err){
		
		//ȸ������ ���н�
		failed();
	}
	
	else{
		//ȸ������ ������
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

