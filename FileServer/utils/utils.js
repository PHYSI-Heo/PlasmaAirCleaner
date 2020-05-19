const fs = require('fs');

module.exports.createFolder = (path) => {
	if (!fs.existsSync(path)){
		fs.mkdirSync(path);
	}	
}		