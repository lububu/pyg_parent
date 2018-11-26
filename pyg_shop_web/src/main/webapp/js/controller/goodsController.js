 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,typeTemplateService,itemCatService,uploadService,goodsService){
	
	$controller('baseController',{$scope:$scope});//继承
    //读取列表数据绑定到表单中

	//根据商品分别类查询返回其对应的分类名字
	$scope.categoryList=[];
	$scope.findCategoryList=function(){
		itemCatService.findAll().success(
			function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.categoryList[response[i].id]=response[i].name;
                }
            }
		)
	}


	$scope.status=['未审核','审核通过','审核未通过'];
	$scope.categoryList1=function(){
		itemCatService.findByParentId(0).success(
			function (response) {
			$scope.categoryList1=response
            }
		)
	}
	//这是监听下拉列表条件查询
    $scope.$watch('searchEntity.auditStatus',function (newValue, oldValue) {
        $scope.reloadList();//刷新列表
    })
	//这是监听动态下拉列表
 	$scope.$watch('entity.goods.category1Id',function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.categoryList2=response
            }
        )
    })

    $scope.$watch('entity.goods.category2Id',function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.categoryList3=response
        }
        )
    })

    $scope.$watch('entity.goods.category3Id',function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId=response.typeId;
            }
        )
    })

    $scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
            	$scope.typeTemplate=JSON.parse(response.brandIds);
            }
        )
        //根据模板的变化获取到模板的规格选项
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
			$scope.specList=response;
            }
        )
    })

	$scope.upload=function(){
		uploadService.upload().success(
			function (response) {
                if(response.success){
                    $scope.image_entity.url=response.message;
                    document.getElementById("file").value=""
                }else{
                    alert(response.message);
                }
            }
		)
	}
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
        $scope.entity.goods.id = $location.search()["id"];
		goodsService.findOne($scope.entity.goods.id).success(
			function(response){
				$scope.entity= response;
                editor.html($scope.entity.goodsDesc.introduction);
                $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages)
                $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems)
                for (var i = 0; i <$scope.entity.items.length; i++) {
                    $scope.entity.items[i].spec=JSON.parse($scope.entity.items[i].spec);
                }
			}
		);				
	}
	//回显规格是否被选中
	$scope.isChecked=function(name,value){
        var specItems=$scope.entity.goodsDesc.specificationItems;
        //去查询是否有这个名称的规格存在
        var object=$scope.selectObject(specItems,name,"attributeName")
				if (object==null){
					return false;
				} else {
                    for (var i = 0; i <object.attributeValue.length ; i++) {
						if (object.attributeValue[i]==value){
							return true;
						} else {
							if (i==object.attributeValue.length-1) {
                                return false;
                            }else {
								continue;
							}
						}
                    }
				}
            }

	//显示列表
	$scope.createItemList=function(){
		//这个相当于获得规格得的一个数组
        var specItems=$scope.entity.goodsDesc.specificationItems;
        //自定义一个最普通的
		$scope.entity.items=[{spec:{},price:0,num:9999,status:"1",isDefault:"0"}];
		//循环规格的个数
        for (var i = 0; i <specItems.length; i++) {
            $scope.entity.items=addColumn($scope.entity.items,specItems[i].attributeName,specItems[i].attributeValue);
        }
	}


	addColumn=function(lists,name,value){
		var newList=[];
		//这里相当于循环已有的对象的个数
        for (var i = 0; i <lists.length ; i++) {
        	var oldRow=lists[i];
        	//这里相当于有多个value值得话就在循环出更多个普通的对象
            for (var j = 0; j <value.length; j++) {
				var newRow=JSON.parse(JSON.stringify(oldRow));
				newRow.spec[name]=value[j];
				newList.push(newRow);
            }
        }
        return newList;
	}




	//这里是判断是往里面家元素还是创建对象
	$scope.updateSpecAttribute=function($event,name,value){
		var specItems=$scope.entity.goodsDesc.specificationItems
        var object=$scope.selectObject(specItems,name,"attributeName")
	if ($scope.selectObject(specItems,name,"attributeName")==null){
		//即没有对象，向里面添加对象
        // [{"attributeName":"网络","attributeValue":["移动3G","移动4G"]}]
        specItems.push({"attributeName":name,"attributeValue":[value]})
	} else {
		//有对象，向里面添加元素
		if ($event.target.checked){
            object.attributeValue.push(value);
		} else {
            object.attributeValue.splice(object.attributeValue.indexOf(value),1)
			if (object.attributeValue.length==0){
				//如果对象元素为0，则移除整个对象
                specItems.splice(specItems.indexOf(object),1);
			}
		}
	}
	}

	//判断对象是否存在
	$scope.selectObject=function(list,name,key){
        for (var i = 0; i <list.length ; i++) {
			if (list[i][key]==name){
				//说明有对象存在
				return list[i];
			}//没有对象存在
        }
        return null;
	}
	// 这里添加的是个组合对象需要进行封装
	$scope.entity={goods:{isEnableSpec:'1'},goodsDesc:{itemImages:[],specificationItems:[]},items:[]}

    $scope.image_entity={};
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

    $scope.dele_image_entity=function(index){
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }
	//保存 
	$scope.save=function(){
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加

		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询
					location.href="goods.html";
                    // $scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]},items:[]}
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
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
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    
});	
