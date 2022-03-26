package yygh.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import yygh.listener.DictListener;
import yygh.mapper.DictMapper;
import yygh.model.cmn.Dict;
import yygh.service.DictService;
import yygh.vo.cmn.DictEeVo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl
        extends ServiceImpl<DictMapper, Dict>
        implements DictService {

    @Autowired
    private DictListener dictListener;

    /*
    @Cacheable: 根据方法对其返回结果进行缓存，下次请求时，如果缓存存在，则直接读取缓存数据返回；
    如果缓存不存在，则执行方法，并把返回的结果存入缓存中。
    value为必填属性，它指定了你的缓存存放在哪块命名空间
     */
    @ApiOperation("根据上级id获取子节点数据列表")
    @Override
    @Cacheable(value = "dict",keyGenerator = "keyGenerator")
    public List<Dict> findChildData(Long parentID) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentID);
        //baseMapper在ServiceImpl中已经被Mybatis-plus封装
        List<Dict> dicts = baseMapper.selectList(wrapper);
        //向集合中的每个对象设置hasChlidren的值
        for (Dict dict:dicts){
            //用自己的ID判断自己下面是否有子节点
            boolean flag = this.hasChildrenData(dict.getId());
            //根据结果设置值
            dict.setHasChildren(flag);
        }
        return dicts;
    }

    @Override
    public void exportDictData(HttpServletResponse response) {
        //设置头信息
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = "dict";
        //Content-disposition: 让该操作以下载的方式打开
        response.setHeader("Content-disposition",
                "attachment;filename="+ fileName + ".xlsx");

        //查询数据库
        List<Dict> dicts = baseMapper.selectList(null);
        //将Dict中的数据封装进DictEeVo中
        List<DictEeVo> dictEeVos = new ArrayList<>();
        for(Dict dict : dicts){
            DictEeVo dictEeVo = new DictEeVo();
            //使用spring提供的工具类进行复制
            BeanUtils.copyProperties(dict, dictEeVo);
            dictEeVos.add(dictEeVo);
        }

        //进行写操作
        try {
            EasyExcel.write(response.getOutputStream(), DictEeVo.class)
                    .sheet("DictSheet").doWrite(dictEeVos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //使用该注解标志的方法，会清空指定的缓存。
    @CacheEvict(value = "dict", allEntries=true)
    @Override
    public void importDictData(MultipartFile multipartFile) {
        try {
            //上传文件的数据与数据库冲突就会报错
            EasyExcel.read(multipartFile.getInputStream(),
                    DictEeVo.class, dictListener).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //刷新缓存，同步Redis与mysql中的数据
    @CacheEvict(value = "dict", allEntries=true)
    @Override
    public void flushCache(){}

    //获取数据字典名称
    @Override
    public String getDictName(String dictCode, String value) {
        //判断是否传入dictCode
        if(StringUtils.isEmpty(dictCode)){//为空
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("value", value);
            Dict dict = baseMapper.selectOne(wrapper);
            return dict.getName();
        }else {//不为空
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("dict_code", dictCode);
            Dict dictByDictCode = baseMapper.selectOne(wrapper);
            //获取到的ID便是子类的父ID
            Long parentID = dictByDictCode.getId();
            //根据父ID和value找到相应的数据名称
            Dict dict = baseMapper.selectOne(
                    new QueryWrapper<Dict>()
                            .eq("parent_id", parentID)
                            .eq("value", value));
            return dict.getName();
        }
    }

    //根据dictCode获取下级节点
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code", dictCode);
        Dict dictByDictCode = baseMapper.selectOne(wrapper);
        if (dictByDictCode == null){
            return null;
        }
        return this.findChildData(dictByDictCode.getId());
    }

    //判断parentID下面是否有子节点
    private boolean hasChildrenData(Long parentID){
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentID);
        Integer count = baseMapper.selectCount(wrapper);
        //若大于0则含有子节点
        return count > 0;
    }

}
