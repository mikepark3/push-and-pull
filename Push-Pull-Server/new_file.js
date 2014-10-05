/**
 * New node file
 */
var http = require('http');
var express = require('express');
var path = require('path');
var formidable = require('formidable');
var fs = require('fs');
var app = express();
var server = http.createServer(app).listen(3000,function(){
	
	console.log("http server is running on 3000");
});

//module.exports = function(app) {

    // upaload homework video for hmcam
    app.post('/upload', function(req, res) {

        var form = new formidable.IncomingForm();

        var files = [];
        var fields = [];
        form.keepExtensions = true;
        form.uploadDir = __dirname + '/../temp';
        form.maxFieldsSize = 10 * 1024*1024;
        form
            .on('fileBegin',function(name,file){
                console.log('fileBegin-' + name + ':' + JSON.stringify(file));
            })
            .on('progress',function(bytesReceived,bytesExpected){
                console.log('progress-' + bytesReceived +'/' + bytesExpected);
            })
            .on('aborted', function(){
                console.log('aborted');
            })
            .on('error', function(){
                console.log('error');
            })
            .on('end', function(){
                console.log('end');
            });
        form.parse(req,function(err,fields,files){
            console.log('fields: ' + JSON.stringify(fields));
            console.log('files: ' + JSON.stringify(files));


            var uploadFile = files.uploadFile;

            if (uploadFile) {

                // 蹂�닔瑜��좎뼵�⑸땲��
                //var path = __dirname +  "\\files\\" + uploadFile.name;
                var name = uploadFile.name;
                var type = uploadFile.type;
                var filePath = uploadFile.path;

                // 鍮꾨뵒���뚯씪 �뺤씤
                if (path.extname(filePath) == '.mp4')
                {
                    // 鍮꾨뵒���뚯씪��寃쎌슦: �뚯씪 �대쫫��蹂�꼍
                    var outputPath = __dirname + '/../public/videos/' + Date.now() + '_' + name;
                    fs.rename(filePath, outputPath, function (error) {
                        var videopath = path.basename(outputPath);
                        // move done, and store information to db
                        res.send("upload ok");
                    });
                } else {
                    // 鍮꾨뵒���뚯씪���꾨땶 寃쎌슦: �뚯씪���쒓굅�⑸땲��
                    fs.unlink(filePath, function (error) {
                        res.send(400);
                    });
                }
            } else {
                //�뚯씪���놁쓣 寃쎌슦
                res.send(404);
            }
        });

    });

//}