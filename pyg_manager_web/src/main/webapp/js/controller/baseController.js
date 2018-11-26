app.controller('baseController',function ($scope) {
    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,
        //这里定义成0就只查询一次，底层实现不需要了解。
        totalItems: 0,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        // onchange  代表只要上面的参数有一个改变，那么就会执行方法
        onChange: function () {
            $scope.reloadList();
        }
    };

    //定义一个刷新的方法
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage)
    }
    $scope.selectIds=[];
    //定义一个删除的字符数组   问题是怎么将元素id添加进来
    //创建一个复选框的点击事件
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            //如果被选中就向里面添加id
            $scope.selectIds.push(id);
        } else {
            var index = $scope.selectIds.indexOf(id);
            $scope.seletIds.splice(index, 1);
        }
    }

    $scope.jsonToString=function(jsonString,key){
        var json=JSON.parse(jsonString);//将json字符串转换为json对象
        var value="";
        for(var i=0;i<json.length;i++){
            if (i > 0) {
                value+=","
            }
            value+=json[i][key]
        }
        return value;
    }

    // //提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
    // $scope.jsonToString=function(jsonString,key){
    //     var json=JSON.parse(jsonString);//将json字符串转换为json对象
    //     var value="";
    //     for(var i=0;i<json.length;i++){
    //         if(i>0){
    //             value+=","
    //         }
    //         value+=json[i][key];
    //     }
    //     return value;
    // }
})