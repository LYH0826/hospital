package yygh.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import yygh.model.cmn.Dict;
import yygh.result.Result;
import yygh.service.DictService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin
public class DictController {

    @Autowired
    private DictService dictService;

    @ApiOperation("导入数据字典")
    @PostMapping("importData")
    //得到上传的文件
    public Result importDict(MultipartFile file){
        dictService.importDictData(file);
        return Result.ok();
    }

    //导出不需要返回数据
    @ApiOperation("导出数据字典接口")
    @GetMapping("exportData")
    public void exportDict(HttpServletResponse response){
        dictService.exportDictData(response);
    }


    //用于ElementUi树形组件使用
    @ApiOperation("根据数据id查询子数据列表")
    @GetMapping("findChildData/{parentID}")
    public Result findChildData(@PathVariable("parentID") Long parentID){
        List<Dict> childDatas = dictService.findChildData(parentID);
        return Result.ok(childDatas);
    }

    @ApiOperation("刷新缓存")
    @GetMapping("flushCache")
    public Result flushCache(){
        dictService.flushCache();
        return Result.ok();
    }

    @ApiOperation("根据dictcode和value查询，获取数据字典名称")
    @GetMapping("/getName/{dictCode}/{value}")
    public String getName(@PathVariable("dictCode") String dictCode,
                          @PathVariable("value") String value){
        return dictService.getDictName(dictCode, value);
    }

    @ApiOperation("根据value查询")
    @GetMapping("/getName/{value}")
    public String getName(@PathVariable("value") String value){
        return dictService.getDictName("", value);
    }

    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findByDictCode/{dictCode}")
    public Result<List<Dict>> findByDictCode(@PathVariable("dictCode") String dictCode){
        List<Dict> list = dictService.findByDictCode(dictCode);
        return Result.ok(list);
    }
}
