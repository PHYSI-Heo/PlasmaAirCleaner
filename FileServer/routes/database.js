const express = require('express');
const router = express.Router();
const db = require('../utils/dbSQL');
const util = require('../utils/utils');

router.post('/register', (req, res)=>{
	db.insertSetInfo(req.body.did, null, null, null, null, null, null, (result)=>{	
		if(result.create){
			util.createFolder("./public/thumb/" + req.body.did);
			util.createFolder("./public/res/" + req.body.did);		
		}		
		res.json(result);
	});
});

router.post('/get/info', (req, res)=>{
	db.selectSetInfo(req.body.did, (result)=>{
		res.json(result);
	});
});

router.post('/update/info', (req, res)=>{
	db.deleteSetInfo(req.body.did, (ignore)=>{
		db.insertSetInfo(req.body.did, req.body.city, req.body.province, 
			req.body.lat, req.body.lon,	req.body.displayTime, 
			req.body.cctvEnable, (result)=>{				
			res.json(result);
		});		
	});
});

router.post('/get/imgs', (req, res)=>{
	db.selectSetImg(req.body.did, (result)=>{
		res.json(result);
	});
});

router.post('/update/imgs', (req, res)=>{
	db.deleteSetImg(req.body.did, (ignore)=>{
		db.insertSetImg(req.body, (result)=>{
			res.json(result);
		});
	});
});

module.exports = router;

