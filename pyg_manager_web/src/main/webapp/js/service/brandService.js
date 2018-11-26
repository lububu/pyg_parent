//自定义一个服务
app.service("brandService",function ($http) {
    //查询全部的品牌
    this.findAll=function () {
        return $http.get("../brand/findAll.do");
    }
    //分页查询
    this.findPage=function (page, rows) {
        return $http.get("../brand/findPage.do?page=" + page + "&rows=" + rows);
    }
    //分页条件查询
    this.search=function (page,rows,searchEntity) {
        return $http.post("../brand/search.do?page=" + page + "&rows=" + rows,searchEntity);
    }
    //回显查询单个品牌
    this.findOne=function (id) {
        return $http.get("../brand/findOne.do?id=" + id);
    }
    //批量删除商品
    this.dele=function (seletIds) {
        return $http.get("../brand/dele.do?ids=" + seletIds)
    }
    //添加品牌
    this.add=function (entity) {
        return $http.post("../brand/add.do",entity);
    }
    //修改品牌
    this.update=function (entity) {
        return $http.post("../brand/update.do",entity);
    }

    this.selectOptionList=function () {
        return $http.get("../brand/selectOptionList.do");
    }
})