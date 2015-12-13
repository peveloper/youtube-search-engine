var express = require('express');
var WebSocket = require('ws');
var router = express.Router();

router.get('/', function(req, res, next) {
  var userQuery = req.query.userQuery;
  var obj = {};
  obj.query = userQuery;

  if(req.query.topic!= undefined){
    obj.topic = req.query.topic;
  }else{
    obj.topic="NBA NFL"
  }
  if (obj.query==""){
    obj.query=obj.topic
  }
  var results = undefined;
  var ws = new WebSocket('ws://localhost:8887');  // per il mio computer 'ws://test.secure.dev:8887'

  ws.onopen = function() {
    // Web Socket is connected, send data using send()
    ws.send(JSON.stringify(obj));
  };

  ws.onmessage = function(mess) {
    results = JSON.parse(mess.data);

    var numberOfPages = results.length/10;
    obj.pageNumber =[];
    for (var i = 1; i <= numberOfPages; i++) {
      obj.pageNumber.push(i);
    }
    obj.results = results.slice(0,10);
    obj.pageNumber[0] = -1;
    obj.before =[];
    obj.after = [];

    obj.after.push(obj.pageNumber[obj.pageNumber.length-1]);
    obj.pageNumber = obj.pageNumber.slice(0,5);
    res.render('results', obj);
  };

});

router.get('/:topic/:userQuery/:page', function(req, res, next) {
  var userQuery = req.params.userQuery;
  var page = req.params.page;
  var obj = {};
  obj.query = userQuery;
  if(req.params.topic!= undefined){
    obj.topic = req.params.topic;
  }else{
    obj.topic = "NBA NFL";
  }
  var results = undefined;
  var ws = new WebSocket('ws://localhost:8887'); // per il mio computer 'ws://test.secure.dev:8887'

  ws.onopen = function() {
    // Web Socket is connected, send data using send()
    ws.send(JSON.stringify(obj));
  };

  ws.onmessage = function(mess) {
    results = JSON.parse(mess.data);

    var numberOfPages = results.length/10;
    obj.pageNumber =[];
    for (var i = 1; i <= numberOfPages; i++) {
      obj.pageNumber.push(i);
    }
    obj.results = results.slice((page-1)*10,page*10);
    obj.pageNumber[page - 1] = -1 * obj.pageNumber[page - 1];
    obj.before =[1];
    obj.after = [];
    obj.after.push(obj.pageNumber[obj.pageNumber.length-1]);
    var min = page-2;
    if (min<=1){
      min = 1;
    }
    var max = min+5;
    if (max>=obj.pageNumber.length){
      obj.after = [];
      max = obj.pageNumber.length;
      min = max-5;
    }
    obj.pageNumber = obj.pageNumber.slice(min,max);
    res.render('results', obj);
  };
});

module.exports = router;
