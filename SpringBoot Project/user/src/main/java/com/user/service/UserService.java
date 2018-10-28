package com.user.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.user.dao.IUserDAO;
import com.user.exception.UserNotFoundException;
import com.user.model.LoginInfoBean;
import com.user.model.SaltPasswordBean;
import com.user.model.TokenBean;
import com.user.model.UserAuthBean;
import com.user.model.UserProfileBean;
import com.user.model.UserRequestBean;
import com.user.util.PasswordUtils;

/**
 * The Class UserService.
 */
@Service
public class UserService implements IUserService {

	/** Autowire user DAO. */
	@Autowired
	IUserDAO userDAO;
	
	//Add dummy users & their tokens & their auth info
	public boolean addUsers(UserRequestBean userRequestBean) {
		if (userDAO.userExists(userRequestBean.getUsername())) {
			return false;
		} else {

			UserProfileBean userProfileBean = new UserProfileBean();
			userProfileBean.setUsername(userRequestBean.getUsername());
			userProfileBean.setPhoneNo(userRequestBean.getPhoneNo());
			userProfileBean.setFullName(userRequestBean.getFullName());

			Integer userProfileId = userDAO.addUserProfile(userProfileBean);

			TokenBean tokenBean = new TokenBean();
			tokenBean.setUserProfileId(userProfileId);
			tokenBean.setUserToken(UUID.randomUUID().toString());
			tokenBean.setCreatedTime(new java.sql.Date(System.currentTimeMillis()));
			tokenBean.setValidityPeriod(3600000);
			Integer tokenId = userDAO.updateTokenForUser(tokenBean);

			LoginInfoBean loginInfoBean = new LoginInfoBean();
			loginInfoBean.setUsername(userRequestBean.getUsername());
			loginInfoBean.setPassword(userRequestBean.getPassword());
			loginInfoBean.setSaltValue(userRequestBean.getSaltValue());
			loginInfoBean.setTokenId(tokenId);
			userDAO.addUserCredentials(loginInfoBean);

			return true;
		}
	}

	//getUser profile by accessToken & username
	public UserProfileBean getUserProfile(String userName, String authorizationHeader) {
		String accessToken=authorizationHeader.split("Bearer ")[1];
		return userDAO.getUserProfile(userName, accessToken);
	}
	
	//return user's token
	public String issueToken(String username) {
		return userDAO.issueToken(username);
	}
	
	//get token by relevant username
	public String getTokenByUsername(String userName) throws UserNotFoundException {
		try {
			return userDAO.issueToken(userName);
		} catch (EmptyResultDataAccessException e) {
			throw new UserNotFoundException("User Not Found");
		}
	}
	
	//check if valid user
	public boolean isValidUser(UserAuthBean userAuthBean) {
		try {
			SaltPasswordBean saltPassbean = getSaltPassByUserName(userAuthBean.getUsername());

			PasswordUtils utils = new PasswordUtils();
			boolean passwordMatch = utils.authenticateByPassword(userAuthBean.getPassword(), saltPassbean.getPassword(),
					saltPassbean.getSaltValue());

			if (passwordMatch) {
				return true;
			} else {
				return false;
			}

		} catch (UserNotFoundException e) {
			return false;
		}
	}
	
	//get user's password & salt value for matching
	public SaltPasswordBean getSaltPassByUserName(String username) throws UserNotFoundException {
		try {
			return userDAO.getSaltPassByUserName(username);
		} catch (EmptyResultDataAccessException e) {
			throw new UserNotFoundException("User Not Found");
		}
	}
}