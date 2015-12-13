var express = require('express');
var WebSocket = require('ws');
var router = express.Router();

router.get('/', function(req, res, next) {
  var userQuery = req.query.userQuery;
  var obj = {};
  obj.query = userQuery;
  obj.topic = "nfl"; //TODO change topic with req.query.topic
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
    res.render('results', obj);
  };

});

router.get('/:userQuery/:page', function(req, res, next) {
  var userQuery = req.params.userQuery;
  var page = req.params.page;
  var obj = {};
  obj.query = userQuery;
  obj.topic = "nfl";  //TODO change topic with req.params.topic
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
    res.render('results', obj);
  };
});

module.exports = router;
