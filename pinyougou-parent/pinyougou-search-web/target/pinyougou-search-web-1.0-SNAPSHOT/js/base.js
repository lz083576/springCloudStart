var app = angular.module('pinyougou',[]);
//定义一个过滤器
app.filter('trustHtml',['$sce',function ($sce) {
    return function (data) {//传入参数是被过滤的内容
        return $sce.trustAsHtml(data);//过滤后的内容是信任html的

    }
} ]);