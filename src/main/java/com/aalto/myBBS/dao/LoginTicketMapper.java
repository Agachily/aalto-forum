package com.aalto.myBBS.dao;

import com.aalto.myBBS.service.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * This interface is for all the operations related to the login ticket
 */
@Mapper
@Deprecated
public interface LoginTicketMapper {

    @Insert({"insert into login_ticket (user_id, ticket, status, expired) ",
             "values(#{userId}, #{ticket}, #{status}, #{expired})"}) // The value is obtained from the loginTicket entity

    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({"select id, user_id, ticket, status, expired ",
             "from login_ticket where ticket=#{ticket}"})
    LoginTicket selectByTicket(String ticket);

    @Update("update login_ticket set status=#{status} where ticket=#{ticket}")
    int updateStatus(String ticket, int status);
}
