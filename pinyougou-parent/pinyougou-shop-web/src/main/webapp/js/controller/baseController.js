app.controller('baseController',function ($scope) {
    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,//当前页
        totalItems: 10,//总记录数
        itemsPerPage: 10,//每页显示多少
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){//页码发生改变触发onChang
            $scope.reloadList();//重新加载
        }
    };
    //重新加载列表 数据
    $scope.reloadList=function(){
        //切换页码
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };

    //判断是否存在id，没有添加，有则删除
    $scope.selectIds=[];
    $scope.updateSelection=function ($event,id) {
        if ($event.target.checked){
            $scope.selectIds.push(id);
        }else {
            var index=$scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);
        }
    };

    $scope.jsonToString=function (jsonString,key) {
        var jsonObject=JSON.parse(jsonString);
        var value="";
        for (var i=0;i<jsonObject.length;i++){
            if (i>0){
                value+=",";
            }
            value+=jsonObject[i][key];
        }
        return value;
    }
    //在list集合中查询某key的对象
    $scope.searchObjectByKey=function (list,key,keyValue) {
        for (var i=0;i<list.length;i++){
           if( list[i][key]==keyValue){
               return list[i];
           }
        }
        return null;
    }

});

