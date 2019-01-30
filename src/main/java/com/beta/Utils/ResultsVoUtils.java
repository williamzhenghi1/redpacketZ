package com.beta.Utils;


import com.beta.Emuns.ChannelEmus;
import com.beta.Vo.ResultsVo;

public class ResultsVoUtils {
    public static ResultsVo success(Object object)
    {
        ResultsVo resultsVo =new ResultsVo();
        resultsVo.setCode(ChannelEmus.Success.getCode());
        resultsVo.setMsg(ChannelEmus.Success.getMsg());
        resultsVo.setObject(object);
        return resultsVo;
    }

    public static ResultsVo fail()
    {
        ResultsVo resultsVo =new ResultsVo();
        resultsVo.setCode(ChannelEmus.NOT_FOUNT_Faild.getCode());
        resultsVo.setMsg(ChannelEmus.NOT_FOUNT_Faild.getMsg());
        resultsVo.setObject(null);
        return resultsVo;
    }
}
