package com.user.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.user.exception.UserNotFoundException;
import com.user.service.IUserService;
import com.user.status.UserTokenStatus;

@Component
public class ValidateHeaderTokenUtil implements IValidateHeaderToken{
	
	@Autowired
	private IUserService userService;
	
	@Override
	public UserTokenStatus validateAuthToken(String userName, String authorizationHeader) {
		String accessToken=authorizationHeader.split("Bearer ")[1];
		String userToken;
		try {
			userToken = userService.getTokenByUsername(userName);
			if (userToken.equals(accessToken)) {
				return UserTokenStatus.SUCCESS;
			}else {
				return UserTokenStatus.INVALIDTOKEN;	
			}			
		} catch (UserNotFoundException e) {
			return UserTokenStatus.NOUSERFOUND;
		}
	}	
}