package com.user.dao;

import org.springframework.dao.EmptyResultDataAccessException;

import com.user.model.LoginInfoBean;
import com.user.model.SaltPasswordBean;
import com.user.model.TokenBean;
import com.user.model.UserProfileBean;

/**
 * The Interface IUserDAO.
 */
public interface IUserDAO {

	public Integer addUserProfile(UserProfileBean userProfileBean);

	public void addUserCredentials(LoginInfoBean loginInfoBean);

	public Integer updateTokenForUser(TokenBean tokenBean);

	public UserProfileBean getUserProfile(String userName, String accessToken);

	public String issueToken(String username)throws EmptyResultDataAccessException;

	public SaltPasswordBean getSaltPassByUserName(String username) throws EmptyResultDataAccessException;

	public boolean userExists(String username);

}