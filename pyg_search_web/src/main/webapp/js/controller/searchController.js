app.controller('searchController',function($scope,searchService,$location){
    //搜索
    //这里是向搜索对象中添加搜索的条件
    $scope.searchMap={keywords:'',brand:'',category:'',spec:{},
        price:'',pageNo:1,pageSize:10,sortFiled:'',sort:''};//封装查询条件的map

    //获取到首页的数据并查询
    $scope.loadkeywords=function(){
        $scope.searchMap.keywords=$location.search()["keywords"];
        if ($scope.searchMap.keywords == null){
            return;
        }
        $scope.search();
    }


    //排序
    $scope.sortByFiled=function(fileName,sort){
        $scope.searchMap.sortFiled=fileName;
        $scope.searchMap.sort=sort;
        $scope.search();
    }


    //构建分页
//构建分页标签(totalPages为总页数)
    buildPageLabel=function(){
        $scope.pageLabel=[];//新增分页栏属性
        var maxPageNo= $scope.resultMap.totalPages;//得到最后页码
        var firstPage=1;//开始页码
        var lastPage=maxPageNo;//截止页码
        if($scope.resultMap.totalPages> 5){//如果总页数大于5页,显示部分页码
            if($scope.searchMap.pageNo<=3){//如果当前页小于等于3
                lastPage=5; //前5页
            }else if( $scope.searchMap.pageNo>=lastPage-2  ){//如果当前页大于等于最大页码-2
                firstPage= maxPageNo-4;
            }else{ //显示当前页为中心的5页
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;
            }
        }
        //循环产生页码标签
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }

    //点击选项设置但前的页数
    $scope.updatePageNo=function(value){
        if (value<1 || value>$scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo=value;
        $scope.search();
    }

    //隐藏品牌分类如果品牌列表红包含查询的关键字
    $scope.keywordsIsBrand=function(){
        //循环brandList
        var brandList = $scope.resultMap.brandList;
        for (var i = 0; i < brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf(brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }





    //添加搜索选项  name传递的是什么样的条件，value选择的是哪个选项
    $scope.addSearchItem=function (name,value) {
        if(name=='brand'||name=='category' || name=='price'){//添加品牌和分类条件
            $scope.searchMap[name]=value;
        }else{//选择的就是规格
            $scope.searchMap.spec[name]=value;
        }
        $scope.search();
    }
    //移除面包屑
    $scope.removeSearchItem=function(name){
        if(name=='brand'||name=='category' ||name=='price'){//添加品牌和分类条件
            $scope.searchMap[name]="";
        }else{//选择的就是规格
            delete $scope.searchMap.spec[name];
        }
        $scope.search();
    }
    $scope.search=function(){
        $scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
        $scope.searchMap.pageSize=parseInt($scope.searchMap.pageSize);
        searchService.search( $scope.searchMap ).success(
            function(response){
                $scope.resultMap=response;//搜索返回的结果
                buildPageLabel();//调用
            }
        );
    }
});