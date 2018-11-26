app.controller('indexController',function ($scope, indexService) {
    $scope.showName=function () {
        indexService.login().success(
            function (response) {
                $scope.name=response.name;
            }
        )
    }
})