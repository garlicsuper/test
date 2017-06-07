package com.mi.test.test1.service;

import com.mi.test.test1.helper.DatabaseHelper;
import com.mi.test.test1.model.Customer;
import com.mi.test.test1.util.PropsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Administrator on 2017/6/6.
 */
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);


    /**
     * 获取客户列表
     * @param keyword
     * @return
     */
    public List<Customer> getCustomerList(String keyword) {
        String sql = "SELECT * FROM customer";
        return DatabaseHelper.queryEntityList(Customer.class,sql);
    }

//    /**
//     * 获取客户列表
//     * @param keyword
//     * @return
//     */
//    public List<Customer> getCustomerList(String keyword) {
//        Connection conn = null;
//        List<Customer> customerList = new ArrayList<Customer>();
//        try{
//            String sql = "SELECT * FROM customer";
//            conn = DatabaseHelper.getConnection();
//            PreparedStatement stmt = conn.prepareStatement(sql);
//            ResultSet rs = stmt.executeQuery();
//            while(rs.next()){
//                Customer customer = new Customer();
//                customer.setId(rs.getLong("id"));
//                customer.setName(rs.getString("name"));
//                customer.setContact(rs.getString("contact"));
//                customer.setTelephone(rs.getString("telephone"));
//                customer.setEmail(rs.getString("email"));
//                customer.setRemark(rs.getString("remark"));
//                customerList.add(customer);
//            }
//        } catch (SQLException e) {
//            LOGGER.error("execute sql failure",e);
//        }finally {
//            DatabaseHelper.closeConnection(conn);
//        }
//        return customerList;
//    }

    /**
     * 获取客户
     * @param id
     * @return
     */
    public Customer getCustomer(long id){
        String sql = "SELECT * FROM customer where id=?";
        return DatabaseHelper.queryEnity(Customer.class,sql,id);
    }

    /**
     * 创建客户
     * @param fieldMap
     * @return
     */
    public boolean createCustomer(Map<String,Object> fieldMap){
        return DatabaseHelper.insertEntity(Customer.class,fieldMap);
    }

    /**
     * 更新客户
     * @param id
     * @param fieldMap
     * @return
     */
    public boolean updateCustomer(long id,Map<String,Object> fieldMap){
        return DatabaseHelper.updateEntity(Customer.class,id,fieldMap);
    }

    /**
     * 删除客户
     * @param id
     * @return
     */
    public boolean deleteCustomer(long id){
        return DatabaseHelper.deleteEntity(Customer.class,id);
    }

}
