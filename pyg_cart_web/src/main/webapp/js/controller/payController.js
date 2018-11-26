app.controller('payController', function ($scope,$location, $controller, payService) {

    $controller('cartController', {$scope: $scope})
    // 创建二维码
    $scope.createNative = function () {
        payService.createNative().success(
            function (response) {
                $scope.out_trade_no = response.out_trade_no;
                $scope.total_fee = (response.total_fee / 100).toFixed(2);
                var qr = new QRious({
                    element: document.getElementById('qrious'),
                    size: 250,
                    level: 'H',
                    value: response.code_url
                });
                // 当返回结果之后向后端发送请求测试看是否完成支付,需要订单号
                queryPayStatus();
            }
        )
    }
    // 查询订单支付状态
    queryPayStatus = function () {
        payService.queryPayStatus($scope.out_trade_no).success(
            function (response) {
                if (response.success) {
                    // 成功跳转到支付成功界面
                    location.href = "paysuccess.html#?total_fee="+$scope.total_fee;
                }else {
                    // 支付失败
                    if (response.message == "outTime") {
                        alert("支付超时，请从新操作！")
                        $scope.createNative();// 从新生成二维码
                    }else {
                        location.href = "payfail.html"; // 跳转到失败页面
                    }
                }
            }
        )
    }

    // 成功页面获取总金额
    $scope.getMoney = function () {
        return $location.search()['total_fee'];
    }
})