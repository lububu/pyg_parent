app.service('cartService',function ($http) {
        this.findCartList = function () {
            return $http.get("/cart/findCartList.do")
        }

    this.addGoodsToCartList = function (itemId,num) {
        return $http.get("/cart/addGoodsToCartList.do?itemId="+itemId + "&num=" +num);
    }

    this.login=function () {
        return $http.get("/cart/login.do")
    }

    // 查询地址
    this.findAddressByUser=function () {
        return $http.get("/address/findAddressByUser.do")
    }

    this.submitOrder=function (order) {
        return $http.post("/order/add.do",order);
    }

})
