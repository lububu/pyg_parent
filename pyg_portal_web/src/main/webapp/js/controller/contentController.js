 //控制层 
app.controller('contentController' ,function($scope,contentService){
    $scope.categoryList=[];
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
			function (response) {
				$scope.categoryList[categoryId]=response;
            }
		)
	}


	//跳转到搜索页面
	$scope.search=function () {
		location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
});
