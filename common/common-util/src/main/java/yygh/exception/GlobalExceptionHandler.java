package yygh.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import yygh.result.Result;

//全局异常处理
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail("全局异常捕获");
    }

    @ExceptionHandler(YyghException.class)
    @ResponseBody
    public Result error(YyghException e){
        e.printStackTrace();
        return Result.fail(e);
    }

}
