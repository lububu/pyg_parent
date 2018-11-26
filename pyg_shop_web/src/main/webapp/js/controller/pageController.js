app.controller('pageController',function($scope){
    //点击使商品数量增加或者减少

    //加入购物车
    $scope.addToCart=function(){
        alert("已将id为"+$scope.sku+"一共有"+$scope.num+"商品")
    }

    //默认选择sku 是第一个
    $scope.loadSku=function(){
        $scope.sku=skuList[0];
        //需要进行深克隆
        $scope.specification=JSON.parse(JSON.stringify($scope.sku.spec))
    }

    //首先需要定义商品最少的数量为1
    $scope.num=1
    $scope.addNum=function(x){
        $scope.num=$scope.num+x;
        if($scope.num<1){
            $scope.num=1;
        }
    }

    //根据点击选项向对象中添加值
    $scope.specification={};
    $scope.selectOption=function (key, value) {
        $scope.specification[key]=value
        searchSku();
    }


    matchObject=function(map1,map2){
        for(var k in map1){
            if(map1[k]!=map2[k]){
                return false;
            }
        }
        for(var k in map2){
            if(map2[k]!=map1[k]){
                return false;
            }
        }
        return true;
    }
    //查询sku即根据点击sku显示对应的title
    searchSku=function(){
    //循环集合 进行比较
        for (var i = 0; i < skuList.length; i++) {
            //怎么比较两个json对象
            if (matchObject(skuList[i].spec,$scope.specification)){
                $scope.sku=skuList[i];
                return;
            }else {
                $scope.sku={id:0,title:'--------',price:0};//如果没有匹配的
            }
        }
    }



    //判断是是否被选中显示
    $scope.isSelected=function (key,value) {

        if ($scope.specification.key==value){
            alert(value)
            return true;
        }
        return false;
    }
})