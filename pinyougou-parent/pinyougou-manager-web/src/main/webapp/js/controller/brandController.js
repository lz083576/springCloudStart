app.controller('brandController',function ($scope,$controller,brandService) {//使用了继承以及service必须注入进来才能使用
    $controller('baseController',{$scope:$scope});

    $scope.findAll=function () {
        brandService.findAll().success(function (reponse) {
            $scope.list=reponse;
        });
    };

    $scope.searchEntity={};
    //条件查询
    $scope.search=function (page,size) {
        brandService.search(page,size,$scope.searchEntity).success(function (response) {
            $scope.list=response.rows;
            $scope.paginationConf.totalItems=response.total;//更新总记录数
        });
    };

    /*$scope.findPage=function (page,size) {
        brandService.findPage(page,size).success(function (reponse) {
            $scope.list=reponse.rows;
            $scope.paginationConf.totalItems=reponse.total;//更新总记录数
        });
    };*/





    //保存
    $scope.save=function () {
        var object=null;
        if ($scope.entity.id!=null){
            object=brandService.update($scope.entity);
        }else {
            object=brandService.add($scope.entity);
        }

        object.success(function (response) {
            if (response.success){
                $scope.reloadList();
            }else {
                alert(response.message);
            }
        })
    };
    //根据id查询一个
    $scope.findOne=function (id) {
        brandService.findOne(id).success(function (response) {
            $scope.entity=response;
        })
    };


    $scope.del=function () {
        brandService.del($scope.selectIds).success(function (response) {
            if (response.success){
                $scope.reloadList();
            }else {
                alert(response.message);
            }
        })
    };

});