package yygh.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import yygh.mapper.DictMapper;
import yygh.model.cmn.Dict;
import yygh.vo.cmn.DictEeVo;

@Component
public class DictListener extends AnalysisEventListener<DictEeVo> {

    @Autowired
    private DictMapper dictMapper;

    //从第二行开始读取每行的内容
    @Override
    public void invoke(DictEeVo data, AnalysisContext context) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(data, dict);
        //将读取到的内容添加到数据库中
        dictMapper.insert(dict);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }
}
