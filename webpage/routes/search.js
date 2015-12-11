var express = require('express');
var router = express.Router();

router.get("/", function (req,res,next) {
    var userQuery = req.query.userQuery;
    console.log("userQuery: "+userQuery);
    var obj ={};
    obj.userQuery = userQuery;
    var results = [{link:"www.youtube.com",text:"this is youtube"},{link:"www.facebook.com",text:"this is facebook"}];
    obj.results = results;
    obj.pageNumber = [1,2,3,4,5];//set the numbers of page
    obj.pageNumber[0]=-1;
    res.render('results', obj);
});


router.get("/:userQuery/:page", function (req,res,next) {
    var userQuery = req.params.userQuery;
    var page = req.params.page;
    console.log("userQuery: "+userQuery);
    var obj ={};
    obj.userQuery = userQuery;
    var results = [{link:"www.youtube.com",text:"this is youtube"},{link:"www.facebook.com",text:"this is facebook"}];
    obj.results = results;
    obj.pageNumber = [1,2,3,4,5];//set the numbers of page

    obj.pageNumber[page-1]=-1*obj.pageNumber[page-1];
    res.render('results', obj);
});

module.exports = router;