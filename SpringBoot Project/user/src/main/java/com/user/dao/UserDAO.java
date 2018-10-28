package com.user.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.user.model.LoginInfoBean;
import com.user.model.SaltPasswordBean;
import com.user.model.TokenBean;
import com.user.model.UserProfileBean;

/**
 * The Class UserDAO represent DAO layer.
 */
@Transactional
@Repository
public class UserDAO implements IUserDAO {

	/** use The SpringJdbcTemplate. */
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	//insert user profile & return profileId 
	public Integer addUserProfile(UserProfileBean userProfileBean) {
		// Add user
		String sql = "INSERT INTO user_profile (username, full_name, phone_no) values (?, ?, ?)";
		jdbcTemplate.update(sql, userProfileBean.getUsername(), userProfileBean.getFullName(),userProfileBean.getPhoneNo());

		sql = "SELECT user_profile_id FROM user_profile WHERE username=?";
		Integer userId = jdbcTemplate.queryForObject(sql, Integer.class, userProfileBean.getUsername());

		return userId;
	}
	
	//insert token for relevant userProfile & return tokenId
	public Integer updateTokenForUser(TokenBean tokenBean) {
		// Add token for user
		String sql = "insert into user_token (access_token,user_profile_id,created_time,validity_period) values(?,?,?,?)";
		jdbcTemplate.update(sql, tokenBean.getUserToken(), tokenBean.getUserProfileId(), tokenBean.getCreatedTime(),tokenBean.getValidityPeriod());

		sql = "SELECT token_id FROM user_token WHERE binary access_token=? AND user_profile_id=?";
		Integer token_id = jdbcTemplate.queryForObject(sql, Integer.class, tokenBean.getUserToken(),
				tokenBean.getUserProfileId());

		return token_id;

	}
	
	//insert user auth details for relevant tokenId
	public void addUserCredentials(LoginInfoBean loginInfoBean) {
		// Add credentials for user
		String sql = "insert into user_auth (token_id,username,password,salt_value) values(?,?,?,?)";
		jdbcTemplate.update(sql, loginInfoBean.getTokenId(), loginInfoBean.getUsername(), loginInfoBean.getPassword(),loginInfoBean.getSaltValue());
	}
	
	//check if username valid
	public boolean userExists(String username) {
		String sql = "SELECT count(*) FROM user_profile WHERE username = ?";
		int count = jdbcTemplate.queryForObject(sql, Integer.class, username);

		if (count == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	//get userProfile for given username & token
	public UserProfileBean getUserProfile(String userName, String accessToken) {
			String sql = "select up.username,up.full_name,up.phone_no from user_token ut,user_profile up where ut.user_profile_id=up.user_profile_id and up.username=? and binary ut.access_token=?;";
			RowMapper<UserProfileBean> rowMapper = new BeanPropertyRowMapper<UserProfileBean>(UserProfileBean.class);
			UserProfileBean userProfileBean = jdbcTemplate.queryForObject(sql, rowMapper, userName, accessToken);
			return userProfileBean;
	}

	//provide access token for the given user
	public String issueToken(String username) {
		String sql = "SELECT ut.access_token FROM user_token ut,user_auth ua where ut.token_id=ua.token_id and ua.username=?";
		return jdbcTemplate.queryForObject(sql, String.class, username);
	}

	//get salt value & password pair for validate password for given username
	public SaltPasswordBean getSaltPassByUserName(String username) throws EmptyResultDataAccessException{
		String sql = "select  salt_value,password from user_auth where username=?;";
		RowMapper<SaltPasswordBean> rowMapper = new BeanPropertyRowMapper<SaltPasswordBean>(SaltPasswordBean.class);
		SaltPasswordBean userTokenResponse= jdbcTemplate.queryForObject(sql, rowMapper, username);
		return userTokenResponse;			
	}
}