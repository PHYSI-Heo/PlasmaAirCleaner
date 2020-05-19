const express = require('express');
const router = express.Router();

const MAX_UPLOAD_SIZE = 10;

const path = require("path");
const multer = require('multer');
const fs = require("fs");
const fse = require('fs-extra');

const ffmpeg = require('fluent-ffmpeg');

const mainDir = path.dirname(require.main.filename);
var rootPath = __dirname.substring(0, __dirname.lastIndexOf('\\'));

// const Thumbnail = require('thumbnail');
// thumbnail 
// gm : error. ( excute graphicsmagick )
// Thumbnail : not supported video.

var videoTypes = ['.mp4', '.avi', '.wmv', '.mov', '.flv',];


let storage = multer.diskStorage({	
  destination: function(req, file, callback) {
    callback(null, "public/res/" + req.body.did + '/')
  },
  filename: function(req, file, callback) {
    let extension = path.extname(file.originalname); // 확장자
    let basename = path.basename(file.originalname, extension); //  파일이름
    callback(null, file.originalname)
  }
});

let upload = multer({
	storage: storage
});

router.post('/upload', upload.array('fileName', MAX_UPLOAD_SIZE), (req, res) => {  
  for(var i = 0; i < req.files.length; i++){
    console.log("# Upload File Info.");
    console.log(req.files[i]);  

    var originalName = req.files[i].filename.toString();
    var typeIndex = originalName.lastIndexOf('.');
    var fType = originalName.substring(typeIndex);
    var fName = originalName.substring(0, typeIndex);
    console.log("# File Type : " + fType + " , priginal Name : " + fName);

    var originalPath = mainDir + '\\' + req.files[i].path;
    var thumbPath = mainDir + '\\public\\thumb\\' + req.body.did;

    if(videoTypes.includes(fType)){
      // https://github.com/fluent-ffmpeg/node-fluent-ffmpeg
      var proc = new ffmpeg(originalPath)
        .takeScreenshots({        
          count: 1,
          timemarks: [ '5' ], // number of seconds
          filename : fName + '.png',
        }, thumbPath, function(err) {
          console.log('! Screenshots were saved')
      });
    }else{
      thumbPath = thumbPath + '\\' + req.files[i].filename;
      fse.copy(originalPath, thumbPath, err => {
        if (err) 
          console.error(err);
        else
          console.log('> Save Thumbnail Success!')
      })
    }
    
  }
	
	res.status(204).end();
  // =  No Content : 요청한 작업을 수행하였고 데이터를 반환할 필요가 없다는것을 의미
});


router.get('/download/:type/:fileName/:did', (req, res) => {
  if(req.params.type == 'default'){
    console.log("# Default Download file : " + req.params.fileName);
    if (req.params.fileName) {    
      var filePath = rootPath + "/public/defaultIMGs/" + req.params.fileName;
      res.download(filePath);
    } else {
      res.status(204).end();
    }
  } else if(req.params.type == 'user'){
    console.log("# User Download file : " + req.params.did + " > " + req.params.fileName);
    if (req.params.did && req.params.fileName) {
      var filePath = rootPath + "/public/res/" + req.params.did + '/' + req.params.fileName;
      res.download(filePath);
    } else {
      res.status(204).end();
    }
  }else{
     res.status(204).end();
  } 
});


router.post('/default/imgs', (req, res) => {
  fs.readdir(path.join(rootPath, 'public/defaultIMGs/'), (err, files) => {    
    if (err) {
      console.log(err);
    } 
    var fileList = [];    
    files.forEach(function (file) {
      fileList.push({'fileName' : file});
    });

    res.json({
      'resCode' : 1001,
      'files' : fileList
    });
  });
});


module.exports = router;
