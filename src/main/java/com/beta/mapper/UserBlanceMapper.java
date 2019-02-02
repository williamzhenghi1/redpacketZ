package com.beta.mapper;

import com.beta.aqmp.UserBalanceTaskMessage;
import com.beta.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface UserBlanceMapper {

    @Update("update User set balance = balance + #{balanceChange} ,version =version + 1 where userid=#{userid} and version =#{version}")
    public boolean updateUserBalancceService(@Param("balanceChange")Double balance,@Param("userid")String userID ,@Param("version")int version);


    @Select("select version from User where userid = #{userid}")
    public int getVersion(@Param("userid") String  userid);

}
