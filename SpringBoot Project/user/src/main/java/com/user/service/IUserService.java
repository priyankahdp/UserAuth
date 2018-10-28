package com.user.service;

import com.user.exception.UserNotFoundException;
import com.user.model.UserAuthBean;
import com.user.model.UserProfileBean;
import com.user.model.UserRequestBean;

/**
 * The Interface IUserService.
 */
public interface IUserService {

	/**
	 * Adds the users.
	 *
	 * @param userBean
	 * @return true, if successful
	 */
	public boolean addUsers(UserRequestBean userRequestBean);

	/**
	 * Gets the user profile.
	 *
	 * @param userName
	 * @param accessToken
	 * @return userProfile
	 */
	public UserProfileBean getUserProfile(String userName, String accessToken);

	/**
	 * Issue token for validUsers.
	 *
	 * @param username
	 * @return token
	 */
	public String issueToken(String username);

	/**
	 * return Token for the given username.
	 *
	 * @param username
	 * @return password & token pair
	 */
	public String getTokenByUsername(String userName)throws UserNotFoundException ;

	/**
	 * check if valid user by passing username & password pair
	 *
	 * @param userAuthBean
	 * @return true, if successful
	 */
	public boolean isValidUser(UserAuthBean userAuthBean);

}
