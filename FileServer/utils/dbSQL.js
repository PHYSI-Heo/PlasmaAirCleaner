const mysql = require('./mysql');

module.exports.existDeviceID = (id, callback) => {
	var sql = "SELECT * FROM setInfos WHERE did = ?";
	mysql.query(sql, [id], (res) => {   
		res.exist = res.resCode == 1001 && res.rows.length != 0;
		callback(res);
	});
}

module.exports.selectSetInfo = (id, callback) => {
	var sql = "SELECT * FROM setInfos WHERE did = ?";
	mysql.query(sql, [id], (res) => {   
		callback(res);
	});
}

module.exports.deleteSetInfo = (id, callback) => {
	var sql = "DELETE FROM setInfos WHERE did = ?";
	mysql.query(sql, [id], (res) => {   
		callback(res);
	});
}

module.exports.insertSetInfo = (id, city, province, lat, lon, display, cctv, callback) => {
	var sql;
	var params;
	if(display){
		sql = "INSERT INTO setInfos VALUES (?, ?, ?, ?, ?, ?, ?)";
		params = [id, city, province, lat, lon, display, cctv];
	}else{
		sql = "INSERT INTO setInfos(did) VALUES (?)";
		params = [id];
	}
	mysql.query(sql, params, (res) => {   
		res.create = res.resCode == 1001;
		callback(res);
	});
}


module.exports.selectSetImg = (id, callback) => {
	var sql = "SELECT * FROM setImgs WHERE did = ? ORDER BY _order ASC";
	mysql.query(sql, [id], (res) => {   
		callback(res);
	});
}

module.exports.deleteSetImg = (id, callback) => {
	var sql = "DELETE FROM setImgs WHERE did = ?";
	mysql.query(sql, [id], (res) => {   
		callback(res);
	});
}

module.exports.insertSetImg = (body, callback) => {
	var itemSize = body.images.length;
	var sql;
	if(itemSize != 0){		
	    sql = "INSERT INTO setImgs VALUES ";
	    for (const img of body.images) {
	      sql += "('" + body.did + "','" + img._order + "','" + img.filePath + "','"  + img.fileName + "'),";
	    }
	    sql = sql.substr(0, sql.length - 1);
	}  	  
	// console.log(sql);
	mysql.query(sql, [], (res) => {   
		callback(res);
	});
}
