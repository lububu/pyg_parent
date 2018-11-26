 //控制层 
app.controller('seckillGoodsController' ,function($scope,$interval,$location,seckillGoodsService){

	// 判断页面是否登陆进行跳转
	$scope.jumpPage = function(name,id){
		if (name == 'anonymousUser') {
			alert("请先登陆好吗？少BB，少挨打！")
			// location.href = "login.html";
		}else {
			location.href = "http://localhost:9109/seckill-item.html#?id=" + id;
		}
	}
	// 查询某个商品的详细信息
	$scope.findOneFromRedis = function() {
		var id = $location.search()['id'];
		seckillGoodsService.findOneFromRedis(id).success(
			function (response) {
				$scope.secKillGood = response;
                var seconde = Math.floor((new Date($scope.secKillGood.endTime).getTime() - new Date().getTime()) / 1000);

                var time = $interval(function () {

                    if(seconde > 0){
                        seconde = seconde - 1;
                        $scope.timeTitle = convertTimeString(seconde);
                        // alert($scope.timeTitle)
                    }else{
                        $interval.cancel(time);
                        alert("秒杀结束");
                    }
                },1000);
            }
        );
    }


    //转换秒为   天小时分钟秒格式  XXX天 10:22:33
    convertTimeString=function(allsecond){
        var days= Math.floor( allsecond/(60*60*24));//天数
        var hours= Math.floor( (allsecond-days*60*60*24)/(60*60) );//小时数
        var minutes= Math.floor(  (allsecond -days*60*60*24 - hours*60*60)/60    );//分钟数
        var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
        var timeString="";
        if(days>0){
            timeString=days+"天 ";
        }
        return timeString+hours+":"+minutes+":"+seconds;
    }
    // $controller('cartController', {$scope: $scope})
    $scope.showName=function () {
        seckillGoodsService.login().success(
            function (response) {
                $scope.loginName=response.loginName;
            }
        )
    }
	// 查询秒杀商品
	$scope.findList = function() {
		seckillGoodsService.findList().success(
			function (response) {
				$scope.secKillList = response;
            }
		)
	}

    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		seckillGoodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		seckillGoodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		seckillGoodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=seckillGoodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=seckillGoodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		seckillGoodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		seckillGoodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    
});	
