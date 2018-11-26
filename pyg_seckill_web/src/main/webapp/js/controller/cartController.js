//购物车控制层 
app.controller('cartController',function($scope,cartService){
    // 根据用户的userid查询地址列表
    $scope.findAddressByUser = function(){
        cartService.findAddressByUser().success(
            function (response) {
                $scope.addressList = response;
                for(var i=0;i< $scope.addressList.length;i++){
                    if($scope.addressList[i].isDefault=='1'){
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }
            }
        )
    }
//选择地址
    $scope.selectAddress=function(address){
        $scope.address=address;
    }

//判断是否是当前选中的地址
    $scope.isSelectedAddress=function(address){
        if(address==$scope.address){
            return true;
        }else{
            return false;
        }
    }

    // 支付方式的选择
    $scope.order = {payment_type:'1'};
    $scope.changePagment = function(statsu){
    $scope.order.payment_type = statsu;
    }
    // 提交订单
    $scope.submitOrder = function(){
        // 需要设置订单中的一些属性
        $scope.order.userId = $scope.address.userId;
        $scope.order.receiver = $scope.address.contact;
        $scope.order.receiverArea_Name = $scope.address.address;
        $scope.order.receiverMobile = $scope.address.mobile;
        cartService.submitOrder($scope.order).success(
            function (response) {
            if (response.success){
                location.href = "pay.html"
            }
            alert(response.message)
            }
        )
    }

    $scope.showName=function () {
        cartService.login().success(
            function (response) {
                $scope.loginName=response.loginName;
            }
        )
    }
    //查询购物车列表
    $scope.findCartList=function(){
        cartService.findCartList().success(
            function(response){
                $scope.cartList=response;
                searchAllMoneyAndcount();
            }
        );
    }

    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {
                if (response.success) {
                    // 成功就刷新列表
                 $scope.findCartList();
                }else {
                    alert(response.message)
                }
            }
        )
    }

    // 计算合计数和合计金额
    searchAllMoneyAndcount = function () {
        $scope.allCount = 0;
        $scope.allMoney = 0.00;
        // 需要循环所有的商家
        for (var i = 0; i < $scope.cartList.length; i++) {
            for (var j = 0; j < $scope.cartList[i].orderItemList.length; j++) {
                $scope.allCount += $scope.cartList[i].orderItemList[j].num;
                $scope.allMoney += $scope.cartList[i].orderItemList[j].num * $scope.cartList[i].orderItemList[j].price
            }
        }
    }
});