const express = require('express');
const bodyParser = require('body-parser');
const path = require('path');

const db = require('./utils/mysql');
const util = require('./utils/utils');

util.createFolder("./public/thumb");
util.createFolder("./public/res");

db.createPool();


var app = express();

app.set('port', process.env.PORT || 3000);

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: false
}));
app.use(require('stylus').middleware(path.join(__dirname, 'public')));
app.use(express.static(path.join(__dirname, 'public')));


var idRouter = require('./routes/identity');
app.use('/identity', idRouter);

var dbRouter = require('./routes/database');
app.use('/db', dbRouter);

var imgRouter = require('./routes/images');
app.use('/image', imgRouter);


app.listen(app.get('port'), function() {
  console.log("# Start Server..");
});


