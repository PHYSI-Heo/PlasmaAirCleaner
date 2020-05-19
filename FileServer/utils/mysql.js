const mysql = require('mysql');

const MYSQLIP = 'localhost';
const MYSQLID = 'root';
const MYSQLPWD = '1234';
const DBNAME = 'PAC_DB';

const dbInfo = {
  host : MYSQLIP,
  port : 3306,
  user : MYSQLID,
  password : MYSQLPWD,
  database : DBNAME,
  connectionLimit:100,
  waitForConnections:true
};

var dbPool;

module.exports.createPool = () => {
  dbPool = mysql.createPool(dbInfo);
  console.log("# Create MySQL ThreadPool..");
  if(dbPool){
    initTable();
  }
}

function query(sql, values, callback){
  var result = {};
  dbPool.getConnection(function (con_Err, con) {
    if(con_Err){
      // DB Connect Err
      result.resCode = 1002;
      console.log('\x1b[35m%s\x1b[0m', "## DB Connect Err : " + con_Err.message);
      callback(result);
    }else{
      con.query(sql, values, function (query_Err, rows) {
        if(query_Err){
          // Query Error
          result.resCode = 1003;
          console.log('\x1b[35m%s\x1b[0m', "## DB Query Err : " + query_Err.message);
        }else{
          // Query Result
          result.resCode = 1001;
          result.rows = rows;
        }
        con.release();
        callback(result);
      });
    }
  });
}
module.exports.query = query;


const SQL_CT_DEVICE_INFO = "CREATE TABLE setInfos ( " +
  "did varchar(20) not null primary key, " +
  "city varchar(20), " +
  "province varchar(20), " +
  "lat varchar(20), " +
  "lon varchar(20), " +
  "displayTime varchar(10) not null default '5', " +
  "cctvEnable char(1) not null default '1');";

const SQL_CT_IMAGE_SET = "CREATE TABLE setImgs ( " +
  "did varchar(20) not null, " +
  "_order int not null, " +
  "filePath text not null, " +
  "fileName varchar(50) not null);";  

function initTable() {
    query(SQL_CT_DEVICE_INFO, [], (res)=>{
      console.log("# Create Device Info Table : " + res.resCode);
    });

    query(SQL_CT_IMAGE_SET, [], (res)=>{
      console.log("# Create Image Setup Table : " + res.resCode);
    });
};  