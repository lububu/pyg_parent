app.controller('brandController', function ($scope,$controller, brandService) {
    //继承      被继承的controller   被继承的   本js的
    $controller('baseController',{$scope:$scope})
    //查询所有品牌信息，并没有实现分页的效果
    $scope.findAll = function () {
        // http://localhost:9101/admin/brand.html
        // http://localhost:9101/brand/findAll.do
        brandService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        )
    }

    //分页查询
    $scope.findPage = function (page, rows) {
        //这里必须要返回到webapp目录就行了，相当于就找到了整个目录
        brandService.findPage(page,rows).success(
            function (response) {
                $scope.list = response.rows;
                //双向绑定
                $scope.paginationConf.totalItems = response.total;
            }
        )
    }


    //设置一个对象的数组   对象是大括号
    $scope.entity = {};
    //添加品牌
    $scope.add = function () {
        brandService.add($scope.entity).success(
            function (response) {
                // 添加成功   刷新
                if (response.success) {
                    $scope.reloadList()
                } else {
                    //添加失败 弹出一个警告框
                    alert(response.message)
                }
            }
        )
    }
    //回显示的方法
    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        )
    }
    //修改商品的方法
    $scope.save = function () {
        var methodName =null;//方法名称
        if ($scope.entity.id != null) {//如果有ID
            methodName =brandService.update($scope.entity);//则执行修改方法
        }else {
            methodName =brandService.add($scope.entity);
        }
        methodName.success(
            function (response) {
                if (response.success) {
                    $scope.reloadList()
                } else {
                    //添加失败 弹出一个警告框
                    alert(response.message)
                }
            }
        )
    }


    //批量删除
    $scope.dele = function () {
        brandService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    //删除成功 那么就初始化这个数组
                    $scope.selectIds = [];
                    $scope.reloadList();
                } else {
                    //添加失败 弹出一个警告框
                    alert(response.message)
                }
            }
        )
    }
    //定义一个search需要的对象
    $scope.searchEntity={};
    //查询操作
    $scope.search = function (page, rows) {
        //这里必须要返回到webapp目录就行了，相当于就找到了整个目录
        brandService.search(page,rows,$scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                //双向绑定
                $scope.paginationConf.totalItems = response.total;
            }
        )
    }


})