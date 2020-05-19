const express = require('express');
const router = express.Router();
const db = require('../utils/dbSQL');

router.post('/create', (req, res)=>{
	createIdentity((result) => {
		res.json(result);
	});
});

router.post('/exist', (req, res)=>{
	db.existDeviceID(req.body.did, (result)=>{
		res.json(result);
	});
});

module.exports = router;



function createIdentity(callback) {
	setRandomValue((value)=>{
		db.existDeviceID(value, (res)=>{
			if(res.exist){
				createIdentity((result)=>{
					callback(result);
				});
			}else{
				console.log("# Create ID : " + value);
				res.did = value;
				callback(res);
			}
		});
	});
}

async function setRandomValue(callback) {
  var ALPHA = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2',
    '3', '4', '5', '6', '7', '8', '9'
  ];
  var value = Array.from("######");		// 6 자리
  const promises = value.map((char, index) => {
    value[index] = ALPHA[Math.floor(Math.random() * ALPHA.length)];
  });
  await Promise.all(promises);
  value = value.join('');
  callback(value);
}
