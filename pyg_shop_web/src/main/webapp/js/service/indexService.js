app.service('indexService',function ($http) {
    this.login=function () {
        return $http.get("../login.do")
    }
})